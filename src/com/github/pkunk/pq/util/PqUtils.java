package com.github.pkunk.pq.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * User: pkunk
 * Date: 2012-01-01
 */
public class PqUtils {
    public static int random(int max) {
        return (int)(Math.random() * max);
    }

    public static int randomLow(int max) {
        return Math.min(random(max), random(max));
    }

    public static int randSign() {
        return random(2) * 2 - 1;
    }

    public static boolean odds(int chance, int outOf) {
        return random(outOf) < chance;
    }

    public static int square(int value) {
        return value*value;
    }

    public static String indefinite(String s, int qty) {
        if (qty == 1) {
            if ("AEIOUYaeiouy".contains(s.substring(0,1)))
                return "an " + s;
            else
                return "a " + s;
        } else {
            return String.valueOf(qty) + " " + plural(s);
        }
    }

    public static String definite(String s, int qty) {
        if (qty > 1)
            s = plural(s);
        return "the " + s;
    }

    public static String plural(String s) {
        if (s.endsWith("y"))
            return s.substring(0, s.length()-1) + "ies";
        else if (s.endsWith("us"))
            return s.substring(0,s.length()-2) + "i";
        else if (s.endsWith("ch") || s.endsWith("x") || s.endsWith("s") || s.endsWith("sh"))
            return s + "es";
        else if (s.endsWith("f"))
            return s.substring(0,s.length()-1) + "ves";
        else if (s.endsWith("man") || s.endsWith("Man"))
            return s.substring(0,s.length()-2) + "en";
        else return s + "s";
    }


    public static String roughTime(int seconds) {
        if (seconds < 120) return seconds + " seconds";
        else if (seconds < 60 * 120) return seconds/60 + " minutes";
        else if (seconds < 60 * 60 * 48) return seconds/3600 + " hours";
        else if (seconds < 60 * 60 * 24 * 60) return seconds/(3600*24) + " days";
        else if (seconds < 60 * 60 * 24 * 30 * 24) return seconds/(3600*24* 30) +" months";
        else return seconds/(3600*24*30*12) + " years";
    }

    public static String getLongTimestamp() {
        return getTimestamp("yyyy-MM-dd-HH-mm-ss-SSSS");
    }

    public static String getShortTimestamp() {
        return getTimestamp("yyyyMMddHHmmssSSSS");
    }

    private static String getTimestamp(String pattern) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }

}
