package com.example.guardrail.engine

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.guardrail.lab.DarkPatternModelLoader
import com.example.guardrail.lab.LabWriter
import com.example.guardrail.lab.LogisticRegressionScorer
import com.example.guardrail.lab.buildDetectionLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.MessageDigest

class GuardRailService : AccessibilityService() {

    companion object {
        private const val TAG = "GuardRailService"
        private const val THRESHOLD = 0.0 // Score threshold for dark pattern classification
    }

    // Initialize the tree traverser for DFS operations
    private val treeTraverser = AccessibilityTreeTraverser(TAG)

    // Logistic regression scorer (initialized lazily)
    private var scorer: LogisticRegressionScorer? = null

    // Overlay engine for managing overlays
    private lateinit var overlayEngine: OverlayEngine

    // Debounce job for accessibility event processing
    private var debounceJob: Job? = null

    // Viewport signature for deduplication (based on visible content only)
    private var lastViewportSignature: String? = null

    // Flag to track if current event is a scroll event
    private var isScrollEvent = false

    private data class PendingDetection(
        val log: com.example.guardrail.lab.DetectionLog,
        val bounds: Rect,
        val timestamp: Long = System.currentTimeMillis()
    )
    private val pendingDetections = mutableListOf<PendingDetection>()

    // Coroutine scope for async operations (Main dispatcher for UI operations)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            // Extract package name and check blacklist immediately
            val packageName = it.packageName?.toString()

            // If package is blacklisted, return immediately without any processing
            if (AppBlacklist.isBlocked(packageName)) {
                return
            }

            val eventType = AccessibilityEvent.eventTypeToString(it.eventType)
            val packageNameSafe = packageName ?: "Unknown"

            Log.d(TAG, "Event Type: $eventType, Package Name: $packageNameSafe")

            when (it.eventType) {
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    if (pendingDetections.isNotEmpty()) {
                        val clickTime = System.currentTimeMillis()
                        val node = it.source
                        val bounds = Rect()

                        if (node != null) {
                            node.getBoundsInScreen(bounds)

                            // Get the exact center coordinate of the item the user tapped
                            val clickCenterX = bounds.centerX()
                            val clickCenterY = bounds.centerY()

                            val iterator = pendingDetections.iterator()
                            while(iterator.hasNext()) {
                                val pd = iterator.next()
                                val waitTime = clickTime - pd.timestamp

                                val action: String
                                val ignored: Boolean

                                // ACCURACY FIX:
                                // 1. Did the center of their tap land inside the dark pattern?
                                // 2. OR is the clicked item entirely wrapping the dark pattern? (e.g. a big button)
                                val clickedOnFlag = pd.bounds.contains(clickCenterX, clickCenterY) ||
                                                    bounds.contains(pd.bounds)

                                if (clickedOnFlag) {
                                    action = "CLICKED_FLAGGED_ITEM"
                                    ignored = true
                                    // Immediately clear overlay if they click the flagged item
                                    serviceScope.launch(Dispatchers.Main) { overlayEngine.clearOverlays() }
                                } else {
                                    action = "CLICKED_SAFE_ITEM"
                                    ignored = false
                                }

                                val completeLog = pd.log.copy(
                                    timeToNextActionMs = waitTime,
                                    postDetectionAction = action,
                                    warningIgnored = ignored
                                )
                                serviceScope.launch(Dispatchers.IO) {
                                    LabWriter.write(this@GuardRailService, completeLog)
                                }
                                // Clean up so it doesn't trigger on subsequent clicks
                                iterator.remove()
                            }
                        }
                    }
                    isScrollEvent = false
                }

                AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                    if (pendingDetections.isNotEmpty() && !isScrollEvent) {
                        val scrollTime = System.currentTimeMillis()
                        val iterator = pendingDetections.iterator()

                        while(iterator.hasNext()) {
                            val pd = iterator.next()
                            val waitTime = scrollTime - pd.timestamp

                            // If it's a real human scroll (>500ms hesitation)
                            if (waitTime > 500) {
                                val completeLog = pd.log.copy(
                                    timeToNextActionMs = waitTime,
                                    postDetectionAction = "SCROLLED_AWAY",
                                    warningIgnored = false
                                )
                                serviceScope.launch(Dispatchers.IO) {
                                    LabWriter.write(this@GuardRailService, completeLog)
                                }
                            }
                            // ACCURACY FIX: ALWAYS remove from pending, even if <500ms.
                            // Otherwise, fast swipes leave "ghost" detections in the list.
                            iterator.remove()
                        }
                    }
                    Log.d(TAG, "View scrolled - clearing overlays instantly")
                    isScrollEvent = true
                    overlayEngine.clearOverlays()
                }

                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    Log.d(TAG, "Window content changed")
                    isScrollEvent = false
                    // 2. DO NOT CLEAR HERE: This prevents the "flashing" bug caused by minor UI updates
                }
                else -> {
                    isScrollEvent = false
                }
            }

            // Cancel any existing debounce job
            debounceJob?.cancel()

            // Start a new debounced coroutine
            debounceJob = serviceScope.launch(Dispatchers.Main) {
                // Reduce wait time for a snappier response
                delay(150)

                // After delay, access rootInActiveWindow and run analysis
                val rootNode = rootInActiveWindow
                if (rootNode == null) {
                    Log.d(TAG, "rootInActiveWindow is null, returning")
                    return@launch
                }

                // Step 1: Collect all visible text candidates during DFS traversal
                val candidates = mutableListOf<Pair<DarkTextCandidate, AccessibilityNodeInfo>>()
                val candidateCollector = object : CandidateCollector {
                    override fun onCandidate(candidate: DarkTextCandidate, node: AccessibilityNodeInfo) {
                        candidates.add(Pair(candidate, node))
                    }
                }

                treeTraverser.collectCandidates(rootNode, candidateCollector)
                Log.d(TAG, "Collected ${candidates.size} text candidates")

                // Step 2: Compute viewport signature from visible content only
                val viewportSignature = buildViewportSignature(rootNode)
                Log.d(TAG, "Computed viewport signature: ${viewportSignature.take(16)}...")

                // Check if viewport has changed
                // For scroll events, always check signature (may reveal new content)
                // For other events, skip if signature unchanged
                if (viewportSignature == lastViewportSignature && !isScrollEvent) {
                    Log.d(TAG, "Viewport signature unchanged, skipping analysis")
                    return@launch
                }


                Log.d(TAG, "New viewport detected (signature changed or scroll event)")

                // Step 3: Score each candidate and collect confirmed dark patterns
                val confirmedDarkPatterns = mutableListOf<ConfirmedDarkPattern>()
                val startTime = System.nanoTime()

                // Iterate over all candidates
                candidates.forEach { (candidate, node) ->
                    // Score candidate.text using LogisticRegressionScorer
                    if (scorer != null) {
                        val score = scorer!!.score(candidate.text)

                        // If score > THRESHOLD, mark candidate as confirmed
                        if (score > THRESHOLD) {
                            val confirmed = ConfirmedDarkPattern(candidate, node, score)
                            confirmedDarkPatterns.add(confirmed)
                            Log.d(TAG, "🚨 CONFIRMED DARK PATTERN | Score: $score | Text: \"${candidate.text}\" | Bounds: ${candidate.bounds}")
                        } else {
                            Log.v(TAG, "Below threshold: $score | Text: \"${candidate.text}\"")
                        }
                    }
                }

                val endTime = System.nanoTime()
                val latencyMs = (endTime - startTime) / 1_000_000.0

                // Log summary
                Log.d(TAG, "Scored ${candidates.size} candidates in ${String.format("%.2f", latencyMs)} ms")
                Log.d(TAG, "Confirmed dark patterns: ${confirmedDarkPatterns.size}")

                // Update viewport signature since we processed this viewport
                lastViewportSignature = viewportSignature

                // Reset scroll flag
                isScrollEvent = false

                // Overlay confirmed dark patterns
                if (confirmedDarkPatterns.isNotEmpty() && scorer != null) {
                    Log.w(TAG, "🚨 DARK PATTERN ALERT | ${confirmedDarkPatterns.size} confirmed dark patterns detected | Package: $packageNameSafe")

                    // Extract ALL candidates and pass to OverlayEngine at once (multi-overlay batch)
                    val candidatesToOverlay = confirmedDarkPatterns.map { it.candidate }
                    Log.d(TAG, "Passing ${candidatesToOverlay.size} candidates to OverlayEngine for batch overlay drawing")
                    overlayEngine.drawOverlays(candidatesToOverlay)

                    // Log each confirmed dark pattern to database
                    confirmedDarkPatterns.forEach { confirmed ->
                        val log = buildDetectionLog(
                            context = this@GuardRailService,
                            packageName = packageNameSafe,
                            patternType = "dark_pattern",
                            detectedText = confirmed.candidate.text,
                            latencyMs = latencyMs.toLong()
                        )
                        pendingDetections.add(PendingDetection(log, confirmed.candidate.bounds))
                    }
                } else {
                    // Clear overlays if no dark patterns found
                    overlayEngine.clearOverlays()

                    if (scorer == null) {
                        Log.w(TAG, "Scorer not initialized, skipping scoring")
                    }
                }
            }
        }
    }


    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted - clearing overlays")
        overlayEngine.clearOverlays()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "GuardRailService connected")

        // Initialize OverlayEngine
        overlayEngine = OverlayEngine(this)

        // Initialize model loader and scorer
        try {
            DarkPatternModelLoader.initialize(this)
            if (DarkPatternModelLoader.isLoadedSuccessfully()) {
                val weights = DarkPatternModelLoader.getWeights()
                val bias = DarkPatternModelLoader.getBias()
                scorer = LogisticRegressionScorer(weights, bias)
                Log.d(TAG, "Scorer initialized with ${weights.size} weights, bias=$bias")
            } else {
                Log.e(TAG, "Failed to initialize scorer: ${DarkPatternModelLoader.getLoadError()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception initializing scorer: ${e.message}", e)
        }
    }

    /**
     * Build a viewport signature based only on visible content.
     * This ensures scrolling into new content produces a new signature.
     *
     * @param root The root AccessibilityNodeInfo to traverse
     * @return SHA-256 hash of visible content (sorted by position)
     */
    private fun buildViewportSignature(root: AccessibilityNodeInfo?): String {
        if (root == null) {
            return ""
        }

        // Collect visible text nodes with their positions
        val visibleNodes = mutableListOf<ViewportNode>()
        collectVisibleNodes(root, visibleNodes)

        // Sort by vertical position (top), then horizontal (left)
        visibleNodes.sortWith(compareBy({ it.top }, { it.left }))

        // Concatenate all entries
        val concatenated = visibleNodes.joinToString("|") { node ->
            "${node.text}:${node.top}:${node.left}"
        }

        // Hash the result with SHA-256
        return hashString(concatenated)
    }

    /**
     * Recursively collect visible nodes with text and their bounds.
     */
    private fun collectVisibleNodes(node: AccessibilityNodeInfo?, nodes: MutableList<ViewportNode>) {
        if (node == null) return

        // Only include nodes that are visible and have text
        if (node.isVisibleToUser && !node.text.isNullOrEmpty()) {
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            nodes.add(ViewportNode(node.text.toString(), bounds.top, bounds.left))
        }

        // Recursively traverse children
        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i)
            collectVisibleNodes(child, nodes)
        }
    }

    /**
     * Hash a string using SHA-256.
     */
    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Data class to hold visible node information for signature computation.
     */
    private data class ViewportNode(val text: String, val top: Int, val left: Int)

    override fun onDestroy() {
        super.onDestroy()
        // Cancel all coroutines when service is destroyed
        serviceScope.cancel()
        // Destroy overlay engine (cleans up overlays and resources)
        overlayEngine.destroy()
        Log.d(TAG, "GuardRailService destroyed")
    }
}
