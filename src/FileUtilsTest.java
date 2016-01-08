import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 08/01/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class FileUtilsTest {

    @org.junit.Test
    public void testWriteToFile() throws Exception {
        File tempFile = File.createTempFile("Test_", "_moo.log");  // throws an IOException

        String str = "Test string one!!";

        // convert String into InputStream
        try(InputStream is = new ByteArrayInputStream(str.getBytes())) {
            // read it with BufferedReader
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                FileUtils.writeToFile(tempFile, br);
            }
        }

        List<String> strings = Files.readAllLines(tempFile.toPath(), Charset.forName("utf-8"));
        assertTrue(strings.size() > 0 && strings.get(0).equals(str));
    }
}