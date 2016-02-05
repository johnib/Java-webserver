package Root;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 08/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public interface IConfiguration {
    /* getters */
    int getPort();

    int getMaxThreads();

    HashMap getFileExtensions();

    String getRoot(HTTPRequest httpRequest);

    String getRoot();

    String getDefaultPage();

    /* TODO: change this when we do the bonus multi host names */
    IConfiguration getHostConfiguration(String hostName);

    String getRootAbsolutePath(HTTPRequest httpRequest) throws IOException;

    int getMaxDownloaders();

    int getMaxAnalyzers();

    HashSet<String> getImageExtensions();

    HashSet<String> getVideoExtensions();

    HashSet<String> getDocumentExtensions();
}
