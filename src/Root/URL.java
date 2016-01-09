package Root;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */

import java.util.Map;

/**
 * This is a lighter version of Java's URL class, requested by instructor to be implemented.
 */
public class URL {

    /* static properties */
    private static final UrlParser parser = new UrlParser();
    private static final String _protocol = "protocol";
    private static final String _domain = "domain";
    private static final String _port = "port";
    private static final String _uri = "uri";
    private static final String default_protocol = "http";
    private static final int default_port = 80;

    /* non-static properties */
    private final String protocol;
    private final String domain;
    private final int port;
    private final String uri;
    private final String fullURL;


    private URL(String protocol, String domain, int port, String uri) {
        this.protocol = protocol;
        this.domain = domain;
        this.port = port;
        this.uri = uri;
        this.fullURL = String.format("%s://%s:%d%s", protocol, domain, port, uri != null ? uri : "");
    }

    /**
     * Returns a new URL instance representing the given url.
     * *
     *
     * @param url a valid url
     * @return null if URL is not valid.
     */
    public static URL makeURL(String url) {
        Map<String, String> dict = parser.parse(url);
        String protocol, domain, uri;
        int port;

        if (!dict.containsKey(_domain)) {
            return null;
        }

        domain = dict.get(_domain);
        protocol = dict.containsKey(_protocol) ? dict.get(_protocol) : default_protocol;
        uri = dict.get(_uri);
        port = dict.containsKey(_port) ? Integer.parseInt(dict.get(_port)) : default_port;

        return new URL(protocol, domain, port, uri);
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getDomain() {
        return this.domain;
    }

    public int getPort() {
        return this.port;
    }

    public String getUri() {
        return this.uri;
    }

    public String getFullURL() {
        return this.fullURL;
    }

    @Override
    public String toString() {
        return this.fullURL;
    }
}