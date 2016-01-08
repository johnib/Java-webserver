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

    /**
     * Constructs a new pool thread.
     *
     * @param queue the tasks queue.
     */
    public ClientThread(LinkedBlockingQueue<Runnable> queue) {
        super();
        this.queue = queue;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        System.out.printf(started_msg, Thread.currentThread().getId());

        try {
            while (true) {
                // get the next task and handle it
                // System.out.printf(task_pop, Thread.currentThread().getId());
                Runnable task = queue.take();

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
