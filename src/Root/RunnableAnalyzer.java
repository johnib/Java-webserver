package Root;

import java.util.Map;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class RunnableAnalyzer implements Runnable {

    private static final HTMLParser HTML_PARSER = new HTMLParser();
    private static final Crawler crawler = Crawler.getInstance();
    private final String html;
    private final URL sourceUrl;

    public RunnableAnalyzer(URL sourceUrl, String html) {
        this.html = html;
        this.sourceUrl = sourceUrl;
    }

    @Override
    public void run() {
        this.pushDownloadUrlTasks();
        this.analyzeHtml();
    }

    private void analyzeHtml() {

    }

    /**
     * Extracts all URLs/URIs from the given HTML.
     * It pushes new URL tasks to the downloader queue.
     */
    private void pushDownloadUrlTasks() {
        // the reason parsing is done here and not in the constructor
        // is because the thread that constructs this object is a Root.RunnableDownloader.
        Map<String, String> urls = HTML_PARSER.parse(this.html);
        for (String currentUrl : urls.keySet()) {

            // build full url string
            String fullUrl;
            if (currentUrl.contains("uri")) {
                fullUrl = this.sourceUrl.toString().substring(0, this.sourceUrl.toString().lastIndexOf('/') + 1) + urls.get(currentUrl);
            } else {
                fullUrl = urls.get(currentUrl);
            }

            // create URL object
            URL url = URL.makeURL(fullUrl);
            if (url != null) {
                crawler.pushDownloadUrlTask(new RunnableDownloader(url));
            }
        }
    }
}