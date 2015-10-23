package com.xhin.util;


/**
 * Created by xhinliang on 15-10-4.
 * Util
 */
public class TextUtil {
    private static TextUtil sTextUtil;

    private TextUtil() {
    }

    public static TextUtil getInstance() {
        if (sTextUtil == null)
            sTextUtil = new TextUtil();
        return sTextUtil;
    }

    public boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

}
