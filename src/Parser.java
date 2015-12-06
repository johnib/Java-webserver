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
    private final static String bad_line = "Bad line on line %d:\t %s\n";
    private final Pattern regexPattern;
    /* Private fields */
    private Map<String, String> dict = new HashMap<>();

    /**
     * Constructs a new parser given a File object.
     *
     * @param configFile the File object of the config file.
     */
    public Parser(File configFile, String regex) {
        regexPattern = Pattern.compile(regex);

        this.dict = this.parse(configFile);
    }


    /**
     * Performs the parsing and creates a dictionary consisting of all key-value pairs processed
     * @param file the file to parse
     * @return a dictionary consisting of all key-value pairs.
     */
    private Map<String, String> parse(File file) {
        Map<String, String> dict = new HashMap<>();
        int lineCounter = 0;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                Matcher m = regexPattern.matcher(line);

                if (m.find()) {
                    dict.put(m.group(1), m.group(2));
                } else {
                    System.err.printf(bad_line, lineCounter, line);
                }

                lineCounter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
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
}
