package Root;


import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.util.Map;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 08/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class RunnableDownloader implements Runnable {
    private final URL downloadUrl;
    private static final Parser parser = new Parser("(.*): (.*)");

    public RunnableDownloader(URL downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /**
     * This function converts the buffer to string and removes the line ending
     *
     * @param stream The data to read from
     * @return a string containing all the data
     * @throws IOException
     */
    private static String ConvertToNoLineEndString(InputStream stream) throws IOException {
        String line;
        StringBuilder text = new StringBuilder();
        int contentLength = 0;

        try {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
                // Reading headers
                do {
                    line = buffer.readLine();
                    if (line != null && line.matches("[Cc]ontent-[Ll]ength: (\\d+)")) {
                        contentLength = Integer.parseInt(line.split(": ")[1]);
                    }
                } while (line != null && !line.isEmpty());

                while (contentLength > 0 && ((line = buffer.readLine()) != null)) {
                    text.append(line);

                    // Get bytes instead of just the length do to different
                    // encodings taking different amount of bytes
                    // The -2 is because of the line ending
                    contentLength = contentLength - line.getBytes().length - 2;
                }
            }
        } catch (java.net.SocketException e) {
            Logger.writeVerbose("Reading from socket failed");
        }

        return text.toString();
    }

    @Override
    public void run() {
        if (this.downloadUrl == null) return;

        try {
            Logger.writeAssignmentTrace("Downloader starts downloading URL: " + this.downloadUrl);
            try (InputStream headersStream = this.downloadUrl.openHeadStream()) {
                if (headersStream == null) {
                    Logger.writeInfo("RunnableDownloader - stream from URL was null");
                    return;
                }

                String urlAndPath = this.downloadUrl.getFullURL();
                if (urlAndPath.contains("?") ||
                        urlAndPath.contains("#")) {

                    int index = urlAndPath.indexOf("?");
                    if (index > 0) urlAndPath = urlAndPath.substring(0, index - 1);
                    index = urlAndPath.indexOf("#");
                    if (index > 0) urlAndPath = urlAndPath.substring(0, index - 1);
                }

                Map<String, String> headers = GetResponseHeaders(headersStream);

                if (urlEndsWithFileName(urlAndPath)) {
                    ////

                } else if (headers.containsKey("content-type") && headers.get("content-type").toLowerCase().contains("html")) {
                    // This is an html page
                    long size = Crawler.getInstance().getCrawlerResult().AddHtmlSize(Long.parseLong(headers.get("content-length")));
                    Logger.writeVerbose("Downloader - size of all pages is - " + size);
                } else {
                    // Ignore
                    Logger.writeWarning("Downloader downloaded a file that is not html or known file type");
                    return;
                }
            }

            try (InputStream stream = this.downloadUrl.openStream()) {
                    // Reading from the socket the data
                    String html = ConvertToNoLineEndString(stream);

                    // Checking if the read was successful
                    if (html.isEmpty()) return;

                    // Log
                    Logger.writeAssignmentTrace("Downloader ends downloading the URL: " + this.downloadUrl);
                    Logger.writeInfo("The html data:" + html);

                    // Sending to analyzer
                    Root.Crawler.getInstance().pushAnalyzeHtmlTask(new RunnableAnalyzer(this.downloadUrl, html));
            }
        } catch (Exception ex) {
            Logger.writeException(ex);
        }
    }

    private Map<String, String> GetResponseHeaders(InputStream stream) throws IOException {
        String line;
        StringBuilder text = new StringBuilder();

        try {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
                // Reading headers
                line = buffer.readLine();
                while (line != null && !line.isEmpty())
                {
                    text.append(line);
                    text.append(System.lineSeparator());
                    line = buffer.readLine();
                }
            }
        } catch (java.net.SocketException e) {
            Logger.writeVerbose("Reading from socket failed");
        }

        return parser.parse(text.toString());

    }

    private boolean urlEndsWithFileName(String urlAndPath) {
        return false;
        //throw new NotImplementedException();
    }
}
