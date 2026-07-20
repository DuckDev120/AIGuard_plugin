package com.duckslavi.aiguard.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool for fuzzy string matching
 * Implements Levenshtein Distance algorithm and Cosine Similarity calculation
 */
public class FuzzyMatcher {

    /**
     * Calculates the Levenshtein distance between two strings
     * 
     * @param s1 The first string
     * @param s2 The second string
     * @return The distance between the strings
     */
    public static int levenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return Math.max(s1 == null ? 0 : s1.length(), s2 == null ? 0 : s2.length());
        }

        s1 = s1.toLowerCase().trim();
        s2 = s2.toLowerCase().trim();

        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        // Initialize the first row and column
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        // Fill the matrix
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1, // Deletion
                        dp[i][j - 1] + 1), // Insertion
                        dp[i - 1][j - 1] + cost // Substitution
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    /**
     * Calculates similarity between two strings based on Levenshtein Distance
     * 
     * @param s1 The first string
     * @param s2 The second string
     * @return Similarity percentage (0.0 - 1.0)
     */
    public static double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        s1 = s1.toLowerCase().trim();
        s2 = s2.toLowerCase().trim();

        if (s1.equals(s2)) {
            return 1.0;
        }

        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0;
        }

        int distance = levenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLength;
    }

    /**
     * Calculates cosine similarity between two strings
     * 
     * @param s1 The first string
     * @param s2 The second string
     * @return Cosine similarity (0.0 - 1.0)
     */
    public static double cosineSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        s1 = s1.toLowerCase().trim();
        s2 = s2.toLowerCase().trim();

        if (s1.equals(s2)) {
            return 1.0;
        }

        Map<String, Integer> vector1 = getCharacterFrequency(s1);
        Map<String, Integer> vector2 = getCharacterFrequency(s2);

        return calculateCosineSimilarity(vector1, vector2);
    }

    /**
     * Creates a character frequency map for a string
     */
    private static Map<String, Integer> getCharacterFrequency(String text) {
        Map<String, Integer> frequency = new HashMap<>();
        for (char c : text.toCharArray()) {
            String character = String.valueOf(c);
            frequency.put(character, frequency.getOrDefault(character, 0) + 1);
        }
        return frequency;
    }

    /**
     * Calculates cosine similarity between two vectors
     */
    private static double calculateCosineSimilarity(Map<String, Integer> vector1, Map<String, Integer> vector2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        // Calculate scalar product and norms
        for (String key : vector1.keySet()) {
            int value1 = vector1.get(key);
            int value2 = vector2.getOrDefault(key, 0);

            dotProduct += value1 * value2;
            norm1 += value1 * value1;
        }

        for (int value : vector2.values()) {
            norm2 += value * value;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Calculates hybrid similarity (weighted average of Levenshtein and Cosine)
     * 
     * @param s1 The first string
     * @param s2 The second string
     * @return Hybrid similarity (0.0 - 1.0)
     */
    public static double hybridSimilarity(String s1, String s2) {
        double levenshteinSim = calculateSimilarity(s1, s2);
        double cosineSim = cosineSimilarity(s1, s2);

        // Weighted average - Levenshtein gets higher weight
        return (levenshteinSim * 0.7) + (cosineSim * 0.3);
    }

    /**
     * Checks if a string contains a banned word with a specific similarity
     * threshold
     * 
     * @param message    The message to check
     * @param bannedWord The banned word
     * @param threshold  Required similarity threshold
     * @return Check result with similarity details
     */
    public static MatchResult checkSimilarity(String message, String bannedWord, double threshold) {
        if (message == null || bannedWord == null) {
            return new MatchResult(false, 0.0, "", "");
        }

        // Clean message from color codes and special characters
        String cleanMessage = cleanMessage(message);
        String[] words = cleanMessage.split("\\s+");

        double maxSimilarity = 0.0;
        String bestMatch = "";

        // Check every word in the message
        for (String word : words) {
            if (word.trim().isEmpty())
                continue;

            double similarity = hybridSimilarity(word, bannedWord);
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatch = word;
            }
        }

        // Check the whole message as well
        double fullSimilarity = hybridSimilarity(cleanMessage, bannedWord);
        if (fullSimilarity > maxSimilarity) {
            maxSimilarity = fullSimilarity;
            bestMatch = cleanMessage;
        }

        boolean isMatch = maxSimilarity >= threshold;
        return new MatchResult(isMatch, maxSimilarity, bestMatch, bannedWord);
    }

    /**
     * Cleans message from color codes and special characters
     */
    private static String cleanMessage(String message) {
        // Remove Minecraft color codes
        String cleaned = message.replaceAll("§[0-9a-fk-or]", "");
        // Remove special characters (keep letters, numbers, and spaces)
        cleaned = cleaned.replaceAll("[^\\p{L}\\p{N}\\s]", " ");
        // Remove multiple spaces
        cleaned = cleaned.replaceAll("\\s+", " ");
        return cleaned.trim();
    }

    /**
     * Class for match result
     */
    public static class MatchResult {
        private final boolean isMatch;
        private final double similarity;
        private final String matchedText;
        private final String bannedWord;

        public MatchResult(boolean isMatch, double similarity, String matchedText, String bannedWord) {
            this.isMatch = isMatch;
            this.similarity = similarity;
            this.matchedText = matchedText;
            this.bannedWord = bannedWord;
        }

        public boolean isMatch() {
            return isMatch;
        }

        public double getSimilarity() {
            return similarity;
        }

        public String getMatchedText() {
            return matchedText;
        }

        public String getBannedWord() {
            return bannedWord;
        }

        public int getSimilarityPercentage() {
            return (int) Math.round(similarity * 100);
        }
    }
}
