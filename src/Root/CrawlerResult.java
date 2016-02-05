package Root;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 04/02/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class crawlerResult {

    // use url.hashCode() to check if contained in this hashmap
    private final ConcurrentHashMap<Integer, URL> url_history;

    /* statistics */
    private final AtomicInteger externalLinks = new AtomicInteger(0);
    private final AtomicInteger internalLinks = new AtomicInteger(0);
    private final AtomicInteger images = new AtomicInteger(0);
    private final AtomicInteger videos = new AtomicInteger(0);
    private final AtomicInteger documents = new AtomicInteger(0);

    // Bytes lengths
    private final AtomicLong sumOfAllHtmlPagesBytes = new AtomicLong(0);
    private final AtomicLong sumOfAllImagesBytes = new AtomicLong(0);
    private final AtomicLong sumOfAllVideosBytes = new AtomicLong(0);
    private final AtomicLong sumOfAllDocsBytes = new AtomicLong(0);

    public crawlerResult() {
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

    public long addHtmlSize(long pageSize) {
        return sumOfAllHtmlPagesBytes.addAndGet(pageSize);
    }

    public long addImageSize(long size) {
        return sumOfAllImagesBytes.addAndGet(size);
    }

    public long addVideoSize(long size) {
        return sumOfAllVideosBytes.addAndGet(size);
    }

    public long addDocSize(long size) {
        return sumOfAllDocsBytes.addAndGet(size);
    }

    public long getSumOfAllHtmlPagesBytes() { return sumOfAllHtmlPagesBytes.get(); }
    public long getsumOfAllImagesBytes()    { return sumOfAllImagesBytes.get(); }
    public long getsumOfAllVideosBytes()    { return sumOfAllVideosBytes.get(); }
    public long getsumOfAllDocsBytes()      { return sumOfAllDocsBytes.get(); }

    public void createSummaryFile() {

    }

    public int getImages() {
        return images.get();
    }

    public int getVideos() {
        return videos.get();
    }

    public int getDocuments() {
        return documents.get();
    }
}
