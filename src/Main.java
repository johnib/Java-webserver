import java.io.File;
import java.io.IOException;

/**
 * Created by Jonathan Yaniv on 30/11/2015.
 * Copyright (c) 2015 Jonathan Yaniv. All rights reserved.
 */
public class Main {

    /* TEMP CONFIG.INI VALUES */
    private static final int port = 8080;
    private static final String root = "~/wwwroot/";
    private static final String defaultPage = "index.html";
    private static final int maxThreads = 10;

    public static void main(String[] args) throws IOException {

        //TODO: parse config file (for the meantime we have the relevant properties).


        File f = new File(new File(".").getCanonicalPath() + "/config.ini");
        Parser parser = new Parser(f);

        int port = 0;
        port = Integer.parseInt(parser.getValue("port"));
        String root = parser.getValue("root");
        String defaultPage = parser.getValue("defaultPage");
        int maxThreads = Integer.parseInt(parser.getValue("maxThreads"));


        System.out.printf("%s\n%s\n%s\n%s\n", port, root, defaultPage, maxThreads);

    }
}
