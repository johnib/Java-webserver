package Root;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This is a lighter version of Java's URL class, requested by instructor to be implemented.
 */
public class URL implements java.io.Closeable{

    /* static properties */
    private static final URLParser parser = new URLParser();
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
    private Socket socket;



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

    public InputStream openStream() {
        // Only handle http requests
        if (this.getProtocol().compareToIgnoreCase(default_protocol) != 0){
            return null;
        }

        try {
            // Connecting to remote server
            setSocket(new Socket(this.getDomain(), this.getPort()));

            // Opening a stream to write to remote server
            byte[] headers = CreateGetRequest().getBytes(StandardCharsets.US_ASCII);

            System.out.println("-- Request to " + this.getFullURL() + " --");
            System.out.println(new String(headers));

            // Checking that the connection is still open
            if (socket.isClosed()) {

                return null;
            }

            socket.getOutputStream().write(headers);
            socket.getOutputStream().flush();
            return socket.getInputStream();
        } catch (IOException e) {
            Logger.writeException(e);
        }

        return null;
    }

    private String CreateGetRequest() {
        return String.format("GET %s HTTP/1.1" + Common.CRLF +
                "Host: %s" + Common.CRLF +
                "Accept: text/plain" + Common.CRLF +
                "User-Agent: CoolServer/1.1 (Windows NT 10.0; Win64; x64)" + Common.CRLF +
                "Accept-Language: en-US,en;q=0.8,he;q=0.6" + Common.CRLF + Common.CRLF, this.getUri(), this.getDomain());
    }

    @Override
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    private void setSocket(Socket socket) {
        try {
            if (this.socket != null && !this.socket.isClosed()) this.socket.close();
        } catch(Exception ex) {
            Logger.writeVerbose("Unable to close the socket in URL class");
        }

        this.socket = socket;
    }
}