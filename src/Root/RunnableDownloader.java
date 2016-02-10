package Root;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.Map;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 08/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class RunnableDownloader implements Runnable {
    private static final Parser parser = new Parser("(.*): (.*)");
    private final URL downloadUrl;

    public RunnableDownloader(URL downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /**
     * This function converts the buffer to string and sometimes removes the line ending
     *
     * @param stream The data to read from
     * @return a string containing all the data
     * @throws IOException
     */
    public static String ConvertStreamToString(InputStream stream) throws IOException {
        String line;
        StringBuilder text = new StringBuilder();
        int contentLength = 0, lengthRead;
        boolean isChunked = false;
        char[] arr;

        try {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream, "US-ASCII"))) {
                // Reading headers
                do {
                    line = buffer.readLine();
                    if (line != null && line.matches("[Cc]ontent-[Ll]ength: (\\d+)")) {
                        contentLength = Integer.parseInt(line.split(": ")[1]);
                    } else if (line != null && line.matches("[Tt]ransfer-[Ee]ncoding: [Cc]hunked")) {
                        isChunked = true;
                    }
                } while (line != null && !line.isEmpty());

                if (isChunked) {
                    lengthRead = readChunks(buffer, text);

                } else {
                    arr = read(buffer, contentLength);
                    lengthRead = contentLength;

                    text.append(arr);
                }

                // update statistics
                Crawler.getInstance().getTheCrawlerResultInstance().addHtmlSize(lengthRead);
            }
        } catch (SocketException e) {
            Logger.writeVerbose("Reading from socket failed");
        }

        return text.toString();
    }

    /**
     * Returns a char[] containing contentLength chars read from the buffer.
     *
     * @param buffer        input source
     * @param contentLength amount to read
     * @return char[] of the content
     * @throws IOException
     */
    private static char[] read(BufferedReader buffer, int contentLength) throws IOException {
        char[] content = new char[contentLength];
        int charsRead = 0;

        while (charsRead != -1 && charsRead < contentLength) {
            charsRead += buffer.read(content, charsRead, contentLength - charsRead);
        }

        return content;
    }

    /**
     * Reads response in chunks
     *
     * @param buffer the input source
     * @param text   where to push the contents
     * @return the number of chars read
     * @throws IOException
     */
    private static int readChunks(BufferedReader buffer, StringBuilder text) throws IOException {
        int contentLength = Integer.parseInt(buffer.readLine(), 16), totalLength = 0;
        char[] content;

        while (contentLength > 0) {
            content = read(buffer, contentLength);

            text.append(content);
            totalLength += contentLength;

            buffer.readLine(); // read empty line before new chunk's length
            contentLength = Integer.parseInt(buffer.readLine(), 16); // read new chunk's length
        }

        return totalLength;
    }

    @Override
    public void run() {
        if (this.downloadUrl == null) return;

        CrawlerResult crawlerResultInstance = Crawler.getInstance().getTheCrawlerResultInstance();
        try {
            Logger.writeAssignmentTrace("Downloader starts downloading URL: " + this.downloadUrl);
            try (InputStream headersStream = this.downloadUrl.openHeadStream()) {
                if (headersStream == null) {
                    Logger.writeInfo("RunnableDownloader - stream from URL was null");
                    return;
                }

                String fullURL = this.downloadUrl.getFullURL();
                Map<String, String> headers = GetResponseHeaders(headersStream);
                FileType fileType = urlEndsWithFileName(fullURL);

                if (fileType != FileType.Unknown && headers.containsKey("content-length")) {
                    long contentLength = Long.parseLong(headers.get("content-length"));
                    switch (fileType) {
                        case Image:
                            crawlerResultInstance.addImageSize(contentLength);
                            break;
                        case Video:
                            crawlerResultInstance.addVideoSize(contentLength);
                            break;
                        case Document:
                            crawlerResultInstance.addDocSize(contentLength);
                            break;
                    }
                } else if (headers.containsKey("content-type") && headers.get("content-type").toLowerCase().contains("html")) {
                    // This is an html page
                    ProcessHtmlPage();
                } else {
                    // Ignore
                    Logger.writeWarning("Downloader downloaded a file that is not html or known file type");
                }
            }
        } catch (Exception ex) {
            Logger.writeException(ex);
        }
    }

    private void ProcessHtmlPage() throws IOException {
        Stopwatch stopwatch = new Stopwatch();
        // Processing an html page
        try (InputStream stream = this.downloadUrl.openStream()) {
            // Reading from the socket the data
            String html = ConvertStreamToString(stream);
            Crawler.getInstance().getTheCrawlerResultInstance().addRTT(stopwatch.elapsedTime());

            // Checking if the read was successful
            if (html.isEmpty()) return;

            // Log
            Logger.writeAssignmentTrace("Downloader ends downloading the URL: " + this.downloadUrl);
            Logger.writeInfo("The html data:" + html);

            // Sending to analyzer
            Crawler.getInstance().pushAnalyzeHtmlTask(new RunnableAnalyzer(this.downloadUrl, html));
        }
    }

    private Map<String, String> GetResponseHeaders(InputStream stream) throws IOException {
        String line;
        StringBuilder text = new StringBuilder();

        try {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
                // Reading headers
                line = buffer.readLine();
                while (line != null && !line.isEmpty()) {
                    text.append(line);
                    text.append(System.lineSeparator());
                    line = buffer.readLine();
                }
            }
        } catch (java.net.SocketException e) {
            Logger.writeVerbose("Reading from socket failed");
        }
        Logger.writeVerbose("-- Remote Server Response --");
        Logger.writeVerbose(text.toString());
        return parser.parse(text.toString());
    }

    private FileType urlEndsWithFileName(String urlAndPath) {
        String fileExtension = urlAndPath.substring(urlAndPath.lastIndexOf('.') + 1);
        IConfiguration config = Crawler.getInstance().getConfig();

        if (config.getImageExtensions().contains(fileExtension)) return FileType.Image;
        if (config.getDocumentExtensions().contains(fileExtension)) return FileType.Document;
        if (config.getVideoExtensions().contains(fileExtension)) return FileType.Video;
        return FileType.Unknown;
    }
}
