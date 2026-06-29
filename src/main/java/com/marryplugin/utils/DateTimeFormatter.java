package com.marryplugin.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeFormatter {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public static String format(long timestamp) {
        return dateFormat.format(new Date(timestamp));
    }
}
