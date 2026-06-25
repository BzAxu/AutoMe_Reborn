package com.autome.data;

import java.util.List;

public class FilterMatcher {

    // 特殊关键字
    private static final String ONLY_NUMBER  = "{N}";
    private static final String ONLY_ENGLISH = "{E}";
    private static final String ONLY_CHINESE = "{C}";
    private static final String HAS_NUMBER   = "{Ns}";
    private static final String HAS_ENGLISH  = "{Es}";
    private static final String HAS_CHINESE  = "{Cs}";

    public static boolean shouldBypass(String message, List<String> filters) {
        for (String f : filters) {
            if (matches(message, f)) return true;
        }
        return false;
    }

    private static boolean matches(String msg, String filter) {
        switch (filter) {
            case ONLY_NUMBER:  return msg.matches("^[0-9]+$");
            case ONLY_ENGLISH: return msg.matches("^[a-zA-Z\\s]+$");
            case ONLY_CHINESE: return msg.matches("^[\\u4e00-\\u9fa5\\s]+$");
            case HAS_NUMBER:   return msg.matches(".*[0-9].*");
            case HAS_ENGLISH:  return msg.matches(".*[a-zA-Z].*");
            case HAS_CHINESE:  return msg.matches(".*[\\u4e00-\\u9fa5].*");
            default:
                // 普通前缀匹配（精确单词匹配 for "all" 类）
                if (msg.equalsIgnoreCase(filter)) return true;
                // 前缀字符匹配
                if (filter.length() == 1 && msg.startsWith(filter)) return true;
                // 多字符前缀
                if (filter.length() > 1 && !filter.startsWith("{") && msg.startsWith(filter)) return true;
                return false;
        }
    }
}
