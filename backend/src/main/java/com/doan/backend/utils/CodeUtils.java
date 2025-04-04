package com.doan.backend.utils;

public class CodeUtils {
    public static String generateUniqueCode(String prefix, long count) {
        return prefix + count;
    }

    public static String removePrefix(String code, String prefix) {
        if (code.startsWith(prefix)) {
            return code.substring(prefix.length());
        }
        return code;
    }
}
