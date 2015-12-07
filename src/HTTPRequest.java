import java.util.Map;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/7/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class HTTPRequest {

    /* Constants */
    private final static String method = "METHOD";

    private final Map<String, String> dict;

    public HTTPRequest(Map<String, String> dict) {
        this.dict = dict;
    }

    public RequestType getMethod() {
        String value = dict.get(method);
        if (value == null || value.isEmpty()) {
            System.err.printf("Why null or empty?");
            return RequestType.Bad_Request;
        }

        RequestType result = searchEnum(value);
        if (result == null) {
            return RequestType.Not_supported;
        }

        return result;
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

}

