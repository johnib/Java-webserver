package Root;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/6/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class ClientThread extends Thread {

    /* Constants */
    private static final String intrpt_msg = "Thread-%d was interrupted.\n";
    private static final String started_msg = "Thread-%d started\n";
    private static final String task_picked = "Thread-%d picked a task\n";
    private static final String task_finished = "Thread-%d finished running task\n";
    public int handledTasksCount = 0;
    // the tasks queue
    private LinkedBlockingQueue<Runnable> queue;
    private String poolName;

    /**
     * Constructs a new pool thread.
     *
     * @param queue the tasks queue.
     * @param poolName name of the pool. used to print traces
     */
    public ClientThread(LinkedBlockingQueue<Runnable> queue, String poolName) {
        super();
        this.queue = queue;
        this.poolName = poolName;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        System.out.printf(started_msg, Thread.currentThread().getId());

        try {
            while (true) {
                // get the next task and handle it
                // System.out.printf(task_pop, Thread.currentThread().getId());
                // TODO: check the google docs question number 5
                Runnable task = queue.take();
                int queueSize = queue.size();
                Logger.writeAssignmentTrace(String.format("%s dequeue from their queue. number of items:%d", poolName, queueSize));

                System.out.printf(task_picked, Thread.currentThread().getId());

                task.run();
                handledTasksCount++;

                System.out.printf(task_finished, Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.printf(intrpt_msg, Thread.currentThread().getId());
        } catch (Exception e) {
            //TODO: consider a mechanism for reinstantiating new thread in case of lost ones.
            e.printStackTrace();
        }

    }
}
