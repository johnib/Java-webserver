package Root;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/7/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class Common {
    public static final String CRLF = "\r\n";
    public static final byte[] CRLF_BYTES = new byte[]{0x0d, 0x0a};
    public static final String http_parser_method = "METHOD";
    public static final String http_parser_path = "FILE_PATH";
    public static final String http_parser_params = "PARAMS";
    public static final String http_parser_version = "VERSION";
    public static final String http_parser_host = "HOST";
    public static final String http_parser_chunked = "chunked";
    private static HashMap<Integer, String> httpStatuses = new HashMap<Integer, String>() {
        {
            put(200, "OK");
            put(404, "Not Found");
            put(400, "Bad Request");
            put(500, "Internal Root.Server Error");
            put(501, "Not Implemented");
        }
    };

    public static String getHttpStatusName(int statusCode) {
        return httpStatuses.get(statusCode);
    }

    public static String ConvertLongToTimeString(long lastModified) {
        return toISO2616DateFormat(new Date(lastModified));
    }

    public static String toISO2616DateFormat(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(date);
    }
}
