package Root;

import java.util.HashSet;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class PortScanner {

    private static final int default_scannersCount = 25;
    private static PortScanner instance = null;
    private HashSet<Integer> openPorts;
    private ThreadPool threadPool;
    private int scannersCount;

    private PortScanner(int scannersCount) {
        this.scannersCount = scannersCount;
        this.threadPool = new ThreadPool(this.scannersCount, "PortScanner pool");
        this.threadPool.start();

        this.openPorts = new HashSet();
    }

    public static synchronized PortScanner getInstance() {
        return getInstance(default_scannersCount);
    }

    public static synchronized PortScanner getInstance(int scannersCount) {
        if (instance == null) {
            instance = new PortScanner(scannersCount);
        }

        return instance;
    }

    /**
     * Scans the IP of the url. From 1 to 1024.
     *
     * @param url the url to scan
     * @return true if scan started, false if scan is in progress.
     */
    public synchronized boolean scan(URL url) {
        return scan(url, 1, 1024);
    }

    /**
     * Scans the IP of the url. From the start port to the end port.
     *
     * @param url   the url to scan
     * @param start starting port
     * @param end   ending port
     * @return true if scan started, false if scan is in progress.
     */
    public synchronized boolean scan(URL url, int start, int end) {
        if (this.isActive()) {
            return false;
        }

        if (!this.openPorts.isEmpty()) {
            this.openPorts.clear();
        }

        int amountPerScanner = (end - start + 1) / this.scannersCount;
        int nextPort = start;

        RunnableScanner runnableScanner;
        for (int scanner = 0; scanner < scannersCount - 1; scanner++) {
            runnableScanner = new RunnableScanner(this.openPorts, url, nextPort, nextPort + amountPerScanner - 1);
            this.threadPool.addTask(runnableScanner);
            nextPort += amountPerScanner;
        }

        runnableScanner = new RunnableScanner(this.openPorts, url, nextPort, end);
        this.threadPool.addTask(runnableScanner);

        return true;
    }

    /**
     * Returns a clone of the port scan and deletes the data.
     *
     * @return a clone of the results
     */
    public HashSet<Integer> getResults() {
        HashSet clone = (HashSet<Integer>) this.openPorts.clone();
        this.openPorts.clear();

        return clone;
    }

    public boolean isActive() {
        return this.threadPool.isActive();
    }
}
