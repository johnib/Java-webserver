import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/7/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class HTTPParser extends Parser {

    /* Constants */
    private static final String default_method_regex = "(\\S+)\\s+([^?\\s]+)((?:[?&][^&\\s]+)*)\\s+HTTP\\/(.+)|(\\S+)\\:\\s+(.+)";

    /**
     * Constructs a new parser given a File object.
     *
     * @param regex the regex string to be compiled
     */
    private HTTPParser(String regex) {
        super(regex);
    }

    public HTTPParser() {
        this(default_method_regex);
    }

    @Override
    public Map<String, String> parse(String text) {
        Matcher m = super.regexPattern.matcher(text);

        if (m.find()) {
            super.dict.put("METHOD", m.group(1));
            super.dict.put("FILE_PATH", m.group(2));
            super.dict.put("PARAMS", m.group(3));
            super.dict.put("VERSION", m.group(4));
        } else {
            //TODO: method line could not be processed
            System.out.printf("");
        }

        while (m.find()) {
            super.dict.put(m.group(5), m.group(6));
        }

        return super.dict;
    }
}
