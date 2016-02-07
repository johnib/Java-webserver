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

    private static final String fileNameConvention = "%s/%s_%s.html";
    private static final String summaryPageFormat =
            "<br/>\n" +
                    "<div class=\"container\">\n" +
                    "    <ul class=\"list-group\">\n" +
                    "%s" + // contains the list items
                    "    </ul>\n" +
                    "    <a ng-href=\"#/view1\">\n" +
                    "        <span class=\"btn btn-danger\">Back</span>\n" +
                    "    </a>\n" +
                    "</div>\n";
    private static final String listItemFormat =
            "        <li class=\"list-group-item\">\n" +
                    "            %s\n" + // contains the information
                    "        </li>\n";
    // use url.hashCode() to check if contained in this hashmap
    private final ConcurrentHashMap<Integer, URL> url_history;
    /* statistics */
    private final AtomicLong images = new AtomicLong(0);
    private final AtomicLong videos = new AtomicLong(0);
    private final AtomicLong documents = new AtomicLong(0);
    private final AtomicLong internalLinks = new AtomicLong(0);
    private final AtomicLong externalLinks = new AtomicLong(0);
    private final long dateStart;
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
        }
    };
    private String openPorts;

    public CrawlerResult(CrawlerConfig config, File database) {
        this.config = config;
        this.domain = config.url;
        this.dateStart = System.currentTimeMillis();
        this.database = database;
        this.url_history = new ConcurrentHashMap<>(200, 0.75f, 2);
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
        return sumOfAllHtmlPagesBytes.addAndGet(pageSize);
    }

    public long addImageSize(long size) {
        return sumOfAllImagesBytes.addAndGet(size);
    }

    public long addVideoSize(long size) {
        return sumOfAllVideosBytes.addAndGet(size);
    }

    public long addDocSize(long size) {
        return sumOfAllDocsBytes.addAndGet(size);
    }

    public long getSumOfAllHtmlPagesBytes() {
        return sumOfAllHtmlPagesBytes.get();
    }

    public long getsumOfAllImagesBytes() {
        return sumOfAllImagesBytes.get();
    }

    public long getsumOfAllVideosBytes() {
        return sumOfAllVideosBytes.get();
    }

    public long getsumOfAllDocsBytes() {
        return sumOfAllDocsBytes.get();
    }

    /**
     * Creates and writes the domain_startTime.html file for the crawled domain.
     *
     * @return a reference to the written file
     * @throws IOException in case of File construction/writing error
     */
    public File createSummaryFile() throws IOException {
        File summaryFile = new File(String.format(fileNameConvention, this.config.resultsPath, this.domain.getDomain(), this.dateStart));
        FileWriter fw = new FileWriter(summaryFile);

        StringBuilder listItems = new StringBuilder("");
        for (String property : properties.keySet()) {
            String itemValue = String.format(propertiesTextualMapping.get(property), properties.get(property).get());
            String itemHtmlCode = String.format(listItemFormat, itemValue);
            listItems.append(itemHtmlCode);
        }

        // port list
        String itemValue = String.format(propertiesTextualMapping.get("openPorts"), this.openPorts);
        String itemHtmlCode = String.format(listItemFormat, itemValue);
        listItems.append(itemHtmlCode);

        fw.write(String.format(summaryPageFormat, listItems.toString()));
        fw.flush();

        return summaryFile;
    }

    /**
     * Updates the data base with the new summary file
     *
     * @param summaryFile the summary file that was already written to disk
     * @throws IOException    in case of File construction/writing error
     */
    public void updateDatabase(File summaryFile) throws IOException {
        JSONObject db = null;
        JSONArray results = null;
        boolean usingExistingDatabase = false;

        try {
            FileReader reader = new FileReader(this.database);
            db = (JSONObject) new JSONParser().parse(reader);
            results = (JSONArray) db.get("results");
            usingExistingDatabase = true;
        } catch (ParseException | FileNotFoundException e) {
            Logger.writeError("Current database is bad format, creating a new one instead.");
        } catch (IOException e) {
            //TODO: define behaviour
            e.printStackTrace();
        }

        if (!usingExistingDatabase) {
            db = new JSONObject();
            results = new JSONArray();
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

    public void updateLocalFiles(HashSet portScan) {
        if (portScan.isEmpty()) {
            this.openPorts = "not scanned";
        } else {
            ArrayList<Integer> portList = new ArrayList<>(portScan);
            Collections.sort(portList);
            this.openPorts = portList.toString();
        }

        File summaryPage = null;
        try {
            summaryPage = this.createSummaryFile();
            this.updateDatabase(summaryPage);
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: what happens when summary page cannot be created/written?
        }
    }

    public long getImages() {
        return this.images.get();
    }

    public long getVideos() {
        return this.videos.get();
    }

    public long getDocuments() {
        return this.documents.get();
    }

}
