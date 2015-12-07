import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    /* Private fields */
    protected Map<String, String> dict = new HashMap<>();

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
        Matcher m = regexPattern.matcher(text);

        while (m.find()) {
            dict.put(m.group(1), m.group(2));
        }

        return dict;
    }

    /**
     * Returns the value for the given key.
     *
     * @param key the key
     * @return the value of the given key.
     */
    public String getValue(String key) {
        return this.dict.get(key);
    }

    @Override
    public String toString() {
        return this.dict.toString();
    }

    public Map<String, String> getDictionary() {
        return dict;
    }
}