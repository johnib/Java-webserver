import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private final static String connection_reset = "Thread-%d: Connection reset\n";

    // response constants
    private final static String CRLF = "\r\n";
    private final static String statusLine = "HTTP/1.1 %1d %2s" + CRLF + "Date: %3s" + CRLF;
    private final static String generalHeaders = "Connection: close" + CRLF;
    private final static String responseHeaders = "Server: ShekerServer/1.0" + CRLF;
    private final static String entityHeaders = "Last-Modified: %1s" + CRLF +
            "Content-Length: %2d" + CRLF +
            "Content-Type: %3s" + CRLF;


    /* private fields */
    private final Socket socket;

    /**
     * Creates a Runnable wrapper for the given socket.
     *
     * @param clientSocket the socket
     */
    public RunnableClient(Socket clientSocket) {
        this.socket = clientSocket;
    }

    @Override
    public void run() {
        //TODO: implement client-request-response lifecycle

        // The client closed the connection
        if (socket == null || socket.isClosed()) {
            System.out.println(closed_by_the_client);
            return;
        }

        /* read the request from client and print it */
        String requestString = this.readRequest();
        System.out.println(requestString);

        Parser parser = new HTTPParser();
        parser.parse(requestString);

        //TODO: after the parser dictionary is ready - change
        HTTPRequest htreq = new HTTPRequest(parser.getDictionary());

        switch (htreq.getMethod()) {
            case GET:
                this.sendResponse("hello");
                break;
            case POST:
                break;
            case TRACE:
                //TODO: echo the http request back to client
                break;
            case HEAD:
                //TODO: send only headers, not body
                break;
            case Not_supported:
            default:
                sendResponseBadRequest();
                break;
        }

        this.close();
    }

    private void sendResponseBadRequest() {
        sendResponse(CreateResponseHeaders(400, "Bad Request", new Date().toString(), 0, "text/html"));
    }

    private void sendResponse(String response) {
        try {
            DataOutputStream outToClient = new DataOutputStream(this.socket.getOutputStream());

            // output server opening message
            String s = response + CRLF;
            byte[] b = s.getBytes(StandardCharsets.US_ASCII);
            outToClient.write(b);
            outToClient.flush();

        } catch (IOException e) {
            //TODO: implement
            e.printStackTrace();
        }
    }

    //TODO: implement a dictionary to contain all status codes and their corresponding keywords.
    private String CreateResponseHeaders(int statusCode, String statusCodeKeyword, String lastModified, int contentLength, String contentType) {

        Date UtcNow = new Date();
        StringBuilder sb = new StringBuilder();

        // Send all output to the Appendable object sb
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format(statusLine, statusCode, statusCodeKeyword, UtcNow.toString());
        formatter.format(generalHeaders);
        formatter.format(responseHeaders);
        formatter.format(entityHeaders, lastModified, contentLength, contentType);


        return sb.toString();
    }

//    private RequestType getRequestType(String requestString) {
//        String[] lines = requestString.split(CRLF);
//        if (lines.length == 0) {
//            System.out.println(empty_request);
//            return RequestType.Not_supported;
//        }
//
//        String[] parameters = lines[0].split(" ");
//        if (parameters.length == 0) {
//            System.out.println(empty_request);
//            return RequestType.Not_supported;
//        }
//
//        RequestType result = searchEnum(parameters[0]);
//        if (result == null) return RequestType.Not_supported;
//        return result;
//    }

    public String readRequest() {
        String requestLine = null;
        StringBuilder sb = new StringBuilder("");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            requestLine = reader.readLine() + CRLF;

            String header;
            while (!(header = reader.readLine()).isEmpty()) {
                sb.append(header);
                sb.append(CRLF);
            }

        } catch (IOException e) {
            //TODO: implement
            System.out.printf(connection_reset, Thread.currentThread().getId());
        }

        return requestLine + sb.toString();
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
