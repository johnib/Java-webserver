/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */

/**
 * This class will manage the whole crawler procedure once the RunnableClient put the first URL task.
 */
public class Crawler {

    private ThreadPool downloaders;
    private ThreadPool analyzers;

    private boolean working = false;

    public Crawler(Configuration config) {
        this.downloaders = new ThreadPool(config.getMaxDownloaders());
        this.analyzers = new ThreadPool(config.getMaxAnalyzers());
    }


}
