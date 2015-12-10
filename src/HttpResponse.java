import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/7/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class HttpResponse {
    /* Constants */
    private final static String CRLF = Common.CRLF;
    private final static String statusLine = "HTTP/1.1 %1d %2s" + CRLF + "Date: %3s" + CRLF;
    private final static String generalHeaders = "Connection: close" + CRLF;
    private final static String responseHeaders = "Server: CoolServer/1.0" + CRLF;
    private final static String entityHeadersNoContent = "Content-Length: 0" + CRLF;
    private final static String entityHeaders = "Last-Modified: %1s" + CRLF +
            "Content-Type: %2s" + CRLF +
            "Content-Length: %3d" + CRLF;

    // TODO: add to bonus doc: "Nice bad request page"
    private final static String fileNotFoundFile = "FileNotFound.html";
    private final static String badRequestFile = "BadRequest.html";


    private File file;
    private int statusCode;
    private String lastModified = null;
    private String contentType;
    private final HTTPRequest request;
    private byte[] body = null;

    public HttpResponse(File file, int statusCode, String contentType, HTTPRequest request, Configuration config) {

        this.file = file;
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.request = request;

        if (file == null) {
            switch (this.statusCode) {
                case 404:
                    this.file = new File(config.getRoot(request), fileNotFoundFile);
                    break;
                case 400:
                    this.file = new File(config.getRoot(request), badRequestFile);
            }
        }
    }

    public HttpResponse(byte[] body, int statusCode, String contentType, HTTPRequest request, Configuration config) {
        this((File) null, statusCode, contentType, request, config);
        this.body = body;
    }

//    public HttpResponse(){
//
//    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getLastModified() {
        if (lastModified == null) {
            if (file != null && file.exists()) {
                setLastModified(file.lastModified());
            } else {
                setLastModified(new Date());
            }
        }

        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = Common.toISO2616DateFormat(lastModified);
    }

    public void setLastModified(long lastModified) {
        this.lastModified = Common.ConvertLongToTimeString(lastModified);
    }

    public long getContentLength() {
        if (file == null || !file.exists() || file.isDirectory()) return 0;
        return file.length();
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] CreateResponse() throws IOException {

        byte[] headers = CreateResponseHeaders().getBytes(StandardCharsets.US_ASCII);
        if (file == null && body == null) {
            return headers;
        }

        byte[] body;
        if (this.body == null) body = Files.readAllBytes(file.toPath());
        else body = this.body;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(headers);
        outputStream.write(Common.CRLF_BYTES);
        outputStream.write(body);

        return outputStream.toByteArray();
    }



    private String CreateResponseHeaders() {

        Date UtcNow = new Date();
        StringBuilder sb = new StringBuilder();

        // Send all output to the appendable object sb
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format(statusLine, statusCode, Common.getHttpStatusName(statusCode), Common.toISO2616DateFormat(UtcNow));
        formatter.format(generalHeaders);
        formatter.format(responseHeaders);

        if (getContentLength() > 0) {
            formatter.format(entityHeaders, getLastModified(), getContentType(), getContentLength());
        } else {
            formatter.format(entityHeadersNoContent);
        }

        return sb.toString();
    }

    public byte[] getHeaders() {
        return this.CreateResponseHeaders().getBytes(StandardCharsets.US_ASCII);
    }
}
