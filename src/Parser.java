import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jonathan Yaniv on 03/12/2015.
 * Copyright (c) 2015 Jonathan Yaniv. All rights reserved.
 */
public class Parser {

    /* Constants */
    private final static String bad_line = "Bad config line on line %d:\t %s\n";

    /* Private fields */
    private final Map<String, String> dict = new HashMap<>();

    /**
     * Constructs a new parser given a File object.
     *
     * @param configFile the File object of the config file.
     */
    public Parser(File configFile) {
        Pattern p = Pattern.compile("(.+)=(.+)");
        int count = 0;
        try {
            BufferedReader configFileReader = new BufferedReader(new FileReader(configFile));

            String line;
            while ((line = configFileReader.readLine()) != null) {
                Matcher m = p.matcher(line);

                if (m.find()) {
                    dict.put(m.group(1), m.group(2));
                } else {
                    System.err.printf(bad_line, count, line);
                }
                count++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
