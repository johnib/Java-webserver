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
    private final static String default_http_regex = "(\\S+)\\s+([^?\\s]+)((?:[?&][^&\\s]+)*)\\s+HTTP\\/(.+)";
    private final static String default_header_regex = "(\\S+)\\:\\s+(.+)";
    private final static String default_payload_regex = "(([[:alnum:]]+)=([[:alnum:]]))";
    private Pattern regexHttp;
    private Pattern regexPayload;


    private HTTPParser(String regex) {
        super(regex);
    }

    public HTTPParser () {
        this(default_header_regex);
        this.regexHttp = Pattern.compile(default_http_regex);
        this.regexPayload = Pattern.compile(default_payload_regex);
    }

    @Override
    public Map<String, String> parse(String text) {
        String firstLine = text.substring(0, text.indexOf(Common.CRLF));

        Matcher matcher = regexHttp.matcher(firstLine);
        Map<String, String> dict = new HashMap<>();

        if (matcher.find()) {
            dict.put(Common.http_parser_method, matcher.group(1));
            dict.put(Common.http_parser_path, matcher.group(2));
            dict.put(Common.http_parser_params, matcher.group(3));
            dict.put(Common.http_parser_version, matcher.group(4));
        } else {
            //TODO: method line could not be processed
            System.err.printf("Method line could not be processed.\n");
        }

        // Adding all the headers data
        dict.putAll(super.parse(text));

        // override url parameters if payload exists
        String[] payloadPart = text.split(Common.CRLF + Common.CRLF);
        if (payloadPart.length > 1) {
            dict.put(Common.http_parser_params, payloadPart[1]);
        }

        return dict;
    }
}
