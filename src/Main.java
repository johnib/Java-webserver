import java.io.File;
import java.io.IOException;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */

public class Main {

    /* Constants */
    private final static String parsed_config = "Parsed config file, got:\n%s\n";
    private final static String regex_config = "(.+)=(.+)";


    public static void main(String[] args) throws IOException {

        /* parsing config file */
        Parser parser = new Parser(new File(new File(".").getCanonicalPath() + "/config.ini"), regex_config);

        int port = Integer.parseInt(parser.getValue("port"));
        int maxThreads = Integer.parseInt(parser.getValue("maxThreads"));
        String root = parser.getValue("root");
        String defaultPage = parser.getValue("defaultPage");

        System.out.printf(parsed_config, parser.toString());

    }
}
