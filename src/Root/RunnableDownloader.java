package Root;

import java.io.*;
import java.net.URL;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 08/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class RunnableDownloader implements Runnable  {
    private final URL downloadUrl;

    public RunnableDownloader(URL downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public void run() {
        if (downloadUrl == null) return;

        try {
            Logger.writeAssignmentTrace("Downloader starts downloading URL: " + downloadUrl);
            try (InputStream stream = downloadUrl.openStream()) {  // throws an IOException
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
                    Root.Crawler.getInstance().pushAnzlyzeHtmlTask(new RunnableAnalyzer(downloadUrl, ConvertToNoLineEndString(buffer)));
                    Logger.writeAssignmentTrace("Downloader ends downloading the URL: " + downloadUrl);
                }
            }
        } catch (Exception ex) {
            Logger.writeException(ex);
        }
    }

    /**
     * This function converts the buffer to string and removes the line ending
     * @param buffer The data to read from
     * @return a string containing all the data
     * @throws IOException
     */
    private static String ConvertToNoLineEndString(BufferedReader buffer) throws IOException {
        String line;
        StringBuilder text = new StringBuilder();

        while ((line = buffer.readLine()) != null) {
            text.append(line);
        }

        return text.toString();
    }
}
