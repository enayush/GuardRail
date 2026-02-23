package com.example.guardrail.lab

import android.content.Context
import org.json.JSONObject
import java.io.IOException

/**
 * Singleton object responsible for loading and managing dark pattern detection model weights.
 * 
 * Loads the dark_pattern_weights.json file from assets, parses it into a map of feature weights,
 * and extracts the bias value. The file is loaded lazily on first access.
 */
object DarkPatternModelLoader {
    
    private const val WEIGHTS_FILE = "dark_pattern_weights.json"
    private const val BIAS_KEY = "__BIAS__"
    
    // Lazy-loaded weights and bias
    private var weights: Map<String, Double>? = null
    private var bias: Double = 0.0
    private var isLoaded = false
    private var loadError: String? = null
    
    /**
     * Initialize the loader with the given Android context.
     * This method loads the weights file from assets and parses it.
     * 
     * @param context Android context used to access assets
     */
    @Synchronized
    fun initialize(context: Context) {
        if (isLoaded) return
        
        try {
            val json = loadJsonFromAssets(context)
            parseWeightsJson(json)
            isLoaded = true
            loadError = null
        } catch (e: IOException) {
            loadError = "Failed to load weights file: ${e.message}"
            setDefaultValues()
        } catch (e: Exception) {
            loadError = "Failed to parse weights file: ${e.message}"
            setDefaultValues()
        }
    }
    
    /**
     * Get the feature weights map.
     * 
     * @return Map of feature names to their weights, or empty map if loading failed
     */
    fun getWeights(): Map<String, Double> {
        if (!isLoaded) {
            throw IllegalStateException("DarkPatternModelLoader not initialized. Call initialize(context) first.")
        }
        return weights ?: emptyMap()
    }
    
    /**
     * Get the bias value.
     * 
     * @return The bias value, or 0.0 if loading failed
     */
    fun getBias(): Double {
        if (!isLoaded) {
            throw IllegalStateException("DarkPatternModelLoader not initialized. Call initialize(context) first.")
        }
        return bias
    }
    
    /**
     * Check if the weights were loaded successfully.
     * 
     * @return true if weights loaded successfully, false otherwise
     */
    fun isLoadedSuccessfully(): Boolean = isLoaded && loadError == null
    
    /**
     * Get the load error message if loading failed.
     * 
     * @return Error message or null if no error occurred
     */
    fun getLoadError(): String? = loadError
    
    /**
     * Load JSON content from the assets folder.
     */
    private fun loadJsonFromAssets(context: Context): String {
        return context.assets.open(WEIGHTS_FILE).bufferedReader().use { it.readText() }
    }
    
    /**
     * Parse the JSON string and extract weights and bias.
     */
    private fun parseWeightsJson(json: String) {
        val jsonObject = JSONObject(json)
        val weightsMap = mutableMapOf<String, Double>()
        var biasValue = 0.0
        
        // Iterate through all keys in the JSON object
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.getDouble(key)
            
            if (key == BIAS_KEY) {
                biasValue = value
            } else {
                weightsMap[key] = value
            }
        }
        
        weights = weightsMap
        bias = biasValue
    }
    
    /**
     * Set default values when loading fails.
     */
    private fun setDefaultValues() {
        weights = emptyMap()
        bias = 0.0
        isLoaded = true
    }
}

