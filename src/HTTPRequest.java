import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Map;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/7/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class HTTPRequest {
    /* Constants */
    private final static String connection_reset = "Thread-%d: Connection reset\n";

    /* Static */
    private static Parser parser = new HTTPParser();

    /* Fields */
    private Map<String, String> dict;
    private String fullRequest;
    private RequestType methodField = null;
    private String path = null;

    public HTTPRequest(Socket socket) {
        try {
            fullRequest = java.net.URLDecoder.decode(readRequest(socket), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //TODO: basically there's nothing to do, this can't happen as the Encoding type is hardcoded.
            e.printStackTrace();
        }

        this.dict = parser.parse(this.fullRequest);

        System.out.println("--Request--");
        System.out.println(fullRequest);
    }

    /* Public methods */
    public RequestType getMethod() {
        // Check if already parsed
        if (methodField != null) return methodField;

        // Checking if the method exist in the connection
        String httpMethod = dict.get(Common.http_parser_method);
        if (httpMethod == null || httpMethod.isEmpty()) {
            //TODO: format strings
            System.err.printf("Why null or empty?");
            methodField = RequestType.Bad_Request;
            return methodField;
        }

        // Trying to parse the method
        RequestType result = searchEnum(httpMethod);
        methodField = result;
        if (result == null) {
            methodField = RequestType.Not_Implemented;
        }

        return methodField;
    }

    public String getFullRequest() {
        return fullRequest;
    }

    public String getPath() {
        return this.dict.get(Common.http_parser_path);
    }

    /* Private methods */
    private String readRequest(Socket socket) {
        String requestLine = null;
        StringBuilder sb = new StringBuilder("");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            requestLine = reader.readLine() + Common.CRLF;

            int payloadLength = 0;
            String header;
            while (!(header = reader.readLine()).isEmpty()) {
                sb.append(header).append(Common.CRLF);

                // check if there's a content-length
                if (header.matches("[Cc]ontent-[Ll]ength: (\\d+)")) {
                    payloadLength = Integer.parseInt(header.split(": ")[1]);
                }
            }

            if (payloadLength > 0) {
                char[] content = new char[payloadLength];
                reader.read(content, 0, payloadLength);
                sb.append(Common.CRLF).append(new String(content));
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

    public String getHost() {
        return this.dict.get(Common.http_parser_host);
    }
}

