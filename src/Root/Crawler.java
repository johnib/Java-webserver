package Root;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */

import org.json.simple.JSONObject;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * This class will manage the whole crawler procedure once the Root.RunnableClient put the first URL task.
 */
public class Crawler {
    private static Crawler instance = null;
    private static boolean wasInit = false;

    private ThreadPool downloaders;
    private ThreadPool analyzers;
    private Root.crawlerResult crawlerResult;
    private IConfiguration config;

    private Crawler(IConfiguration config) {
        this.config = config;
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
        instance.analyzers.start();
        instance.downloaders.start();
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
    public synchronized Root.crawlerResult crawl(JSONObject config) {
        if (this.isWorking()) {
            Logger.writeInfo("The crawler is currently working");
            return null;
        }

        URL url = URL.makeURL((String) config.get("url"));
        boolean portScan = (boolean) config.get("portScan");
        boolean ignoreRobots = (boolean) config.get("ignoreRobots");

        if (url == null) {
            Logger.writeError("Crawler.crawl cannot create URL object from the url received in the JSON");
            //TODO: define behaviour
            return null;
        }

        CrawlerConfig crawlerConfig = new CrawlerConfig(url, portScan, ignoreRobots);
        this.crawlerResult = new crawlerResult();
        this.startCrawlingOn(crawlerConfig);
        return this.getResult();
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

    private void startCrawlingOn(CrawlerConfig config) {
        this.pushDownloadUrlTask(new RunnableDownloader(config.url));

        if (config.ignoreRobots) {
            // TODO: add all the linked of robot.txt to the downloader
            throw new NotImplementedException();
        }
    }

    private Root.crawlerResult getResult() {
        while (this.isWorking()) {
            try {
                Thread.sleep(1000 * 3);
            } catch (InterruptedException e) {
                // Left empty
                Logger.writeVerbose(e);
            }
        }

        return getCrawlerResult();
    }

    public Root.crawlerResult getCrawlerResult() {
        return crawlerResult;
    }

    public IConfiguration getConfig() {
        return config;
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
