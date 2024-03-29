package Root;

import java.io.File;
import java.io.IOException;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/6/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class Main {

    /* Constants */
    private final static String fatal_error = "FATAL error, either no config file or requested port is used, exiting\n";

    public static void main(String[] args) {
        File configFile = null;
        try {
            configFile = new File(new File(".").getCanonicalPath() + "/config.ini");

        } catch (IOException e) {
            System.err.printf("%s\n%s\n", e.getCause(), e.getMessage());
            e.printStackTrace();

            System.err.printf(fatal_error);
            System.exit(-1);
        }

        @SuppressWarnings("unused")
        Server server = null;
        try {
            Configuration configuration = new Configuration(configFile);
            server = new Server(configuration);
            Crawler.Init(configuration);
        } catch (IOException e) {
            Logger.writeError(fatal_error);
            Logger.writeException(e);
            System.exit(-1);
        }

        if (server.start())
            server.listen();
    }
}
