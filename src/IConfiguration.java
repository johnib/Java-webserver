import java.io.IOException;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 08/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public interface IConfiguration {
    /* getters */
    int getPort();

    int getMaxThreads();

    String getRoot(HTTPRequest httpRequest);

    String getDefaultPage();

    /* TODO: change this when we do the bonus multi host names */
    Configuration getHostConfiguration(String hostName);

    String getRootAbsolutePath(HTTPRequest httpRequest) throws IOException;

    int getMaxDownloaders();

    int getMaxAnalyzers();
}
