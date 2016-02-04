package Tests;

import Root.URL;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 04/02/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class URLTest {

    @Test
    public void testMakeURL() throws Exception {
        URL url = URL.makeURL("http://computernetworkstest.azurewebsites.net/");

    }

    @Test
    public void testGetProtocol() throws Exception {

    }

    @Test
    public void testGetDomain() throws Exception {

    }

    @Test
    public void testGetPort() throws Exception {

    }

    @Test
    public void testGetUri() throws Exception {

    }

    @Test
    public void testGetFullURL() throws Exception {

    }

    @Test
    public void testToString() throws Exception {

    }

    @Test
    public void testOpenStream() throws Exception {
        URL url = URL.makeURL("http://computernetworkstest.azurewebsites.net/");
        assert url != null;
        try(InputStream inputStream = url.openStream()) {
            Assert.assertNotNull(inputStream);
        }
    }

    @Test
    public void testClose() throws Exception {

    }
}