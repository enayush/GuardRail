package com.example.guardrail.engine

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

interface TextNodeConsumer {
    fun onTextNode(node: AccessibilityNodeInfo, text: String)
}

interface CandidateCollector {
    fun onCandidate(candidate: DarkTextCandidate, node: AccessibilityNodeInfo)
}

class AccessibilityTreeTraverser(private val tag: String = "TreeTraverser") {

    /**
     * @param rootNode The root node to start traversal from
     * @param consumer Callback for processing each text node found
     */
    fun traverseAndLogVisibleText(rootNode: AccessibilityNodeInfo?, consumer: TextNodeConsumer) {
        if (rootNode == null) {
            Log.w(tag, "Root node is null, skipping traversal")
            return
        }

        Log.d(tag, "Starting DFS traversal from root: ${rootNode.className}")
        traverseNodeDFS(rootNode, depth = 0, consumer)
    }

    /**
     * Traverse the accessibility tree and collect DarkTextCandidate objects.
     * @param rootNode The root node to start traversal from
     * @param collector Callback for collecting each candidate found
     */
    fun collectCandidates(rootNode: AccessibilityNodeInfo?, collector: CandidateCollector) {
        if (rootNode == null) {
            Log.w(tag, "Root node is null, skipping traversal")
            return
        }

        Log.d(tag, "Starting DFS traversal to collect candidates from root: ${rootNode.className}")
        collectCandidatesDFS(rootNode, depth = 0, collector)
    }

    /**
     * @param node Current node being traversed
     * @param depth Current depth in the tree (for debugging/optimization)
     * @param consumer Callback for processing each text node found
     */
    private fun traverseNodeDFS(node: AccessibilityNodeInfo?, depth: Int, consumer: TextNodeConsumer) {
        if (node == null) return

        // Optimization: Skip nodes that are too deep (prevents excessive traversal)
        if (depth > MAX_DEPTH) {
            Log.v(tag, "Max depth reached, skipping further traversal")
            return
        }

        // Check if node is visible and has text
        if (node.isVisibleToUser && !node.text.isNullOrEmpty()) {
            consumer.onTextNode(node, node.text.toString())
        }

        // Recursively traverse all child nodes
        val childCount = node.childCount
        for (i in 0 until childCount) {
            val childNode = node.getChild(i)
            traverseNodeDFS(childNode, depth + 1, consumer)
        }
    }

    /**
     * DFS traversal that collects DarkTextCandidate objects.
     * @param node Current node being traversed
     * @param depth Current depth in the tree
     * @param collector Callback for collecting each candidate found
     */
    private fun collectCandidatesDFS(node: AccessibilityNodeInfo?, depth: Int, collector: CandidateCollector) {
        if (node == null) return

        // Optimization: Skip nodes that are too deep
        if (depth > MAX_DEPTH) {
            Log.v(tag, "Max depth reached, skipping further traversal")
            return
        }

        // Check if node is visible and has text
        if (node.isVisibleToUser && !node.text.isNullOrEmpty()) {
            val text = node.text.toString()
            val bounds = Rect()
            node.getBoundsInScreen(bounds)

            // Create a hash for the node (based on text and bounds for uniqueness)
            val nodeHash = (text + bounds.toString()).hashCode()

            val candidate = DarkTextCandidate(text, bounds, nodeHash)
            collector.onCandidate(candidate, node)
        }

        // Recursively traverse all child nodes
        val childCount = node.childCount
        for (i in 0 until childCount) {
            val childNode = node.getChild(i)
            collectCandidatesDFS(childNode, depth + 1, collector)
        }
    }

    companion object {
        // Maximum depth to traverse (prevents infinite loops and excessive processing)
        private const val MAX_DEPTH = 50
    }
}
