import java.util.HashMap;

public class Common {
    private static HashMap<Integer, String> httpStatuses = new HashMap<Integer, String>() {
        {
            put(200, "OK");
            put(404, "Not Found");
            put(501, "Not Implemented");
            put(400, "Bad Request");
            put(500, "Internal Server Error");
        }
    };

    public static final String CRLF = "\r\n";
    public static final byte[] CRLFbyte =  new byte[]{0x0d, 0x0a};

    public static String getHttpStatusName(int statusCode) {
        return httpStatuses.get(statusCode);
    }
}
