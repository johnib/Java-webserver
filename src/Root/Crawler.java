package Root;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */

import org.json.simple.JSONObject;

/**
 * This class will manage the whole crawler procedure once the Root.RunnableClient put the first URL task.
 */
public class Crawler {
    private static Crawler instance = null;
    private static boolean wasInit = false;

    private ThreadPool downloaders;
    private ThreadPool analyzers;

    private Crawler(IConfiguration config) {
        this.downloaders = new ThreadPool(config.getMaxDownloaders(), "Downloader");
        this.analyzers = new ThreadPool(config.getMaxAnalyzers(), "Analyzer");
    }

    /**
     * Singleton does not supposed to have a constructor this is the solution
     *
     * @param config - config file containing downloaders and analyzers config
     * @throws UnsupportedOperationException if the class was already init
     */
    public static void Init(IConfiguration config) {
        if (instance != null)
            throw new UnsupportedOperationException("The crawler is already init. only one instance of the class is allowed.");
        instance = new Crawler(config);
        wasInit = true;
    }

    /**
     * Please init the Root.Crawler class before using the instance.
     *
     * @return The single instance of the crawler
     * @throws NullPointerException if the class wasn't init
     */
    public static Crawler getInstance() {
        if (instance == null)
            throw new NullPointerException("Please init the Root.Crawler class before using the instance.");
        return instance;
    }

    public static boolean wasInit() {
        return wasInit;
    }

    /**
     * Creates a CrawlerConfig instance out of the configuration received from client.
     * If all parameters are valid, starts the singleton crawler.
     *
     * @param config the JSON received from client.
     * @return the updated db.json
     */
    public static CrawlerResult crawl(JSONObject config) {
        URL url = URL.makeURL((String) config.get("url"));
        boolean portScan = (boolean) config.get("portScan");
        boolean ignoreRobots = (boolean) config.get("ignoreRobots");

        if (url == null) {
            Logger.writeError("Crawler.crawl cannot create URL object from the url received in the JSON");
            //TODO: define behaviour
            return null;
        }

        CrawlerConfig crawlerConfig = new CrawlerConfig(url, portScan, ignoreRobots);
        Crawler crawler = getInstance();
        crawler.startCrawlingOn(crawlerConfig);

        return crawler.getResult();
    }

    public void pushDownloadUrlTask(RunnableDownloader task) {
        this.downloaders.addTask(task);
    }

    public void pushAnalyzeHtmlTask(RunnableAnalyzer task) {
        this.analyzers.addTask(task);
    }

    public boolean isWorking() {
        return !this.analyzers.isQueueEmpty() || !this.downloaders.isQueueEmpty();
    }

    public void startCrawlingOn(CrawlerConfig config) {
        this.analyzers.start();
        this.downloaders.start();

        this.pushDownloadUrlTask(new RunnableDownloader(config.url));
    }

    private CrawlerResult getResult() {
        while (this.isWorking()) {
            try {
                Thread.sleep(1000 * 3);
            } catch (InterruptedException e) {
                // Left empty
            }
        }

        return null;
    }
}

class CrawlerConfig {

    public final URL url;
    public final boolean portScan;
    public final boolean ignoreRobots;

    public CrawlerConfig(URL url, boolean portScan, boolean ignoreRobots) {
        this.url = url;
        this.portScan = portScan;
        this.ignoreRobots = ignoreRobots;
    }
}
