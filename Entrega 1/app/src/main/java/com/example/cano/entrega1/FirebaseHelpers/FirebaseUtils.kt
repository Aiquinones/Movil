package com.example.cano.entrega1.FirebaseHelpers

/**
 * Various utilities for handling Firebase strings.
 */
object FirebaseUtils {
    /**
     * Encodes unsupported Firebase realtime database characters for keys into a valid string with minimal collision chance.
     */
    fun encode(s: String): String {
        val result = s.replace(".", "_P%ë5nN*")
                .replace("$", "_D%5nNë*")
                .replace("#", "_H%ë5Nn*")
                .replace("[", "_Oë5n%N*")
                .replace("]", "_5nN*C%ë")
                .replace("/", "*_S%ë5nN")

        return result
    }

    /**
     * Decodes a string encoded with [encode].
     */
    fun decode(s: String): String {
        val result = s.replace("_P%ë5nN*", ".")
                .replace("_D%5nNë*", "$")
                .replace("_H%ë5Nn*", "#")
                .replace("_Oë5n%N*", "[")
                .replace("_5nN*C%ë", "]")
                .replace("*_S%ë5nN", "/")

        return result
    }

}