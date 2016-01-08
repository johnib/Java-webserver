/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */

/**
 * This class will manage the whole crawler procedure once the RunnableClient put the first URL task.
 */
public class Crawler {
    private static Crawler instance = null;
    private ThreadPool downloaders;
    private ThreadPool analyzers;

    private boolean working = false;

    /**
     * Singleton dose not spouse to have a constructor this is the solution
     * @param config - config file containing downloaders and analyzers config
     * @throws UnsupportedOperationException if the class was already init
     */
    public static void Init(Configuration config) {
        if (instance != null) throw new UnsupportedOperationException("The crawler is already init. only one instance of the class is allowed.");
        instance = new Crawler(config);

    }

    /**
     * Please init the Crawler class before using the instance.
     * @return The single instance of the crawler
     * @throws NullPointerException if the class wasn't init
     */
    public static Crawler getInstance() {
        if (instance == null) throw new NullPointerException("Please init the Crawler class before using the instance.");
        return instance;
    }


    private Crawler(Configuration config) {
        this.downloaders = new ThreadPool(config.getMaxDownloaders());
        this.analyzers = new ThreadPool(config.getMaxAnalyzers());
    }


    public void start() {
        this.analyzers.start();
        this.downloaders.start();
    }

    public void pushDownloadUrlTask(RunnableDownloader task) {
        this.downloaders.addTask(task);
    }

    // TODO: change signature to driven type
    public void pushAnzlyzeHtmlTask(Runnable task) {
        this.analyzers.addTask(task);
    }

    // TODO: Check if the queues are empty
    public boolean isWorking() {
        return working;
    }
}
