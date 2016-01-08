import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 08/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class RunnableDownloader implements Runnable {
    private final String tempPath;
    private final URL downloadUrl;

    public RunnableDownloader(String tempPath, URL downloadUrl) {
        this.tempPath = tempPath;
        this.downloadUrl = downloadUrl;
    }

    @Override
    public void run() {
        if (downloadUrl == null) return;

        InputStream stream = null;
        BufferedReader buffer;
        String line;

        try {
            stream = downloadUrl.openStream();  // throws an IOException
            buffer = new BufferedReader(new InputStreamReader(stream));

            while ((line = buffer.readLine()) != null) {
                //TODO: finish implementing when Analyzers ready
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) stream.close();
            } catch (IOException ignored) {
            }
        }

    }
}
