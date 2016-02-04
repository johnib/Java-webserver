package Root;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 04/02/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class CrawlerResult {
    private final AtomicLong sumOfAllHtmlPages = new AtomicLong(0);

    public long AddHtmlSize(long pageSize) {
        return sumOfAllHtmlPages.addAndGet(pageSize);
    }

    public long getSumOfAllHtmlPages(){
        return sumOfAllHtmlPages.get();
    }
}
