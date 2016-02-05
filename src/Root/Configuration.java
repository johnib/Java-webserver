package Root;

import sun.security.krb5.Config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/6/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class Configuration implements IConfiguration {
    /* Constants */
    private final static String parsed_config = "Parsed config file, got:\n%s\n";
    private final static String seperator = ",";

    /* private fields */
    private final static String regex_string = "(.+)=(.+)";
    private final int port;
    private final int maxThreads;
    private final String root;
    private final String defaultPage;
    private String rootAbsolutePath = null;
    private final int maxDownloaders;
    private final int maxAnalyzers;
    private final HashSet<String> imageExtensions;
    private final HashSet<String> videoExtensions;
    private final HashSet<String> documentExtensions;
    private final HashMap<String, HashSet> fileExtensions;

    /**
     * Creates a configuration object with all the details regarding the server settings.
     *
     * @param configuration the configuration file.
     * @throws IOException when file cannot be accessed for some reason.
     */
    public Configuration(File configuration) throws IOException {
        //TODO: config parsing needs to support multiple values per key (file formats)

        /* parse config */
        Parser parser = new Parser(regex_string);
        String fileContents = new String(Files.readAllBytes(configuration.toPath()));
        Map<String, String> dict = parser.parse(fileContents);

        String path = dict.get("root");

        File file = new File(path);
        if (file.exists()) { //TODO: why do we need this IF?
//            this.root = path;
            this.root = Paths.get(path).toAbsolutePath().normalize().toString();
        } else {
//            this.root = Paths.get(".", "/").toAbsolutePath().normalize().toString() + File.separator + path;
            this.root = Paths.get(path).toAbsolutePath().normalize().toString();
        }

        this.port = Integer.parseInt(dict.get("port"));
        this.maxThreads = Integer.parseInt(dict.get("maxthreads"));
        this.defaultPage = dict.get("defaultpage");
        this.maxDownloaders = Integer.parseInt(dict.get("maxdownloaders"));
        this.maxAnalyzers = Integer.parseInt(dict.get("maxanalyzers"));
        this.imageExtensions = new HashSet<>(Arrays.asList(dict.get("imageextensions").split(seperator)));
        this.videoExtensions = new HashSet<>(Arrays.asList(dict.get("videoextensions").split(seperator)));
        this.documentExtensions = new HashSet<>(Arrays.asList(dict.get("documentextensions").split(seperator)));

        final Configuration self = this;
        this.fileExtensions = new HashMap<String, HashSet>() {
            {
                put("image", self.imageExtensions);
                put("video", self.videoExtensions);
                put("document", self.documentExtensions);
            }
        };

        Logger.writeWebServerLog(parsed_config, parser.toString());
    }

    /* getters */
    @Override
    public int getPort() {
        return port;
    }

    @Override
    public int getMaxThreads() {
        return maxThreads;
    }

    @Override
    public HashMap getFileExtensions() {
        return this.fileExtensions;
    }

    @Override
    public String getRoot(HTTPRequest httpRequest) {
        String hostName = httpRequest.getHost();

        Configuration hostConfig = this.getHostConfiguration(hostName);

        return hostConfig.root;
    }

    @Override
    public String getRoot() {
        return this.root;
    }

    @Override
    public String getDefaultPage() {
        return defaultPage;
    }

    /* TODO: change this when we do the bonus multi host names */
    @Override
    public Configuration getHostConfiguration(String hostName) {
        return this;
    }

    @Override
    public String getRootAbsolutePath(HTTPRequest httpRequest) throws IOException {
        if (this.rootAbsolutePath != null) return this.rootAbsolutePath;

        // Getting the system absolute path.
        IConfiguration config = getHostConfiguration(httpRequest.getHost());
        File file = new File(config.getRoot(httpRequest));
        this.rootAbsolutePath = file.getCanonicalFile().getAbsolutePath();

        return this.rootAbsolutePath;
    }

    @Override
    public int getMaxDownloaders() {
        return maxDownloaders;
    }

    @Override
    public int getMaxAnalyzers() {
        return maxAnalyzers;
    }

    @Override
    public HashSet<String> getImageExtensions() {
        return imageExtensions;
    }

    @Override
    public HashSet<String> getVideoExtensions() {
        return videoExtensions;
    }

    @Override
    public HashSet<String> getDocumentExtensions() {
        return documentExtensions;
    }
}
