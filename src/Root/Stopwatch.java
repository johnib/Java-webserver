package Root;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 10/02/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class Stopwatch {
    private final long start;

    /**
     * Initializes a new stopwatch.
     */
    public Stopwatch() {
        start = System.currentTimeMillis();
    }


    /**
     * Returns the elapsed CPU time (in milliseconds) since the stopwatch was created.
     *
     * @return elapsed CPU time (in milliseconds) since the stopwatch was created
     */
    public long elapsedTime() {
        long now = System.currentTimeMillis();
        return (now - start);
    }
}
