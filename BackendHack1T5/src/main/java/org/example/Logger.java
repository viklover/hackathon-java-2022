package org.example;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {

    public static void print(String application, String message) {
        System.out.printf("%s - %s: %s\n", new SimpleDateFormat("HH:mm:ss", Locale.GERMAN).format(new Date()), application, message);
    }
}
