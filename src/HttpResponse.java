import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private final static String generalHeadersChunked = "Transfer-Encoding: chunked" + CRLF;
    private final static String responseHeaders = "Server: CoolServer/1.0" + CRLF;
    private final static String entityHeadersLastMod = "Last-Modified: %1s" + CRLF;
    private final static String entityHeadersContentType = "Content-Type: %1s" + CRLF;
    private final static String entityHeadersContentLength = "Content-Length: %1d" + CRLF;

    // TODO: add to bonus doc: "Nice bad request page"
    private final static String fileNotFoundFile = "FileNotFound.html";
    private final static String badRequestFile = "BadRequest.html";
    private final static String notImplementedFile = "NotImplemented.html";
    private final static String internalServerErrorFile = "InternalServerError.html";
    private final HTTPRequest request;

    private File file;
    private int statusCode;
    private String lastModified = null;
    private String contentType;
    private byte[] body = null;
    private final Integer defaultChunkSize = 64;

    public HttpResponse(File file, int statusCode, String contentType, HTTPRequest request, Configuration config) {

        this.file = file;
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.request = request;

        if (file == null) {
            switch (this.statusCode) {
                case 400:
                    this.file = new File(config.getRoot(request), badRequestFile);
                    break;
                case 404:
                    this.file = new File(config.getRoot(request), fileNotFoundFile);
                    break;
                case 500:
                    this.file = new File(config.getRoot(request), internalServerErrorFile);
                    break;
                case 501:
                    this.file = new File(config.getRoot(request), notImplementedFile);
                    break;
                default:
                    // Unknown status code
                    break;
            }
        }
    }

    public HttpResponse(byte[] body, int statusCode, String contentType, HTTPRequest request, Configuration config) {
        this((File) null, statusCode, contentType, request, config);
        this.body = body;
    }

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

    public void setLastModified(long lastModified) {
        this.lastModified = Common.ConvertLongToTimeString(lastModified);
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = Common.toISO2616DateFormat(lastModified);
    }

    public long getContentLength() {

        // In case there is a byte array and not a file
        if (file == null) {
            if (body == null) return 0;
            return body.length;
        }

        // If there is a problem with the file or there is no file
        if (!file.exists() || file.isDirectory()) return 0;
        return file.length();
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return The complete response without chunks
     * @throws IOException
     */
    public byte[] getCompleteResponse() throws IOException {

        byte[] headers = CreateResponseHeaders().getBytes(StandardCharsets.US_ASCII);

        System.out.println("-- Response Headers --");
        System.out.println(new String(headers));

        if (file == null && body == null) {
            return headers;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(headers);
        outputStream.write(Common.CRLF_BYTES);

        // In memory byte array
        if (file == null) {
            outputStream.write(body);
            return outputStream.toByteArray();
        }

        // File handle
        FileIterator fileData = new FileIterator(file, null);

        for (byte[] data : fileData) {
            outputStream.write(data);
        }
        
        return outputStream.toByteArray();
    }

    public void sendResponse(DataOutputStream stream) throws IOException {
        sendResponse(stream, defaultChunkSize);
    }

    public void sendResponse(DataOutputStream stream, Integer chunkSize) throws IOException {
        // if the data is already in memory
        if (this.body != null && !this.request.getIsChunked()) {
            stream.write(this.getCompleteResponse());
            return;
        }

        if (chunkSize == null && this.request.getIsChunked()) chunkSize = defaultChunkSize;


        String stringHeaders = CreateResponseHeaders();
        byte[] headers = stringHeaders.getBytes(StandardCharsets.US_ASCII);
        stream.write(headers);
        stream.write(Common.CRLF_BYTES);
        stream.flush();

        System.out.println("-- Response Headers --");
        System.out.println(stringHeaders);

        if (this.body != null) {
            for (int i = 0; i < this.body.length; i += chunkSize) {
                int nextChunkSize = Math.min(this.body.length - i, chunkSize);
                stream.write(getCunckHeaders(nextChunkSize));
                stream.write(this.body, i, nextChunkSize);
                stream.write(Common.CRLF_BYTES);
                stream.flush();
            }
        }

        // File handle
        FileIterator fileData = new FileIterator(file, chunkSize);

        for (byte[] data : fileData) {
            if (this.request.getIsChunked()) {
                stream.write(getCunckHeaders(data.length));
                stream.write(data);
                stream.write(Common.CRLF_BYTES);
            } else {
                stream.write(data);
            }

            stream.flush();
        }
    }

    private byte[] getCunckHeaders(int nextChunkSize) {
        String data = nextChunkSize + Common.CRLF;
        return data.getBytes(StandardCharsets.US_ASCII);
    }

    private String CreateResponseHeaders() {

        Date UtcNow = new Date();
        StringBuilder sb = new StringBuilder();

        // Send all output to the appendable object sb
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format(statusLine, statusCode, Common.getHttpStatusName(statusCode), Common.toISO2616DateFormat(UtcNow));

        if (this.request.getIsChunked()) {
            formatter.format(generalHeadersChunked);
        } else {
            formatter.format(generalHeaders);
        }
        formatter.format(responseHeaders);

        // Check the OPTIONS with *
        if ((RequestType.OPTIONS.name().compareToIgnoreCase(String.valueOf(this.request.getMethod())) != 0) ||
                !this.request.getPath().equals("*")) {
                    // If there is no data there is nothing to chuck also.
                    if (!this.request.getIsChunked() || getContentLength() == 0) {
                        formatter.format(entityHeadersContentLength, getContentLength());
                    }

                    // If there is content to send it's last modified date and type
                    if (getContentLength() > 0) {
                        formatter.format(entityHeadersLastMod, getLastModified());
                        formatter.format(entityHeadersContentType, getContentType());
                    }
                }

        // output
        System.out.println("--Response Headers--");
        System.out.println(sb.toString());

        return sb.toString();
    }

    public byte[] getHeaders() {
        return this.CreateResponseHeaders().getBytes(StandardCharsets.US_ASCII);
    }
}
