import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/6/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class Server {

    /* Constants */

    private final static String server_error = "Error listening on: %s\nFor reason: %s\n Try a different port?\n";
    private final static String fatal_error = "FATAL error, exiting\n";
    private final static String init_success = "Server: instantiated successfully with configuration:\n%s\n";
    private final static String server_started = "Server: Started successfully\n";
    private final static String listen_fail = "Server cannot listen, not running\n";

    /* private fields */
    private final ThreadPool pool;
    private final Configuration config;
    private boolean serverRunning;
    private ServerSocket serverSocket;

    /**
     * Creates a new server with config file.
     *
     * @param configuration the server configuration must consist of port, maxThreads, root, defaultPage keys.
     */
    public Server(Configuration configuration) {
        this.config = configuration;

        /* create thread pool */
        this.pool = new ThreadPool(config.getMaxThreads());

        //System.out.printf(init_success, parser.toString());
    }

    /**
     * Starts the server.
     */
    public boolean start() {
        /* start server */
        try {
            serverSocket = new ServerSocket(config.getPort());

            serverRunning = true;
            System.out.printf(server_started);
        } catch (IOException e) {
            System.err.printf(server_error, config.getPort(), e.getCause());
            e.printStackTrace();

            System.err.printf(fatal_error);
            serverRunning = false;
        }

        if (serverRunning) {
            /* start threads in pool */
            this.pool.start();
        }

        return serverRunning;
    }

    /**
     * This method makes the server listen on it's port.
     */
    public void listen() {
        if (!serverRunning) {
            System.err.printf(listen_fail);
        }

        try {
            while (serverRunning) {
                /* start listening */
                Socket clientSocket = serverSocket.accept();
                Runnable client = new RunnableClient(clientSocket);

                /* submit task */
                pool.addTask(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Terminates the server and all the worker threads.
     */
    public void terminate() {
        serverRunning = false;

        try {
            serverSocket.close();
        } catch (IOException ignored) {
            // Left empty
        }

        pool.terminate();
    }
}
