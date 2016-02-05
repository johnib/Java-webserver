package Tests;

import Root.HTTPRequest;
import Root.IConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 09/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class MoqConfig implements IConfiguration {
    /* private fields */
    private final static String regex_string = "(.+)=(.+)";
    private int port;
    private int maxThreads;
    private String root;
    private String defaultPage;
    private String rootAbsolutePath = null;
    private int maxDownloaders;
    private int maxAnalyzers;
    private HashSet<String> imageExtensions;
    private HashSet<String> videoExtensions;
    private HashSet<String> documentExtensions;


    @Override
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public int getMaxThreads() {
        return maxThreads;
    }

    @Override
    public HashMap getFileExtensions() {
        return null;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    @Override
    public String getRoot(HTTPRequest httpRequest) {
        return root;
    }

    @Override
    public String getRoot() {
        return this.root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    @Override
    public String getDefaultPage() {
        return defaultPage;
    }

    public void setDefaultPage(String defaultPage) {
        this.defaultPage = defaultPage;
    }

    @Override
    public IConfiguration getHostConfiguration(String hostName) {
        return this;
    }

    @Override
    public String getRootAbsolutePath(HTTPRequest httpRequest) throws IOException {
        return rootAbsolutePath;
    }

    @Override
    public int getMaxDownloaders() {
        return maxDownloaders;
    }

    public void setMaxDownloaders(int maxDownloaders) {
        this.maxDownloaders = maxDownloaders;
    }

    @Override
    public int getMaxAnalyzers() {
        return maxAnalyzers;
    }

    public void setMaxAnalyzers(int maxAnalyzers) {
        this.maxAnalyzers = maxAnalyzers;
    }

    public void setRootAbsolutePath(String rootAbsolutePath) {
        this.rootAbsolutePath = rootAbsolutePath;
    }

    @Override
    public HashSet<String> getImageExtensions() {
        return imageExtensions;
    }

    public void setImageExtensions(HashSet<String> imageExtensions) {
        this.imageExtensions = imageExtensions;
    }

    @Override
    public HashSet<String> getVideoExtensions() {
        return videoExtensions;
    }

    public void setVideoExtensions(HashSet<String> videoExtensions) {
        this.videoExtensions = videoExtensions;
    }

    @Override
    public HashSet<String> getDocumentExtensions() {
        return documentExtensions;
    }

    public void setDocumentExtensions(HashSet<String> documentExtensions) {
        this.documentExtensions = documentExtensions;
    }
}
