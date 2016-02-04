package Root;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 04/02/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class CrawlerResult {

    // use url.hashCode() to check if contained in this hashmap
    private ConcurrentHashMap<Integer, URL> url_history;

    /* statistics */
    private AtomicInteger externalLinks = new AtomicInteger(0);
    private AtomicInteger internalLinks = new AtomicInteger(0);
    private AtomicInteger images = new AtomicInteger(0);
    private AtomicInteger videos = new AtomicInteger(0);
    private AtomicInteger documents = new AtomicInteger(0);


    public CrawlerResult() {
        this.url_history = new ConcurrentHashMap<>(200, 0.75f, 2);
    }

    public boolean hasURL(URL url) {
        return this.url_history.containsKey(url.hashCode());
    }

    public void markVisited(URL url) {
        this.url_history.put(url.hashCode(), url);
    }

    public void increaseInternalLinks() {
        this.internalLinks.incrementAndGet();
    }

    public void increaseExternalLinks() {
        this.externalLinks.incrementAndGet();
    }
}
