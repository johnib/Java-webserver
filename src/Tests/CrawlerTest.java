package Tests;

import Root.Crawler;
import Root.RunnableDownloader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 08/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class CrawlerTest {
    MoqConfig moqConfig;

    @Before
    public void setUp() throws Exception {
        moqConfig = new MoqConfig();
        moqConfig.setMaxAnalyzers(1);
        moqConfig.setMaxDownloaders(1);
        Root.Crawler.Init(moqConfig);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testInit() throws Exception {
        if (!Root.Crawler.wasInit()) Root.Crawler.Init(moqConfig);
        Root.Crawler.Init(moqConfig);
    }

    @Test
    public void testGetInstance() throws Exception {
        Crawler instance = Crawler.getInstance();
        org.junit.Assert.assertNotNull(instance);
    }

    @Test
    public void testPushDownloadUrlTask() throws Exception {
        Crawler instance = Crawler.getInstance();
        instance.pushDownloadUrlTask(new RunnableDownloader(new URL("http://computernetworkstest.azurewebsites.net/")));
    }

    @Test
    public void testPushAnalyzeHtmlTask() throws Exception {

    }

    @Test
    public void testIsWorking() throws Exception {

    }
}