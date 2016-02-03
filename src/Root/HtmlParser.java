package Root;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class HTMLParser extends Parser {

    private static final String[] capture_groups = new String[]{"url", "uri"};
    private static final String html_regex = "(src|href)[\\s]?=[\\s]?[\\\"\\']((?<url>http[s]?:\\/\\/\\S+)|(?<uri>[^\\s\\'\\\"\\#]*))\\#\\S+[\\\"\\']";

    /**
     * Constructs a new parser given a File object.
     *
     * @param regex the regex string to be compiled
     */
    public HTMLParser(String regex) {
        super(regex);
    }

    public HTMLParser() {
        this(html_regex);
    }

    /**
     * Creates a dictionary of key-value pairs holding the URL / URI found in the html.
     *
     * @param html the HTML as a string
     * @return unique value dictionary - no two equal values exist in the returned dictionary.
     */
    @Override
    public Map<String, String> parse(String html) {
        Map<String, String> dict = new HashMap<>();
        Matcher m = super.regexPattern.matcher(html);
        int counter = 0;

        while (m.find()) {
            for (String groupName : capture_groups) {
                if (m.group(groupName) != null &&
                        !m.group(groupName).isEmpty() &&
                        !dict.containsValue(m.group(groupName))) {

                    dict.put(groupName + counter, m.group(groupName));
                    counter++;
                }
            }
        }

        return dict;
    }
}
