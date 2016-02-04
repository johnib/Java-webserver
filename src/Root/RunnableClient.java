package Root;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/6/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */

/**
 * This class is a Runnable wrapper for the client socket, this way it can be processed by other threads.
 */
public class RunnableClient implements Runnable {
    /* Constants */
    private final static String closed_by_the_client = "Connection was forced closed by the client\n";
    private final static byte[] parameter_response_body_up = "<html><body>".getBytes(StandardCharsets.US_ASCII);
    private static final String crawlPath = "/crawl"; //TODO: can be changed to execResult.html later
    private static final String crawlHistoryPath = "/get-history";
    private static final String dbFileName = "db.json";
    private static final String resultsDir = "results";
    // TODO: this value...
    private static final String crawlUrl = "crawlUrl";

    /* private fields */
    private final Socket socket;
    private IConfiguration config;
    private HTTPRequest httpRequest;
    private boolean wasErrorSent = false;
    private File crawlerDataBaseFilePath;


    /**
     * Creates a Runnable wrapper for the given socket.
     *
     * @param clientSocket the socket
     * @param config       The server configuration
     */
    public RunnableClient(Socket clientSocket, IConfiguration config) {
        this.socket = clientSocket;
        this.config = config;
        this.crawlerDataBaseFilePath = new File(config.getRoot() + File.separator + dbFileName);
    }

    @Override
    public void run() {
        // The client closed the connection
        if (socket == null || socket.isClosed()) {
            System.out.println(closed_by_the_client);
            return;
        }

        try {
            this.httpRequest = new HTTPRequest(this.socket);

            // Checking for bad request
            if (httpRequest.getPath() == null || httpRequest.getMethod() == null) {
                System.out.println("No path or method found");
                this.wasErrorSent = true;
                sendResponseBadRequest();
                return;
            }

        } catch (Exception ex) {
            System.out.println("Problem parsing the data");
            this.wasErrorSent = true;
            sendResponseBadRequest();
            return;
        }

        try {
            switch (httpRequest.getMethod()) {
                case GET:
                    this.sendGetResponse();
                    break;
                case POST:
                    this.sendGetResponse();
                    break;
                case TRACE:
                    this.sendTraceResponse();
                    break;
                case HEAD:
                    this.sendHeadResponse();
                    break;
                case OPTIONS:
                    this.sendOptionsResponse();
                    break;
                case Not_Implemented:
                    this.sendResponseNotImplemented();
                    break;
                case Bad_Request:
                default:
                    this.sendResponseBadRequest();
                    break;
            }

        } catch (Exception e) {
            if (!this.wasErrorSent) {
                this.wasErrorSent = true;
                sendInternalServerError();
            }
        } finally {
            this.close();
        }
    }

    private void sendOptionsResponse() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            byte[] headers;
            if (this.httpRequest.getPath().equals("*")) {
                headers = new HTTPResponse((byte[]) null, 200, null, this.httpRequest, this.config).getHeaders();
            } else {
                headers = this.parseGetResponse(this.httpRequest).getHeaders();
            }

            outputStream.write(headers);

            System.out.println("-- Response Headers --");
            System.out.println(new String(headers));

            outputStream.write("Allow:".getBytes(StandardCharsets.US_ASCII));
            boolean isFirst = true;

            for (RequestType each : RequestType.class.getEnumConstants()) {
                if (each != RequestType.Bad_Request && each != RequestType.Not_Implemented) {
                    // comma separated values
                    if (!isFirst) {
                        outputStream.write(",".getBytes(StandardCharsets.US_ASCII));
                    }

                    outputStream.write(each.name().getBytes(StandardCharsets.US_ASCII));
                    isFirst = false;
                }
            }

