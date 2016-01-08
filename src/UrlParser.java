import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class UrlParser extends Parser {

    private static final String[] capture_groups = new String[]{"protocol", "domain", "uri", "port"};
    private static final String url_regex = "(?<protocol>http[s]?):\\/\\/[.*\\@]?(www\\.|.*@)?(?<domain>[\\w\\-]+\\.[\\w\\-]+[\\.[\\w\\-]+]*)(?<port>\\:\\d+)?(?<uri>\\S*)";

    public UrlParser() {
        this(url_regex);
    }

    public UrlParser(String regex) {
        super(regex);
    }

    @Override
    public Map<String, String> parse(String url) {
        Map<String, String> dict = new HashMap<>();
        Matcher m = super.regexPattern.matcher(url);

        while (m.find()) {
            for (String groupName : capture_groups) {
                dict.put(groupName, m.group(groupName));
            }
        }

        if (dict.get("port") == null) {
            dict.put("port", "80");
        }

        return dict;
    }
}
