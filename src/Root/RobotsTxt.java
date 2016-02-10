package Root;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 10/02/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class RobotsTxt {
    private final String robotsFile;
    private HashSet<Pattern> robotsTxtDisallow;
    private HashSet<Pattern> robotsTxtAllow;

    public RobotsTxt(boolean ignoreRobots, URL url) {
        this.robotsFile = GetRobotsTxtFile(url);

        if (!ignoreRobots) {
            try {
                this.robotsTxtDisallow = getRobotsTxtDisallow(this.robotsFile);
                this.robotsTxtAllow = getRobotsTxtAllow(this.robotsFile);
            } catch (IOException e) {
                Logger.writeException(e);
                this.robotsTxtDisallow = new HashSet<>();
            }
        } else {
            // add all the linked of robot.txt to the downloader
            // note that some links contain '*' (wild card) - ignore these links
            // note that some links contain '$' (end of url) - ignore these links
            this.robotsTxtDisallow = new HashSet<>();
            this.robotsTxtAllow = new HashSet<>();
            HashSet<String> robotsTxtFull = getRobotsTxtAllLinks(this.robotsFile);

            Crawler crawler = Crawler.getInstance();
            CrawlerResult crawlerResult = crawler.getTheCrawlerResultInstance();

            for (String uri : robotsTxtFull) {
                URL robotsUri = URL.makeURL(url, uri);
                crawlerResult.markVisited(robotsUri);
                crawler.pushDownloadUrlTask(new RunnableDownloader(robotsUri));
            }
        }
    }

    private HashSet<Pattern> getRobotsTxtAllow(String robotsTxt) throws IOException {
        if (this.robotsTxtDisallow.isEmpty()) return new HashSet<>();

        HashSet<Pattern> result = new HashSet<>();

        Pattern regexPatternUri = Pattern.compile("^\\s*Allow: (?<uri>.*)", Pattern.CASE_INSENSITIVE);
        Pattern regexPatternUserAgent = Pattern.compile("^\\s*User-agent: (?<usaragent>.*)", Pattern.CASE_INSENSITIVE);

        // Going over the robots txt line by line
        BufferedReader reader = new BufferedReader(new StringReader(robotsTxt));
        ParseRobots(result, regexPatternUri, regexPatternUserAgent, reader);

        return result;
    }

    private void ParseRobots(HashSet<Pattern> result, Pattern regexPatternUri, Pattern regexPatternUserAgent, BufferedReader reader) throws IOException {
        boolean myUserAgent = false;
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher m = regexPatternUserAgent.matcher(line);
            if (m.find()) {
                String userAgent = m.group("usaragent");
                myUserAgent = userAgent.contains("*") || userAgent.toLowerCase().contains("coolserver");
            } else if (myUserAgent) {
                m = regexPatternUri.matcher(line);
                if (m.find()) {
                    String uri = "^" + m.group("uri");

                    // Converting to regex
                    // removing the last / to add a regex in the end
                    if ((uri.lastIndexOf('/') == uri.length() - 1) && uri.length() > 0)
                        uri = uri.substring(0, uri.length() - 1);
                    uri = uri.replaceAll("/", "\\\\/");
                    uri = uri.replaceAll("\\?", "\\\\?");
                    uri = uri.replaceAll("\\*", ".*");

                    if ((uri.lastIndexOf('$') == uri.length() - 1) && uri.length() > 0) {
                        uri = uri.substring(0, uri.length() - 1);
                        uri += "\\/{0,1}$";
                    } else if ((uri.lastIndexOf('*') == uri.length() - 1) && (uri.lastIndexOf('/') == uri.length() - 3)) {
                        uri = uri.substring(0, uri.length() - 4);
                        uri = "(" + uri + "\\/{0,1}$" + ")|(" + uri + "\\/.*)";
                    } else {
                        uri += "\\/{0,1}";
                    }


                    try {
                        result.add(Pattern.compile(uri));
                    } catch (Exception ex) {
                        Logger.writeError("Regex pater is wrong." + ex.toString());
                    }
                }
            }
        }
    }

    private HashSet<String> getRobotsTxtAllLinks(String robotsTxt) {
        if (robotsTxt == null) return new HashSet<>();

        Pattern regexPattern = Pattern.compile(".*: (?<uri>.*)");

        Matcher m = regexPattern.matcher(robotsTxt);
        HashSet<String> result = new HashSet<>();

        while (m.find()) {
            String relativeUri = m.group("uri");
            if (!relativeUri.contains("*") && !relativeUri.contains("$")) {
                result.add(relativeUri);
            }
        }

        return result;
    }

    private HashSet<Pattern> getRobotsTxtDisallow(String robotsTxt) throws IOException {

        if (robotsTxt == null) return new HashSet<>();

        HashSet<Pattern> result = new HashSet<>();

        Pattern regexPatternUri = Pattern.compile("^\\s*Disallow: (?<uri>.*)", Pattern.CASE_INSENSITIVE);
        Pattern regexPatternUserAgent = Pattern.compile("^\\s*User-agent: (?<usaragent>.*)", Pattern.CASE_INSENSITIVE);

        // Going over the robots txt line by line
        BufferedReader reader = new BufferedReader(new StringReader(robotsTxt));
        ParseRobots(result, regexPatternUri, regexPatternUserAgent, reader);


        return result;
    }

    private static String GetRobotsTxtFile(URL domainToCrawlOn) {
        try {
            URL url = URL.makeURL(domainToCrawlOn, "/robots.txt");
            try (InputStream inputStream = url.openStream()) {
                return RunnableDownloader.ConvertStreamToString(inputStream, false);
            }
        } catch (IOException ex) {
            Logger.writeException(ex);
            // Ignoring the problem probably 404
        }

        return null;
    }

    /**
     * @param uri the relative url of the url
     * @return This method takes the uri and checks against the robots txt
     */
    public boolean allowUri(String uri) {
        //TODO: remove? if it's not in the robotsTxtDisallow, then the URI is allowed... no?
        for (Pattern p : this.robotsTxtAllow) {
            Matcher m = p.matcher(uri);
            if (m.find()) {
                return true;
            }
        }

        for (Pattern p : this.robotsTxtDisallow) {
            Matcher m = p.matcher(uri);
            if (m.find()) {
                return false;
            }
        }

        return true;
    }
}
