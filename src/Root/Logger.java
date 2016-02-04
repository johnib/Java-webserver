package Root;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 09/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class Logger {

    private static int level = 4;
    private static boolean showServerLog = true;

    public static void writeVerbose(Object trace) {
        if (level >= 4) System.out.println(trace.toString());
    }

    public static void writeInfo(Object trace) {
        if (level >= 3) System.out.println(trace.toString());
    }

    public static void writeWarning(Object trace) {
        if (level >= 2) System.out.println(trace.toString());
    }

    public static void writeError(Object trace) {
        if (level >= 1) System.err.println(trace.toString());
    }

    public static void writeException(Exception ex) {
        writeError("An exception occurred....");
        if (level >= 0) ex.printStackTrace();
    }

    public static void writeAssignmentTrace(String trace) {
        System.out.println(trace);
    }

    public static void writeVerbose(String format, Object ... args) {
        if (level >= 4) System.out.printf(format, args);
    }

    public static void writeWebServerLog(String format, Object ... args) {
        if (showServerLog) System.out.printf(format, args);
    }

    public static void writeWebServerLog(Object trace) {
        if (showServerLog) System.out.println(trace.toString());
    }
}
