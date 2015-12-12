import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/7/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class HTTPRequest {
    /* Constants */
    private final static String connection_reset = "Thread-%d: Connection reset\n";
    private final static String error_method_empty = "HTTPRequest.getMethod(): httpMethod is null or empty, printing dictionary\n%s\n";

    /* Static */
    private static HTTPParser parser = new HTTPParser();
    private static Pattern chunkedPattern = Pattern.compile(Pattern.quote("yes"), Pattern.CASE_INSENSITIVE);

    /* Fields */

    private Map<String, String> headersDict; // request parameters parsed
    private String headers; // request headers

    private Map<String, String> payloadDict; // POST parameters parsed
    private String payload; // POST parameters if exist

    private RequestType methodField = null;
    private String path = null;
    private Boolean isChunked = null;

    public HTTPRequest(Socket socket) {
        readRequest(socket);

        if (this.headers != null && !this.headers.isEmpty()) {
            this.headersDict = parser.parse(this.headers);
        } else {
            System.err.printf(error_method_empty, this.headersDict);
        }

        if (this.payload != null && !this.payload.isEmpty()) {
            this.payloadDict = parser.parsePayload(this.payload);
        }

        System.out.println("--Request--");
        System.out.println(this.headers);
    }

    /**
     * Given a string, searches for its Enum type.
     *
     * @param search the method string
     * @return the enum of type of the requested method string
     */
    private static RequestType searchEnum(String search) {
        if (search != null) {
            for (RequestType each : RequestType.class.getEnumConstants()) {
                if (each.name().compareToIgnoreCase(search) == 0) {
                    return each;
                }
            }
        } else {
            //TODO: dismiss before submission
            System.err.printf("Got bad search string in searchEnum\n");
        }

        return null;
    }

    /* Public methods */
    public RequestType getMethod() {
        // Check if already parsed
        if (methodField != null) return methodField;

        // Checking if the method exist in the connection
        String httpMethod = headersDict.get(Common.http_parser_method);
        if (httpMethod == null || httpMethod.isEmpty()) {
            System.err.printf(error_method_empty, this.headersDict.toString());
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

    public String getHeaders() {
        return headers;
    }

    public String getPath() {
        return this.headersDict.get(Common.http_parser_path);
    }

    /* Private methods */
    private String readRequest(Socket socket) {
        String requestLine = null;
        StringBuilder sb = new StringBuilder("");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            requestLine = reader.readLine() + Common.CRLF;
            sb.append(requestLine);

            int payloadLength = 0;
            String header;
            while (!(header = reader.readLine()).isEmpty()) {
                sb.append(header).append(Common.CRLF);

                // check if there's a content-length
                if (header.matches("[Cc]ontent-[Ll]ength: (\\d+)")) {
                    payloadLength = Integer.parseInt(header.split(": ")[1]);
                }
            }

            this.headers = sb.toString();

            // in case of POST parameters
            if (payloadLength > 0) {
                char[] buffer = new char[payloadLength];

                // read the payload to the array
                reader.read(buffer, 0, payloadLength);
                this.payload = java.net.URLDecoder.decode(new String(buffer).split(Common.CRLF)[0], "UTF-8");

                // append to the full request string
                sb.append(Common.CRLF).append(this.payload);
            }

        } catch (IOException e) {
            //TODO: implement
            System.out.printf(connection_reset, Thread.currentThread().getId());
        }


        return requestLine + sb.toString();
    }

    public String getHost() {
        return this.headersDict.get(Common.http_parser_host);
    }

    public boolean getIsChunked() {
        if (isChunked  != null) return isChunked;

        isChunked = this.headersDict.containsKey(Common.http_parser_chunked) &&
                chunkedPattern.matcher(this.headersDict.get(Common.http_parser_chunked)).find();
        return isChunked;
    }
}

