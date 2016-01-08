1.      These steps will explain how the server works from a higher point of view, less implementation details.
1.0     The main thread listens on the requested port.
1.1     Once connection is accepted, puts it in a queue.
1.2     Returns to step 1.

2.      At the other end of the queue, one of the other N threads (of the Thread Pool) pulls a socket from the queue.
        Each one of the threads:
2.0     Pulls a socket from the queue.
2.1     Reads the request from socket's input stream.
2.2     Parses the request using regex and creates an Root.HTTPRequest object.
2.3     Creates a response from the parsed Root.HTTPRequest.
2.4     Sends the response to the socket's output stream.
2.5     Returns to step 2.