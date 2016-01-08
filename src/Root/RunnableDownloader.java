package Root;

import java.io.*;
import java.net.URL;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 08/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class RunnableDownloader implements Runnable  {
    private final URL downloadUrl;
    private static String tempFilePrefix = "Crawler_";
    private static String tempFileSuffix = ".html";

    public RunnableDownloader(URL downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public void run() {
        if (downloadUrl == null) return;

        InputStream stream = null;
        BufferedReader buffer;

        try {
            File tempFile = File.createTempFile(tempFilePrefix, tempFileSuffix);  // throws an IOException
            Logger.writeAssignmentTrace("Downloader starts downloading URL: " + downloadUrl);
            stream = downloadUrl.openStream();  // throws an IOException
            buffer = new BufferedReader(new InputStreamReader(stream));
            FileUtils.writeToFile(tempFile, buffer);
            Logger.writeAssignmentTrace("Downloader ends downloading the URL: " + downloadUrl);
            // TODO: Check if the analyzer need a path to a file or the text of the html
            Root.Crawler.getInstance().pushAnzlyzeHtmlTask(new RunnableAnalyzer(downloadUrl, tempFile.getAbsolutePath()));

        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                if (stream != null) stream.close();
            } catch (IOException ignore) {
                // Left empty
            }
        }
    }
}
