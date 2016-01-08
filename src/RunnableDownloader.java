import java.io.*;
import java.net.URL;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 08/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class RunnableDownloader implements Runnable  {
    private final URL downloadUrl;
    private static String tempFilePrefix = "Crawler_";
    private static String tempFileSuffixs = ".html";

    public RunnableDownloader(URL downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public void run() {
        if (downloadUrl == null) return;

        InputStream stream = null;
        BufferedReader buffer;

        try {
            File tempFile = File.createTempFile(tempFilePrefix, tempFileSuffixs);  // throws an IOException
            stream = downloadUrl.openStream();  // throws an IOException
            buffer = new BufferedReader(new InputStreamReader(stream));
            FileUtils.writeToFile(tempFile, buffer);
            // TODO: Complete when analyzer is.
            //Crawler.getInstance().pushAnalyzerHtmlTask();

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
