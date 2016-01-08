package Root;

import java.io.*;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 08/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class FileUtils {

    /**
     * @param file The file to write the buffer to
     * @param buffer the data to write to the file
     * @throws IOException - problems with the file parameter
     */
    public static void writeToFile(File file, BufferedReader buffer) throws IOException {
        // Check that the file is ok
        if (file == null || !file.exists() || file.isDirectory()) {
            throw new IOException(String.format("Problem using the given file: %s", file));
        }

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()), "utf-8"))) {
            // Reading the file line by line
            String line;
            while ((line = buffer.readLine()) != null) {
                writer.write(line);
            }
        }

        // TODO: Think if the is the beast way to implement????
    }
}
