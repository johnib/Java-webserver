package Root;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class RunnableAnalyzer implements Runnable {

    private static final HtmlParser HTML_PARSER = new HtmlParser();
    private static final Crawler crawler = Crawler.getInstance();

    private final CrawlerResult crawlerResult;
    private final String html;
    private final URL sourceUrl;

    public RunnableAnalyzer(URL sourceUrl, String html) {
        this.html = html;
        this.sourceUrl = sourceUrl;
        this.crawlerResult = crawler.getTheCrawlerResultInstance();
    }

    @Override
    public void run() {
        try {
            ArrayList<URL> links = this.extractLinksFrom(this.html);
            for (URL url : links) {

                if (url == null) continue;

                if (url.getProtocol().toLowerCase().equals("http") &&
                        Crawler.getInstance().getRobots().allowUri(url.getUri())) {
                    if (this.sourceUrl.isInternalTo(url)) {
                        if (this.tryAddLinkToDownloader(url)) {
                            this.crawlerResult.increaseInternalLinks();
                        }

                    } else {
                        this.crawlerResult.increaseExternalLinks();

                        if (crawler.recognizesFileExtension(url)) {
                            this.tryAddLinkToDownloader(url);
                        }

                        this.crawlerResult.markVisited(url); // will be used when generating external domains statistics
                    }
                } else {
                    if (this.sourceUrl.isInternalTo(url)) {
                        this.crawlerResult.increaseInternalLinks();
                    } else {
                        this.crawlerResult.increaseExternalLinks();
                    }
                }

            }

            Logger.writeInfo("Analyzer: number of links extracted from:\t" + this.sourceUrl.toString() + "\t:" + links.size());
        } catch (Exception ex) {
            Logger.writeException(ex);
        }
    }

    /**
     * Extracts all URLs/URIs from the given HTML.
     *
     * @param html the html to parse
     * @return an array list of URL objects
     */
    private ArrayList<URL> extractLinksFrom(String html) {
        ArrayList<URL> links = new ArrayList<>();

        // the reason parsing is done here and not in the constructor
        // is because the thread that constructs this object is a Root.RunnableDownloader.
        Map<String, String> urls = HTML_PARSER.parse(html);

        for (String linkKey : urls.keySet()) {

            // create URL object
            URL url;
            if (linkKey.contains("uri")) {
                url = URL.makeURL(this.sourceUrl, urls.get(linkKey));
            } else {
                url = URL.makeURL(urls.get(linkKey));
            }

            links.add(url);
        }

        return links;
    }

    /**
     * Pushes new URL task to the downloader queue.
     *
     * @return true if url pushed and false otherwise.
     */
    private boolean tryAddLinkToDownloader(URL url) {
        boolean added = false;

        if (this.isNew(url)) {
            crawler.pushDownloadUrlTask(new RunnableDownloader(url));
            this.crawlerResult.markVisited(url);

            added = true;
        }

        return added;
    }

    /**
     * This method excludes URLs that already were crawled.
     *
     * @param url the URL to check
     * @return true if this URL is new, false otherwise.
     */
    private boolean isNew(URL url) {
        return !this.crawlerResult.hasURL(url);
    }
}