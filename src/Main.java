import java.io.File;
import java.io.IOException;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/6/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class Main {

    /* Constants */
    private final static String fatal_error = "FATAL error, exiting\n";

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
            server = new Server(new Configuration(configFile));
        } catch (IOException e) {
            System.out.printf(fatal_error);
            System.exit(-1);
        }

        if (server.start())
            server.listen();
    }
}
