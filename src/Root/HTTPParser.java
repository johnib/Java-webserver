package Root;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/7/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class HTTPParser extends Parser {
    /* Constants */
    private final static String default_http_regex = "(\\S+)\\s+([^?\\s]+|\\*)((?:[?&][^&\\s]+)*)\\s+HTTP\\/(.+)";
    private final static String default_header_regex = "(\\S+)\\:\\s+(.+)";
    private final static String default_payload_regex = "([^?=&]+)(=([^&]*))?";
    private Pattern regexHttp;
    private Pattern regexPayload;


    private HTTPParser(String regex) {
        super(regex);
    }

    public HTTPParser () {
        this(default_header_regex);
        this.regexHttp = Pattern.compile(default_http_regex);
        this.regexPayload = Pattern.compile(default_payload_regex, Pattern.MULTILINE);
    }

    @Override
    public Map<String, String> parse(String text) {
        Matcher headersMatcher = this.regexHttp.matcher(text);
        Map<String, String> dict = new HashMap<>();

        if (headersMatcher.find()) {
            // The * in the path is only relevant to the OPTIONS method
            if (headersMatcher.group(2).equals("*") &&
                    RequestType.OPTIONS.name().compareToIgnoreCase(headersMatcher.group(1)) != 0) {

                return dict;
            }

            dict.put(Common.http_parser_method, headersMatcher.group(1));
            dict.put(Common.http_parser_path, headersMatcher.group(2));
            dict.put(Common.http_parser_params, headersMatcher.group(3));
            dict.put(Common.http_parser_version, headersMatcher.group(4));
        }

        // adding all the headers data
        dict.putAll(super.parse(text));

        return dict;
    }

    public Map<String, String> parsePayload(String text) {
        Matcher payloadMatcher = this.regexPayload.matcher(text);
        Map<String, String> dict = new HashMap<>();

        // parsing payload if exists
        while (payloadMatcher.find()) {
            dict.put(payloadMatcher.group(1), payloadMatcher.group(3));
        }

        return dict;
    }
}
