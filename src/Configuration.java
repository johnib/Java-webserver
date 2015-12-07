import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/6/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class Configuration {
    /* Constants */
    private final static String parsed_config = "Parsed config file, got:\n%s\n";

    /* private fields */
    private final static String regex_string = "(.+)=(.+)";
    private final int port;
    private final int maxThreads;
    private final String root;
    private final String defaultPage;

    public Configuration(File configuration) throws IOException {
        /* parse config */
        Parser parser = new Parser(regex_string);
        String fileContents = new String(Files.readAllBytes(configuration.toPath()));
        parser.parse(fileContents);

        this.port = Integer.parseInt(parser.getValue("port"));
        this.maxThreads = Integer.parseInt(parser.getValue("maxThreads"));
        this.root = parser.getValue("root");
        this.defaultPage = parser.getValue("defaultPage");

        System.out.printf(parsed_config, parser.toString());
    }

    public int getPort() {
        return port;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public String getRoot() {
        return root;
    }

    public String getDefaultPage() {
        return defaultPage;
    }
}
