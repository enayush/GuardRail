package com.example.guardrail.lab

/**
 * Logistic regression scorer for dark pattern detection.
 *
 * Calculates a log-odds score based on text features and learned weights.
 * Does NOT apply sigmoid transformation - returns raw log-odds score.
 *
 * @param weights Map of feature tokens to their weights
 * @param bias The bias term (intercept)
 */
class LogisticRegressionScorer(
    private val weights: Map<String, Double>,
    private val bias: Double
) {

    /**
     * Calculate the log-odds score for the given text.
     *
     * Process:
     * 1. Lowercase the text
     * 2. Remove non-alphabetic characters
     * 3. Split by whitespace into tokens
     * 4. Sum weights for tokens that exist in the weights map
     * 5. Add bias term
     *
     * @param text Input text to score
     * @return Log-odds score (NOT sigmoid-transformed)
     */
    fun score(text: String): Double {
        // Step 1: Lowercase text
        val lowercased = text.lowercase()

        // Step 2: Remove non-alphabetic characters (keep spaces for splitting)
        val alphabeticOnly = lowercased.replace(Regex("[^a-z\\s]"), " ")

        // Step 3: Split by whitespace into tokens
        val tokens = alphabeticOnly.split(Regex("\\s+")).filter { it.isNotEmpty() }

        // Step 4: Sum weights for matching tokens
        var sum = 0.0
        for (token in tokens) {
            weights[token]?.let { weight ->
                sum += weight
            }
        }

        // Step 5: Add bias term
        sum += bias

        // Return raw log-odds score (no sigmoid)
        return sum
    }
}

