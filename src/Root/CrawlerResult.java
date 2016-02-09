package Root;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 04/02/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class CrawlerResult {

    /* conventions */
    private static final String fileNameConvention = "%s/%s_%s.html";
    private static final String summaryPageFormat =
            "<br/>\n" +
                    "<ul class=\"list-group\">\n" +
                    "%s" + // contains the list of items
                    "</ul>\n" +
                    "<a ng-href=\"#/view1\">\n" +
                    "    <span class=\"btn btn-danger\">Back</span>\n" +
                    "</a>\n";
    private static final String listItemFormat =
            "        <li class=\"list-group-item\">\n" +
                    "            %s\n" + // contains the information
                    "        </li>\n";

    private static final String externalDomainLinkFormat = "<a ng-href=\"#/%s\">%s</a>";

    // use url.hashCode() to check if contained in this hashmap
    private final ConcurrentHashMap<Integer, URL> url_history;

    /* statistics */
    private final AtomicLong images = new AtomicLong(0);
    private final AtomicLong videos = new AtomicLong(0);
    private final AtomicLong documents = new AtomicLong(0);
    private final AtomicLong internalLinks = new AtomicLong(0);
    private final AtomicLong externalLinks = new AtomicLong(0);
    private final AtomicLong pages = new AtomicLong(0);
    private final long dateStart;
    private final AtomicLong totalRtt = new AtomicLong(0);
    private final AtomicLong numRtt = new AtomicLong(0);

    // Bytes lengths
    private final AtomicLong sumOfAllHtmlPagesBytes = new AtomicLong(0);
    private final AtomicLong sumOfAllImagesBytes = new AtomicLong(0);
    private final AtomicLong sumOfAllVideosBytes = new AtomicLong(0);
    private final AtomicLong sumOfAllDocsBytes = new AtomicLong(0);

    private final File database;
    private final URL domain;
    private final CrawlerConfig config;
    private final HashMap<String, AtomicLong> properties = new HashMap() {
        {
            put("images", images);
            put("sumImagesSize", sumOfAllImagesBytes);
            put("videos", videos);
            put("sumVideosSize", sumOfAllVideosBytes);
            put("documents", documents);
            put("sumDocumentsSize", sumOfAllDocsBytes);
            put("internalLinks", internalLinks);
            put("externalLinks", externalLinks);
            put("sumHtmlSize", sumOfAllHtmlPagesBytes);
            put("pages", pages);
        }
    };
    private final HashMap<String, String> propertiesTextualMapping = new HashMap() {
        {
            put("images", "Number of images: %d.");
            put("sumImagesSize", "Total size of images: %d bytes.");
            put("videos", "Number of videos: %d.");
            put("sumVideosSize", "Total size of videos: %d bytes.");
            put("documents", "Number of documents: %d.");
            put("sumDocumentsSize", "Total size of documents: %d bytes");
            put("internalLinks", "Number of internal links: %d.");
            put("externalLinks", "Number of external links: %d.");
            put("sumHtmlSize", "Total size of pages: %d bytes.");
            put("openPorts", "Open ports: %s.");
            put("externalDomains", "External Domains: %s.");
            put("pages", "Number of pages: %d.");
        }
    };
    private String openPorts;

    public CrawlerResult(CrawlerConfig config, File database) {
        this.config = config;
        this.domain = config.url;
        this.dateStart = System.currentTimeMillis();
        this.database = database;
        this.url_history = new ConcurrentHashMap<>(1000, 0.75f, 2);
    }

    public boolean hasURL(URL url) {
        return this.url_history.containsKey(url.hashCode());
    }

    public void markVisited(URL url) {
        this.url_history.put(url.hashCode(), url);
    }

    public void increaseInternalLinks() {
        this.internalLinks.incrementAndGet();
    }

    public void increaseExternalLinks() {
        this.externalLinks.incrementAndGet();
    }

    public long addHtmlSize(long pageSize) {
        long totalHtmlSize = sumOfAllHtmlPagesBytes.addAndGet(pageSize);
        this.pages.incrementAndGet();
        Logger.writeVerbose("Total HTML size so far: " + totalHtmlSize);

        return totalHtmlSize;
    }

    public long addImageSize(long size) {
        long totalImageSize = sumOfAllImagesBytes.addAndGet(size);
        this.images.incrementAndGet();
        Logger.writeVerbose("Total image size so far: " + totalImageSize);

        return totalImageSize;
    }

    public long addVideoSize(long size) {
        long totalVideoSize = sumOfAllVideosBytes.addAndGet(size);
        this.videos.incrementAndGet();
        Logger.writeVerbose("Total video size so far: " + totalVideoSize);

        return totalVideoSize;
    }

    public long addDocSize(long size) {
        long totalDocumentSize = sumOfAllDocsBytes.addAndGet(size);
        this.documents.incrementAndGet();
        Logger.writeVerbose("Total doc size so far: " + totalDocumentSize);

        return totalDocumentSize;
    }

    /**
     * Creates and writes the domain_startTime.html file for the crawled domain.
     *
     * @param db json containing the database
     * @return a reference to the written file
     * @throws IOException in case of File construction/writing error
     */
    public File createSummaryFile(JSONObject db) throws IOException {
        File summaryFile = new File(String.format(fileNameConvention, this.config.resultsPath, this.domain.getDomain(), this.dateStart));
        FileWriter fw = new FileWriter(summaryFile);

        // numeric statistics
        StringBuilder listItems = new StringBuilder("");
        for (String property : properties.keySet()) {
            String itemValue = String.format(propertiesTextualMapping.get(property), properties.get(property).get());
            String itemHtmlCode = String.format(listItemFormat, itemValue);
            listItems.append(itemHtmlCode);
        }

        // port scan statistics
        if (this.openPorts != null && !this.openPorts.isEmpty()) {
            String itemValue = String.format(propertiesTextualMapping.get("openPorts"), this.openPorts);
            String itemHtmlCode = String.format(listItemFormat, itemValue);
            listItems.append(itemHtmlCode);
        }

        // external domain statistics
        String externals = this.stringifyExternalDomains(db);
        if (!externals.isEmpty()) {
            String itemValue = String.format(propertiesTextualMapping.get("externalDomains"), externals);
            String itemHtmlCode = String.format(listItemFormat, itemValue);
            listItems.append(itemHtmlCode);
        }

        fw.write(String.format(summaryPageFormat, listItems.toString()));
        fw.flush();

        return summaryFile;
    }

    /**
     * Updates the data base with the new summary file
     *
     * @param summaryFile the summary file that was already written to disk
     * @throws IOException in case of File construction/writing error
     */
    private void updateDatabase(File summaryFile, JSONObject db) throws IOException {
        JSONArray results;

        if (db == null) {
            db = new JSONObject();
            results = new JSONArray();
        } else {
            results = (JSONArray) db.get("results");
            if (results == null) {
                results = new JSONArray();
            }
        }

        final String link = summaryFile.getName();
        final CrawlerResult self = this;
        JSONObject newEntity = new JSONObject() {
            {
                put("link", link);
                put("domain", self.domain.getDomain());
                put("dateStart", self.dateStart);
            }
        };

        results.add(newEntity);
        db.put("results", results);

        PrintWriter pw = new PrintWriter(this.database);
        pw.write(db.toJSONString());
        pw.flush();
    }

    private JSONObject readFromJSON(File file) {
        JSONObject json = null;

        try {
            FileReader reader = new FileReader(file);
            json = (JSONObject) new JSONParser().parse(reader);
        } catch (ParseException | FileNotFoundException e) {
            Logger.writeError("Current database is bad format, creating a new one instead.");
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }

        return json;
    }

    public void updateLocalFiles(HashSet portScan) {
        if (!portScan.isEmpty()) {
            ArrayList<Integer> portList = new ArrayList<>(portScan);
            Collections.sort(portList);
            this.openPorts = portList.toString();
        }

        try {
            JSONObject db = this.readFromJSON(this.database);
            File summaryPage = this.createSummaryFile(db);
            this.updateDatabase(summaryPage, db);
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: what happens when summary page cannot be created/written?
        }
    }

    /**
     * Creates a String containing all the external domains.
     *
     * @return a string containing all the external domains.
     */
    private String stringifyExternalDomains(JSONObject db) {
        StringBuilder list = new StringBuilder("");
        HashMap<String, String> crawlerHistory = this.getCrawlerHistory(db);

        HashSet<URL> externalDomains = this.getExternalDomains();
        for (URL external : externalDomains) {
            String value;
            if (crawlerHistory.containsKey(external.getDomain())) {
                value = String.format(externalDomainLinkFormat, crawlerHistory.get(external.getDomain()), external.getDomain());
            } else {
                value = external.getDomain();
            }

            list.append(value.concat(", "));
        }

        return list.toString().isEmpty() ? list.toString() : list.toString().substring(0, list.toString().lastIndexOf(", "));
    }

    private HashMap<String, String> getCrawlerHistory(JSONObject db) {
        HashMap<String, String> history = new HashMap<>();

        if (db != null && !db.isEmpty()) {
            JSONArray domains = (JSONArray) db.get("results");
            for (Object domain : domains) {
                String domainString = ((String) ((JSONObject) domain).get("domain"));
                String domainStatisticsPage = ((String) ((JSONObject) domain).get("link"));

                if (domainString != null && !domainString.isEmpty()) {
                    history.put(domainString, domainStatisticsPage);
                }
            }
        }

        return history;
    }

    private HashSet<URL> getExternalDomains() {
        HashSet<URL> externals = new HashSet<>();

        for (URL external : this.url_history.values()) {
            if (!external.getDomain().equals(this.domain.getDomain())) {
                externals.add(external);
            }
        }

        return externals;
    }

    public long addRtt(double rtt) {
        numRtt.incrementAndGet();
        return totalRtt.addAndGet((long) rtt);
    }

    public long getAvrageRtt() {
        return totalRtt.get() / numRtt.get();
    }
}
