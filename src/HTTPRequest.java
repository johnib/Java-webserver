import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/7/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class HTTPRequest {
    /* Constants */
    private final static String connection_reset = "Thread-%d: Connection reset\n";
    private final static String method = "METHOD";

    /* Static */
    private static Parser parser = new HTTPParser();

    /* Fields */
    private Map<String, String> dict;
    private String fullRequest;
    private RequestType methodField = null;

    public HTTPRequest(Socket socket) {
        fullRequest = readRequest(socket);
        System.out.println(fullRequest);
        dict = parser.parse(fullRequest);
    }

    /* Public methods */
    public RequestType getMethod() {
        // Check if already parsed
        if (methodField != null) return methodField;

        // Checking if the method exist in the connection
        String value = dict.get(method);
        if (value == null || value.isEmpty()) {
            System.err.printf("Why null or empty?");
            methodField = RequestType.Bad_Request;
            return methodField;
        }

        // Trying to parse the method
        RequestType result = searchEnum(value);
        methodField = result;
        if (result == null) {
            methodField = RequestType.Not_Implemented;
        }

        return methodField;
    }

    /* Private methods */
    private String readRequest(Socket socket) {
        String requestLine = null;
        StringBuilder sb = new StringBuilder("");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            requestLine = reader.readLine() + Common.CRLF;

            String header;
            while (!(header = reader.readLine()).isEmpty()) {
                sb.append(header);
                sb.append(Common.CRLF);
            }

        } catch (IOException e) {
            //TODO: implement
            System.out.printf(connection_reset, Thread.currentThread().getId());
        }

        return requestLine + sb.toString();
    }

    private static RequestType searchEnum(String search) {
        if (search != null) {
            for (RequestType each : RequestType.class.getEnumConstants()) {
                if (each.name().compareToIgnoreCase(search) == 0) {
                    return each;
                }
            }
        } else {
            //TODO: beatify
            System.err.printf("Got bad search string in searchEnum\n");
        }

        return null;
    }

    public String getFullRequest() {
        return fullRequest;
    }
}

