package Root;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 04/02/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class CrawlerConfig {
    private URL url;
    private boolean ignoreRobotTxt;
    private boolean portScan;

    // TODO: bonus (if there is time)
    //private String mobileNumber;

    public CrawlerConfig(URL url, boolean ignoreRobotTxt, boolean portScan) {
        this.url = url;
        this.ignoreRobotTxt = ignoreRobotTxt;
        this.portScan = portScan;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public boolean isIgnoreRobotTxt() {
        return ignoreRobotTxt;
    }

    public void setIgnoreRobotTxt(boolean ignoreRobotTxt) {
        this.ignoreRobotTxt = ignoreRobotTxt;
    }

    public boolean isPortScan() {
        return portScan;
    }

    public void setPortScan(boolean portScan) {
        this.portScan = portScan;
    }
}
