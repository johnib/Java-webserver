package Root;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 09/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class Logger {
    public static void writeInfo(String trace) {
        System.out.println(trace);
    }

    public static void writeError(String trace) {
        System.err.println(trace);
    }

    public static void writeAssignmentTrace(String trace) {
        System.out.println(trace);
    }
}
