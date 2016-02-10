package Root;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * This class will manage the whole crawler procedure once the Root.RunnableClient put the first URL task.
 */
public class Crawler {
    private static Crawler instance = null;
    private static boolean wasInit = false;
    private static PortScanner scanner;

    private ThreadPool downloaders;
    private ThreadPool analyzers;
    private CrawlerResult crawlerResult;
    private IConfiguration config;
    private HashSet<Pattern> robotsTxt;
    private RobotsTxt robots;

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

        scanner = PortScanner.getInstance();
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
    public synchronized CrawlerResult crawl(JSONObject config) {
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

        URL domainToCrawlOn = URL.stripUri(url);
        CrawlerConfig crawlerConfig = new CrawlerConfig(domainToCrawlOn, portScan, ignoreRobots, this.config.getResultsPath());
        this.crawlerResult = new CrawlerResult(crawlerConfig, this.config.getDatabase());
        this.crawlerResult.setDisrespectRobots(ignoreRobots);
        this.startCrawlingOn(crawlerConfig);

        if (crawlerConfig.portScan) {
            scanner.scan(crawlerConfig.url);
        }

        this.waitForFinish();
        this.crawlerResult.updateLocalFiles(scanner.getResults());

        return this.crawlerResult;
    }


    public void pushDownloadUrlTask(RunnableDownloader task) {
        this.downloaders.addTaskBlocking(task);
    }

    public void pushAnalyzeHtmlTask(RunnableAnalyzer task) {
        this.analyzers.addTaskBlocking(task);
    }

    public boolean isWorking() {
        return this.downloaders.isActive() || this.analyzers.isActive() || scanner.isActive();
    }

    private void startCrawlingOn(CrawlerConfig config) {
        this.robots = new RobotsTxt(config.ignoreRobots, config.url);
        this.pushDownloadUrlTask(new RunnableDownloader(config.url));
    }

    private CrawlerResult waitForFinish() {
        try {
            while (this.isWorking()) {
                Thread.sleep(3000);
            }

            for (int i = 0; i < 10; i++) {
                while (this.isWorking()) {
                    Thread.sleep(150);
                }
            }
        } catch (InterruptedException e) {
            Logger.writeVerbose(e);
        }

        return getTheCrawlerResultInstance();
    }

    /**
     * This method checks if the given link ends with knonw extension with relation to the config.ini file.
     *
     * @param url the url to check
     * @return true if the url's extension is known, false otherwise.
     */
    public boolean recognizesFileExtension(URL url) {
        boolean knownExt = false;
        HashMap<FileType, HashSet> knownFileTypes = this.config.getFileExtensions();

        for (FileType type : knownFileTypes.keySet()) {
            HashSet typeSet = knownFileTypes.get(type);
            if (typeSet.contains(url.getExtension())) {
                knownExt = true;
                break;
            }
        }

        return knownExt;
    }

    public CrawlerResult getTheCrawlerResultInstance() {
        return crawlerResult;
    }

    public IConfiguration getConfig() {
        return config;
    }

    public RobotsTxt getRobots() {
        return robots;
    }
}

class CrawlerConfig {

    public final URL url;
    public final boolean portScan;
    public final boolean ignoreRobots;
    public final String resultsPath;

    public CrawlerConfig(URL url, boolean portScan, boolean ignoreRobots, String resultsPath) {
        this.url = url;
        this.portScan = portScan;
        this.ignoreRobots = ignoreRobots;
        this.resultsPath = resultsPath;
    }
}
