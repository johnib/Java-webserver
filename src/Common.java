import java.util.HashMap;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/7/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class Common {
    private static HashMap<Integer, String> httpStatuses = new HashMap<Integer, String>() {
        {
            put(200, "OK");
            put(404, "Not Found");
            put(501, "Not Implemented");
            put(400, "Bad Request");
            put(500, "Internal Server Error");
        }
    };

    public static final String CRLF = "\r\n";
    public static final byte[] CRLF_BYTES =  new byte[]{0x0d, 0x0a};
    public static final String http_parser_method = "METHOD";
    public static final String http_parser_path = "FILE_PATH";
    public static final String http_parser_params = "PARAMS";
    public static final String http_parser_version = "VERSION";


    public static String getHttpStatusName(int statusCode) {
        return httpStatuses.get(statusCode);
    }
}
