package Root;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 08/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class RunnableDownloader implements Runnable  {
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

        while ((line = buffer.readLine()) != null) {
            text.append(line);
        }

        return text.toString();
    }

    @Override
    public void run() {
        if (this.downloadUrl == null) return;

//        try {
//            Logger.writeAssignmentTrace("Downloader starts downloading URL: " + this.downloadUrl);
//            // TODO: no implementation for openStream(), need to open socket and send GET request manually
//            try (InputStream stream = this.downloadUrl.openStream()) {
//                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(stream))) {
//                    Root.Crawler.getInstance().pushAnzlyzeHtmlTask(new RunnableAnalyzer(this.downloadUrl, ConvertToNoLineEndString(buffer)));
//                    Logger.writeAssignmentTrace("Downloader ends downloading the URL: " + this.downloadUrl);
//                }
//            }
//        } catch (Exception ex) {
//            Logger.writeException(ex);
//        }
    }
}
