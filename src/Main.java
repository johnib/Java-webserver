import java.io.File;
import java.io.IOException;

/**
 * Created by Jonathan Yaniv on 30/11/2015.
 * Copyright (c) 2015 Jonathan Yaniv. All rights reserved.
 */
public class Main {

    /* Constants */
    private final static String parsed_config = "Parsed config file, got:\n%s\n";


    public static void main(String[] args) throws IOException {

        /* parsing config file */
        Parser parser = new Parser(new File(new File(".").getCanonicalPath() + "/config.ini"));

        int port = Integer.parseInt(parser.getValue("port"));
        int maxThreads = Integer.parseInt(parser.getValue("maxThreads"));
        String root = parser.getValue("root");
        String defaultPage = parser.getValue("defaultPage");

        System.out.printf(parsed_config, parser.toString());

    }
}
