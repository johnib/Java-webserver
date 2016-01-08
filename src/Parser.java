import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/6/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class Parser {

    /* Constants */
    protected final Pattern regexPattern;

    /**
     * Constructs a new parser given a File object.
     *
     * @param regex the regex string to be compiled
     */
    public Parser(String regex) {
        this.regexPattern = Pattern.compile(regex);
    }

    /**
     * Performs the parsing and creates a dictionary consisting of all key-value pairs processed
     *
     * @param text the string to be parsed.
     * @return a dictionary consisting of all key-value pairs.
     */
    public Map<String, String> parse(String text) {
        Map<String, String> dict = new HashMap<>();
        Matcher m = this.regexPattern.matcher(text);

        while (m.find()) {
            dict.put(m.group(1).toLowerCase(), m.group(2));
        }

        return dict;
    }
}