package Root;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class UrlParser extends Parser {

    private static final String[] capture_groups = new String[]{"protocol", "domain", "uri", "port"};
    private static final String url_regex = "((?<protocol>http[s]?)\\:\\/\\/)?[.*\\@]?(www\\.|.*@)?(?<domain>([\\w\\-]+\\.[\\w\\-]+[\\.[\\w\\-]+]*)|localhost|([0-9]{1,3}\\.){4,4})(\\:(?<port>\\d{2,5}))?(?<uri>[^\\s\\:?\\#]*)";
    private static final String uri_normalizer_regex = "(?<redundant>/\\w+/\\.\\./)";


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
                String value = m.group(groupName);

                if (groupName.equals("uri")) {
                    value = this.normalizeUri(value);
                }

                if (value != null && !value.isEmpty()) {
                    dict.put(groupName, value);
                }
            }
        }

        return dict;
    }

    private String normalizeUri(String uri) {
        while (uri.contains("/../")) {
            uri = uri.replaceAll(uri_normalizer_regex, "/");
        }
        return uri;
    }
}