            outputStream.write(Common.CRLF_BYTES);
            outputStream.write(Common.CRLF_BYTES);
        } catch (IOException e) {
            this.wasErrorSent = true;
            sendInternalServerError();
        }

        sendResponse(outputStream.toByteArray());

        // Closing the stream
        try {
            outputStream.close();
        } catch (IOException ignored) {
        }
    }

    private void sendHeadResponse() {
        byte[] headers = this.parseGetResponse(this.httpRequest).getHeaders();
        this.sendResponse(headers);

        System.out.println("-- Response Headers --");
        System.out.println(new String(headers));
    }

    private void sendGetResponse() {
        HTTPResponse response = this.parseGetResponse(httpRequest);
        this.sendResponse(response);
    }

    private HTTPResponse parseGetResponse(HTTPRequest httpRequest) {
        String path = httpRequest.getPath();

        switch (path) {
            case crawlPath:
                //TODO: define behaviour for crawler
                Logger.writeError("crawl path");
                if (Crawler.getInstance().isWorking()) {
                    Logger.writeInfo("The crawler is currently working");
                    return getResponseJsonCrawlerBusy();
                }

                Crawler.getInstance().pushDownloadUrlTask(new RunnableDownloader(URL.makeURL(httpRequest.getJsonParam(crawlUrl))));
                CrawlerResult result = Crawler.getInstance().getResult();

                return getResponseJson(result, "text/json");

                //TODO: when crawler is done, use case crawlHistoryPath to retrieve the new db.
            case crawlHistoryPath:
                //TODO: define behaviour to extract history crawling
                System.err.println("crawl history path");
                return this.getResponseFile(this.crawlerDataBaseFilePath, "application/json; charset=utf-8");
            default:
                // normal web server behaviour

                File file = new File(config.getRoot(httpRequest), path);

                // keep the path secured under the wwwroot/ folder
                if (isPathTraversalAttack(file, httpRequest, config)) {
                    return getResponseBadRequest();
                }

//                // allows requests inside /wwwroot/Results only when Referer header is the server.
//                try {
//                    if (file.getCanonicalFile().toString().startsWith(config.getRoot() + File.separator + resultsDir)) {
//                        String referer = httpRequest.getReferer();
//                        if (referer == null
//                                || (!referer.contains("localhost")
//                                && !referer.contains("127.0.0.1"))) {
//
//                            return getResponseForbidden();
//                        }
//                    }
//                } catch (IOException e) {
//                    return getResponseForbidden();
//                }

                // First, make sure the path exists
                if (!file.exists()) {
                    // return File Not Found (Even if it's a directory)
                    return getResponseFileNotFound();
                }

                // If it is a directory take the default file
                if (file.isDirectory()) {
                    file = new File(file.getAbsolutePath(), config.getDefaultPage());

                    // In case the default file was not present
                    if (!file.exists()) {
                        return getResponseFileNotFound();
                    }
                }

                // Check the file type
                int extIndex = file.getName().lastIndexOf('.');
                if (extIndex > 0) {
                    String ext = file.getName().substring(extIndex);
                    if (".bmp, .gif, .png, .jpg".contains(ext)) {
                        return getResponseFile(file, "image");
                    } else if (ext.endsWith(".ico")) {
                        return getResponseFile(file, "icon");
                    } else if (".html, .htm, .php, .js".contains(ext)) {
                        return getResponseFile(file, "text/html");
                    }
                }

                // This is the default behavior
                return getResponseFile(file, "application/octet-stream");
        }
    }

    private HTTPResponse getResponseJsonCrawlerBusy() {
        // TODO: this method
        throw new NotImplementedException();
    }

    private HTTPResponse getResponseJson(CrawlerResult result, String s) {
        // TODO: this method
        throw new NotImplementedException();
    }

    private boolean isPathTraversalAttack(File file, HTTPRequest httpRequest, IConfiguration config) {
        try {
            // Get the default path of the web site
            String root = config.getRootAbsolutePath(httpRequest);

            // Returns true if the file path is not in the right subdirectory
            return !file.getCanonicalPath().startsWith(root);
        } catch (IOException e) {
            System.err.println("Error in isPathTraversalAttack");
        }

        // Security error or IOError
        return true;
    }


    private HTTPResponse getResponseFile(File file, String contentType) {
        return new HTTPResponse(file, 200, contentType, this.httpRequest, this.config);
    }

    private void sendInternalServerError() {
        sendResponse(getResponseInternalServerError());
    }

    private HTTPResponse getResponseInternalServerError() {
        return new HTTPResponse((File) null, 500, "text/html", httpRequest, config);
    }


    private HTTPResponse getResponseFileNotFound() {
        return new HTTPResponse((File) null, 404, "text/html", httpRequest, config);
    }

    private HTTPResponse getResponseForbidden() {
        return new HTTPResponse((File) null, 403, "text/html", httpRequest, config);
    }

    private void sendTraceResponse() {
        String value = httpRequest.getHeaders();
        sendResponse(getResponseTrace(value));
    }

    private HTTPResponse getResponseTrace(String value) {
        return new HTTPResponse(value.getBytes(StandardCharsets.US_ASCII), 200, "message/http", httpRequest, config);
    }

    private void sendResponseNotImplemented() {
        sendResponse(getResponseNotImplemented());
    }

    private HTTPResponse getResponseNotImplemented() {
        return new HTTPResponse((File) null, 501, null, httpRequest, config);
    }

    private void sendResponseBadRequest() {
        sendResponse(getResponseBadRequest());
    }

    public HTTPResponse getResponseBadRequest() {
        return new HTTPResponse((File) null, 400, "text/html", httpRequest, config);
    }

    private void sendResponse(HTTPResponse response) {
        try {
            if (this.socket == null || this.socket.isClosed()) return;
            DataOutputStream outToClient = new DataOutputStream(this.socket.getOutputStream());

            if (httpRequest.getIsChunked()) {
                response.sendResponse(outToClient);
            } else {
                response.sendResponse(outToClient, null);
            }

            outToClient.flush();
            outToClient.close();

        } catch (IOException e) {
            handleSendException();
        }
    }

    private void sendResponse(byte[] response) {
        try {
            if (this.socket == null || this.socket.isClosed()) return;
            DataOutputStream outToClient = new DataOutputStream(this.socket.getOutputStream());

            // output server opening message
            outToClient.write(response);
            outToClient.flush();
            outToClient.close();
        } catch (IOException e) {
            handleSendException();
        }
    }

    private void handleSendException() {
        // In case there is a problem sending the error response
        // do nothing
        if (!this.wasErrorSent) {
            // This should change the response code to 500 (internal error)
            // and send another html file for this error.
            // and continue the process as if nothing happened.
            this.wasErrorSent = true;
            sendInternalServerError();
        }
    }

    /**
     * Do all necessary operations before stopping to handle this client.
     */
    public void close() {
        if (!socket.isClosed()) {
            try {
                System.out.println("Closing socket");
                socket.close();
            } catch (IOException e) {
                //TODO: implement
            }
        }
    }
}
