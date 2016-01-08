package Root;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class RunnableAnalyzer implements Runnable {

    private static final HtmlParser htmlParser = new HtmlParser();
    private static final Crawler crawler = Crawler.getInstance();
    private final String html;
    private final URL sourceUrl;

    public RunnableAnalyzer(URL sourceUrl, String html) {
        this.html = html;
        this.sourceUrl = sourceUrl;
    }

    @Override
    public void run() {
        // the reason parsing is done here and not in the constructor
        // is because the thread that constructs this object is a Root.RunnableDownloader.
        Map<String, String> urls = htmlParser.parse(html);
        for (String key : urls.keySet()) {
            String fullUrl = key.contains("uri") ? this.sourceUrl.getProtocol() + "://" + this.sourceUrl.getAuthority() + urls.get(key) : urls.get(key);
            try {
                URL url = new URL(fullUrl);
                crawler.pushDownloadUrlTask(new RunnableDownloader(url));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }


    }
}
