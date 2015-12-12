import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/7/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class FileIterator implements Iterable<byte[]>, Iterator<byte[]> {

    private final File file;
    private final FileInputStream fis;
    private int chuckSize;
    private long fileLen;
    private boolean errorOccured = false;


    public FileIterator (File file, Integer chuckSize) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) throw new IOException("Unable to locate or open file.");
        if (chuckSize != null) this.chuckSize = chuckSize;

        this.fileLen = file.length();

        // Setting the this.chuckSize
        if (chuckSize == null && fileLen > Integer.MAX_VALUE) {
            this.chuckSize = Integer.MAX_VALUE;
        }
        else if(chuckSize == null) {
            this.chuckSize = (int)fileLen;
        }

        this.file = file;
        fis = new FileInputStream(file);
    }

    @Override
    public boolean hasNext() {
        if (errorOccured) return false;
        Boolean result;

        try {
            result = fis.available() != 0;
        } catch (IOException e) {
            result = false;
        }

        return result;
    }

    @Override
    public byte[] next() {

        try
        {
            byte[] bFile = new byte[chuckSize];

            // read until the end of the stream.
            int read = fis.read(bFile, 0, bFile.length);

            // in the last chunk there is leas bytes usually.
            if (read < bFile.length) {
                bFile = Arrays.copyOf(bFile, read);
            }

            return bFile;
        }
        catch(FileNotFoundException e)
        {
            System.err.println("File Not Found, Not suppose to happen: " + e.toString());
        }
        catch(IOException e)
        {
            System.err.println("Error reading the file: " + e.toString());
        }

        // If we got here there was an error
        errorOccured = true;
        return new byte[0];
    }

    @Override
    public void remove() {

    }

    @Override
    public Iterator<byte[]> iterator() {
        return this;
    }
}
