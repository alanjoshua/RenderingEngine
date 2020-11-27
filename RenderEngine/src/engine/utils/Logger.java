package engine.utils;

public class Logger {

    public static boolean showLogs = true;
    public static boolean showErrors = true;

    public static void log(String text) {
        if (showLogs) {
            System.out.println(text);
        }
    }

    public static void log(Object text) {
        if (showLogs) {
            System.out.println(text.toString());
        }
    }

    public static void logError(String text) {
        if (showErrors) {
            System.err.println(text);
        }
    }

}
