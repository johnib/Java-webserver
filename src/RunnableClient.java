import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

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
    private final static String empty_request = "The client request was empty\n";

    // response constants
    private static final String CRLF = "\r\n";
    private final static String statusLine = "HTTP/1.1 %1d %2s" + CRLF + "Date: %3s" + CRLF;
    private final static String generalHeaders = "Connection: close" + CRLF;
    private final static String responseHeaders = "Server: ShekerKolshoServer/1.0" + CRLF;
    private final static String entityHeaders = "Last-Modified: %1s" + CRLF +
            "Content-Length: %2d" + CRLF +
            "Content-Type: %3s" + CRLF;


    /* private fields */
    private final Socket socket;
    private Configuration config;

    /**
     * Creates a Runnable wrapper for the given socket.
     *
     * @param clientSocket the socket
     * @param config The server configuration
     */
    public RunnableClient(Socket clientSocket, Configuration config) {
        this.socket = clientSocket;
        this.config = config;
    }

    @Override
    public void run() {
        //TODO: implement client-request-response lifecycle

        // The client closed the connection
        if (socket == null || socket.isClosed()) {
            System.out.println(closed_by_the_client);
            return;
        }

        //TODO: after the parser dictionary is ready - change
        HTTPRequest httpRequest = new HTTPRequest(this.socket);

        switch (httpRequest.getMethod()) {
            case GET:
                this.sendGetResponse(httpRequest);
                break;
            case POST:
                break;
            case TRACE:
                this.sendTraceResponse(httpRequest);
                break;
            case HEAD:
                //TODO: send only headers, not body
                break;
            case Not_Implemented:
                this.sendResponseNotImplemented();
                break;
            case Bad_Request:
            default:
                this.sendResponseBadRequest();
                break;
        }

        this.close();
    }

    private void sendGetResponse(HTTPRequest httpRequest) {
        String path = httpRequest.getPath();
        File file = new File(config.getRoot(), path);

        // First, make sure the path exists
        if (!file.exists()) {
            // return File Not Found (Even if it's a directory)
            sendFileNotFound();
            return;
        }

        // If it is a directory take the default file
        if (file.isDirectory()) {
            file = new File(file.getAbsolutePath(), config.getDefaultPage());

            // In case the default file was not present
            if (!file.exists()) {
                sendFileNotFound();
                return;
            }
        }

        // Check the file type
        int extIndex = file.getName().lastIndexOf('.');
        if (extIndex > 0) {
            String ext = file.getName().substring(extIndex);
            if (".bmp, .gif, .png, .jpg".contains(ext)) {
                sendGetImageFile(file, httpRequest, config);
            } else if (ext.endsWith(".ico")) {
                sendIconFile(file, httpRequest, config);
            }
        }

        // This is the default behavior
        sendTextFile(file, httpRequest, config);
    }

    private void sendTextFile(File file, HTTPRequest httpRequest, Configuration config) {
        throw new NotImplementedException();
    }

    private void sendIconFile(File file, HTTPRequest httpRequest, Configuration config) {
        throw new NotImplementedException();
    }

    private void sendGetImageFile(File file, HTTPRequest httpRequest, Configuration config) {
        throw new NotImplementedException();
    }

    private void sendFileNotFound() {
        sendResponse(CreateResponseHeaders(404, new Date().toString(), 0, "text/html"), new byte[0]);
    }

    private void sendTraceResponse(HTTPRequest httpRequest) {
        String value = httpRequest.getFullRequest();

        sendResponse(
                CreateResponseHeaders(200, new Date().toString(), value.getBytes(StandardCharsets.US_ASCII).length, "message/http"),
                value);
    }

    private void sendResponseNotImplemented() {
        sendResponse(CreateResponseHeaders(501, new Date().toString(), 0, "text/html"), new byte[0]);
    }

    private void sendResponseBadRequest() {
        sendResponse(CreateResponseHeaders(400, new Date().toString(), 0, "text/html"), new byte[0]);
    }

    private void sendResponse(String responseHeaders, String responseBody) {
        sendResponse(responseHeaders, responseBody.getBytes(StandardCharsets.US_ASCII));
    }

    private void sendResponse(String responseHeaders, byte[] responseBody) {
        try {
            DataOutputStream outToClient = new DataOutputStream(this.socket.getOutputStream());

            // output server opening message
            byte[] headersBytes = responseHeaders.getBytes(StandardCharsets.US_ASCII);
            outToClient.write(headersBytes);
            outToClient.write(responseBody);
            outToClient.write(Common.CRLF_BYTES);
            outToClient.flush();

        } catch (IOException e) {
            //TODO: implement
            e.printStackTrace();
        }
    }

    //TODO: implement a dictionary to contain all status codes and their corresponding keywords.
    private String CreateResponseHeaders(int statusCode, String lastModified, int contentLength, String contentType) {

        Date UtcNow = new Date();
        StringBuilder sb = new StringBuilder();

        // Send all output to the appendable object sb
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format(statusLine, statusCode, Common.getHttpStatusName(statusCode), UtcNow.toString());
        formatter.format(generalHeaders);
        formatter.format(responseHeaders);
        formatter.format(entityHeaders, lastModified, contentLength, contentType);


        return sb.toString();
    }

    /**
     * Do all necessary operations before stopping to handle this client.
     */
    public void close() {
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                //TODO: implement
            }

        }
    }
}
