package Root;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on {$Date}.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class RunnableScanner implements Runnable {

    // in milliseconds
    private static final int timeout = 250;

    private final HashSet openPorts;
    private final URL url;
    private final int start;
    private final int end;

    public RunnableScanner(HashSet<Integer> openPorts, URL url, int start, int end) {
        this.openPorts = openPorts;
        this.url = url;
        this.start = start;
        this.end = end;
    }

    @Override
    public void run() {
        Socket socket;
        InetSocketAddress socketAddress;
        for (int port = this.start; port <= this.end; port++) {
            socket = new Socket();
            socketAddress = new InetSocketAddress(this.url.getDomain(), port);

            try {
                socket.connect(socketAddress, timeout);
                socket.close();
                this.openPorts.add(port);
            } catch (IOException ignored) {
            }
        }
    }
}
