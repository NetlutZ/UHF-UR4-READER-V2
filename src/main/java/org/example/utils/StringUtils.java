package org.example.utils;

public class StringUtils {




    /**
     * @param src
     * @return
     */
    public static boolean isEmpty(String src) {
        return src == null || src.isEmpty();
    }

    /**
     * @param str
     * @return
     */
    public static boolean isHex(String str) {
        if (isEmpty(str)) {
            return false;
        }

        if (str.length() % 2 == 0) {
            String validate = "(?i)[0-9a-f]+";
            return str.matches(validate);
        }

        return false;
    }

    public static int toInt(String src, int defValue) {
        try {
            return Integer.valueOf(src);
        } catch (Exception e) {
            // TODO: handle exception
            return defValue;
        }
    }

    /**
     * @param src
     * @return
     */
    public static boolean isIPAddress(String src) {
        if(isEmpty(src)) {
            return false;
        }
        final String regexIp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])"
                + "(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        return src.matches(regexIp);
    }
}