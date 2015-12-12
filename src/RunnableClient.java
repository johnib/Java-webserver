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
    private final static String empty_request = "The client request was empty\n";

    // response constants

    private static final String CRLF = Common.CRLF;

    /* private fields */
    private final Socket socket;
    private Configuration config;
    private HTTPRequest httpRequest;
    private boolean isErrorOccurred = false;

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

        this.httpRequest = new HTTPRequest(this.socket);

        switch (httpRequest.getMethod()) {
            case GET:
                this.sendGetResponse();
                break;
            case POST:
                break;
            case TRACE:
                //TODO: test
                this.sendTraceResponse();
                break;
            case HEAD:
                //TODO: send only headers, not body
                //TODO: test
                this.sendHead();
                break;
            case OPTIONS:
                //TODO: implement
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

    private void sendHead() {
        sendResponse(parseGetResponse(this.httpRequest).getHeaders());
    }

    private void sendGetResponse() {
        HttpResponse response = parseGetResponse(httpRequest);
        sendResponse(response);
    }

    private HttpResponse parseGetResponse(HTTPRequest httpRequest) {
        String path = httpRequest.getPath();
        File file = new File(config.getRoot(httpRequest), path);

        // keep the path secured under the wwwroot/ folder
        if (isPathTraversalAttack(file, httpRequest, config)) {
            return getResponseBadRequest();
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
                return getResponseImageFile(file);
            } else if (ext.endsWith(".ico")) {
                return getResponseIconFile(file);
            } else if (".html, .htm, .php, .js".contains(ext)) {
                return getResponseTextFile(file);
            }
        }

        // This is the default behavior
        return getResponseGeneralFile(file);
    }

    private boolean isPathTraversalAttack(File file, HTTPRequest httpRequest, Configuration config) {
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

    private HttpResponse getResponseGeneralFile(File file) {
        return getResponseFile(file, "application/octet-stream");
    }

    private HttpResponse getResponseTextFile(File file) {
        return getResponseFile(file, "text/html");
    }

    private HttpResponse getResponseIconFile(File file) {
        return getResponseFile(file, "icon");
    }

    private HttpResponse getResponseImageFile(File file) {
        return getResponseFile(file, "image");
    }

    private HttpResponse getResponseFile(File file, String contentType) {
        return new HttpResponse(file, 200, contentType, this.httpRequest, this.config);
    }

    private void sendInternalServerError() {
        throw new UnsupportedOperationException("Not implemented yet");
    }


    private void sendFileNotFound() {
        sendResponse(getResponseFileNotFound());
    }

    private HttpResponse getResponseFileNotFound() {
        return new HttpResponse((File) null, 404, "text/html", httpRequest, config);
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
            sendResponse(response.CreateResponse());
        } catch (IOException e) {
            if (!isErrorOccurred) {
                isErrorOccurred = true;
                //TODO: this should change the response code to 501 (internal error)
                //TODO: and send another html file for this error.
                //TODO: and continue the process as if nothing happened.
                sendInternalServerError();
            }
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

            System.out.println("--Response--");
            System.out.println(new String(response, StandardCharsets.US_ASCII));
            System.out.println();

        } catch (IOException e) {
            //TODO: implement
            e.printStackTrace();
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
