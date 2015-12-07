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
    private static final String default_method_regex = "(\\S+)\\s+([^?\\s]+)((?:[?&][^&\\s]+)*)\\s+HTTP\\/(.+)|(\\S+)\\:\\s+(.+)";

    private HTTPParser(String regex) {
        super(regex);
    }

    public HTTPParser () {
        this(default_method_regex);
    }

    @Override
    public Map<String, String> parse(String text) {
        Matcher m = super.regexPattern.matcher(text);
        Map<String, String> dict = new HashMap<>();

        if (m.find()) {
            dict.put("METHOD", m.group(1));
            dict.put("FILE_PATH", m.group(2));
            dict.put("PARAMS", m.group(3));
            dict.put("VERSION", m.group(4));
        } else {
            //TODO: method line could not be processed
            System.out.printf("");
        }

        while (m.find()) {
            dict.put(m.group(5), m.group(6));
        }

        return dict;
    }
}
