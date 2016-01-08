package Root; /**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */

/**
 * This class will manage the whole crawler procedure once the Root.RunnableClient put the first URL task.
 */
public class Crawler {
    private static Crawler instance = null;
    private static boolean wasInit = false;

    private ThreadPool downloaders;
    private ThreadPool analyzers;

    /**
     * Singleton dose not spouse to have a constructor this is the solution
     * @param config - config file containing downloaders and analyzers config
     * @throws UnsupportedOperationException if the class was already init
     */
    public static void Init(IConfiguration config) {
        if (instance != null) throw new UnsupportedOperationException("The crawler is already init. only one instance of the class is allowed.");
        instance = new Crawler(config);
        wasInit = true;
        instance.start();
    }

    /**
     * Please init the Root.Crawler class before using the instance.
     * @return The single instance of the crawler
     * @throws NullPointerException if the class wasn't init
     */
    public static Crawler getInstance() {
        if (instance == null) throw new NullPointerException("Please init the Root.Crawler class before using the instance.");
        return instance;
    }


    private Crawler(IConfiguration config) {
        this.downloaders = new ThreadPool(config.getMaxDownloaders());
        this.analyzers = new ThreadPool(config.getMaxAnalyzers());
    }

    public static boolean wasInit() {
        return wasInit;
    }


    private void start() {
        this.analyzers.start();
        this.downloaders.start();
    }

    public void pushDownloadUrlTask(RunnableDownloader task) {
        this.downloaders.addTask(task);
    }

    public void pushAnzlyzeHtmlTask(RunnableAnalyzer task) {
        this.analyzers.addTask(task);
    }

    public boolean isWorking() {
        return !this.analyzers.isQueueEmpty() || !this.downloaders.isQueueEmpty();
    }
}
