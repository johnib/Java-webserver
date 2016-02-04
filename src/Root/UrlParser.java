package Root;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class URLParser extends Parser {

    private static final String[] capture_groups = new String[]{"protocol", "domain", "uri", "port"};
    private static final String url_regex = "((?<protocol>http):\\/\\/)?[.*\\@]?(www\\.|.*@)?(?<domain>[\\w\\-]+\\.[\\w\\-]+[\\.[\\w\\-]+]*)(?<port>\\:\\d+)?(?<uri>\\S*)";

    public URLParser() {
        this(url_regex);
    }

    public URLParser(String regex) {
        super(regex);
    }

    @Override
    public Map<String, String> parse(String url) {
        Map<String, String> dict = new HashMap<>();
        Matcher m = super.regexPattern.matcher(url);

        while (m.find()) {
            for (String groupName : capture_groups) {
                String value = m.group(groupName);

                if (value != null && !value.isEmpty()) {
                    dict.put(groupName, m.group(groupName));
                }
            }
        }

        return dict;
    }
}
