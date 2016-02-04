package Root;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 08/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class RunnableDownloader implements Runnable {
    private final URL downloadUrl;

    public RunnableDownloader(URL downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /**
     * This function converts the buffer to string and removes the line ending
     *
     * @param buffer The data to read from
     * @return a string containing all the data
     * @throws IOException
     */
    private static String ConvertToNoLineEndString(BufferedReader buffer) throws IOException {
        String line;
        StringBuilder text = new StringBuilder();
        int contentLength = 0;

        try {

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
            try (InputStream stream = this.downloadUrl.openStream()) {
                if (stream == null) {
                    Logger.writeInfo("RunnableDownloader - stream from URL was null");
                    return;
                }

                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
                    // Reading from the socket the data
                    String html = ConvertToNoLineEndString(buffer);

                    // Checking if the read was successful
                    if (html.isEmpty()) return;

                    // Log
                    Logger.writeAssignmentTrace("Downloader ends downloading the URL: " + this.downloadUrl);
                    Logger.writeInfo("The html data:" + html);

                    // Sending to analyzer
                    Root.Crawler.getInstance().pushAnalyzeHtmlTask(new RunnableAnalyzer(this.downloadUrl, html));
                }
            }
        } catch (Exception ex) {
            Logger.writeException(ex);
        }
    }
}
