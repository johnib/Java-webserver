import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/6/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */

/**
 * This class is a Runnable wrapper for the client socket, this way it can be processed by other threads.
 */
public class RunnableClient implements Runnable {

    /* private fields */
    private final Socket socket;
    private static final String CRLF = "\r\n";

    /**
     * Creates a Runnable wrapper for the given socket.
     *
     * @param clientSocket the socket
     */
    public RunnableClient(Socket clientSocket) {
        this.socket = clientSocket;
    }

    @Override
    public void run() {
        //TODO: implement client-request-response lifecycle

        /* read the request from client and print it */
        String requestString = this.readRequest();
        System.out.println(requestString);


        this.close();
    }

    public String readRequest() {
        String requestLine = null;
        StringBuilder sb = new StringBuilder("");

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            requestLine = reader.readLine() + CRLF;

            String header;
            while (!(header = reader.readLine()).isEmpty()) {
                sb.append(header);
                sb.append(CRLF);
            }

        } catch (IOException e) {
            //TODO: implement
            System.out.printf("Thread-%d: Connection reset\n", Thread.currentThread().getId());
        }

        return requestLine + sb.toString();
    }

    /**
     * Do all necessary operations before stopping to handle this client.
     */
    public void close() {
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                //TODO: implement
            }

        }

    }
}
