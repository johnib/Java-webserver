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
    private static String default_http_regex = "(\\S+)\\s+([^?\\s]+)((?:[?&][^&\\s]+)*)\\s+HTTP\\/(.+)";
    private static String default_header_regex = "(\\S+)\\:\\s+(.+)";
    private Pattern regexHttp;


    private HTTPParser(String regex) {
        super(regex);
    }

    public HTTPParser () {
        this(default_header_regex);
        regexHttp = Pattern.compile(default_http_regex);
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
            System.out.printf("");
        }

        // Adding all the headers data
        dict.putAll(super.parse(text));
        return dict;
    }
}
