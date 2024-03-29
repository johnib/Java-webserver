package Root;

import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
    private static final String crawlPath = "/crawl";
    private static final String crawlHistoryPath = "/get-history";
    private static final String dbFileName = "db.json";
    private static final String resultsDir = "results";

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
            Logger.writeWebServerLog(closed_by_the_client);
            return;
        }

        try {
            this.httpRequest = new HTTPRequest(this.socket);

            // Checking for bad request
            if (httpRequest.getPath() == null || httpRequest.getMethod() == null) {
                Logger.writeWebServerLog("No path or method found");
                this.wasErrorSent = true;
                sendResponseBadRequest();
                return;
            }

        } catch (Exception ex) {
            Logger.writeWebServerLog("Problem parsing the data");
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
                headers = new HttpResponse((byte[]) null, 200, null, this.httpRequest, this.config).getHeaders();
            } else {
                headers = this.parseGetResponse(this.httpRequest).getHeaders();
            }

            outputStream.write(headers);

            Logger.writeWebServerLog("-- Response Headers --");
            Logger.writeWebServerLog(new String(headers));

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

        Logger.writeWebServerLog("-- Response Headers --");
        Logger.writeWebServerLog(new String(headers));
    }

    private void sendGetResponse() {
        HttpResponse response = this.parseGetResponse(httpRequest);
        this.sendResponse(response);
    }

    private HttpResponse parseGetResponse(HTTPRequest httpRequest) {
        String path = httpRequest.getPath();

        switch (path) {
            case crawlPath:
                Logger.writeError("crawl path");
                if (Crawler.getInstance().isWorking()) {
                    Logger.writeInfo("The crawler is currently working");
                    return getResponseInternalServerError();
                }

                JSONObject crawlerConfig;
                try {
                    crawlerConfig = (JSONObject) new JSONParser().parse(this.httpRequest.getPayload());
                } catch (ParseException e) {
                    Logger.writeError("Received non-parsable JSON from client");
                    return getResponseBadRequest();
                }

                CrawlerResult result = Crawler.getInstance().crawl(crawlerConfig);

                String phoneNumber = crawlerConfig.get("phone").toString();
                if (!phoneNumber.isEmpty() && result != null) {
                    SmsSender.sendSms(phoneNumber, result);
                }

            case crawlHistoryPath:
                System.err.println("crawl history path");

                return this.getResponseFile(this.crawlerDataBaseFilePath, "application/json; charset=utf-8");

            default:
                // normal web server behaviour

                File file = new File(config.getRoot(httpRequest), path);

                // keep the path secured under the wwwroot/ folder
                if (isPathTraversalAttack(file, httpRequest, config)) {
                    return getResponseBadRequest();
                }

                // allows requests inside /wwwroot/Results only when Referer header is the server.
                try {
                    if (file.getCanonicalFile().toString().startsWith(config.getRoot() + File.separator + resultsDir)) {
                        String referer = httpRequest.getReferer();
                        if (referer == null
                                || (!referer.contains("localhost")
                                && !referer.contains("127.0.0.1"))) {

                            return getResponseForbidden();
                        }
                    }
                } catch (IOException e) {
                    return getResponseForbidden();
                }

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


    private HttpResponse getResponseFile(File file, String contentType) {
        return new HttpResponse(file, 200, contentType, this.httpRequest, this.config);
    }

    private void sendInternalServerError() {
        sendResponse(getResponseInternalServerError());
    }

    private HttpResponse getResponseInternalServerError() {
        return new HttpResponse((File) null, 500, "text/html", httpRequest, config);
    }


    private HttpResponse getResponseFileNotFound() {
        return new HttpResponse((File) null, 404, "text/html", httpRequest, config);
    }

    private HttpResponse getResponseForbidden() {
        return new HttpResponse((File) null, 403, "text/html", httpRequest, config);
    }

    private void sendTraceResponse() {
        String value = httpRequest.getHeaders();
        sendResponse(getResponseTrace(value));
    }

    private HttpResponse getResponseTrace(String value) {
        return new HttpResponse(value.getBytes(StandardCharsets.US_ASCII), 200, "message/http", httpRequest, config);
    }

    private void sendResponseNotImplemented() {
        sendResponse(getResponseNotImplemented());
    }

    private HttpResponse getResponseNotImplemented() {
        return new HttpResponse((File) null, 501, null, httpRequest, config);
    }

    private void sendResponseBadRequest() {
        sendResponse(getResponseBadRequest());
    }

    public HttpResponse getResponseBadRequest() {
        return new HttpResponse((File) null, 400, "text/html", httpRequest, config);
    }

    private void sendResponse(HttpResponse response) {
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
                Logger.writeWebServerLog("Closing socket");
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
