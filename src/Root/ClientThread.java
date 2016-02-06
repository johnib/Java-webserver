package Root;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final AtomicInteger activeCount;
    public int handledTasksCount = 0;
    private LinkedBlockingQueue<Runnable> queue;
    private String poolName;

    /**
     * Constructs a new pool thread.
     *
     * @param queue       the tasks queue.
     * @param poolName    the name of the pool. used to print traces.
     * @param activeCount the active threads counter.
     */
    public ClientThread(LinkedBlockingQueue<Runnable> queue, String poolName, AtomicInteger activeCount) {
        super();
        this.queue = queue;
        this.poolName = poolName;
        this.activeCount = activeCount;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        Logger.writeWebServerLog(started_msg, Thread.currentThread().getId());

        try {
            while (true) {
                // get the next task and handle it
                // System.out.printf(task_pop, Thread.currentThread().getId());
                // TODO: check the google docs question number 5
                Runnable task = queue.take();

                int activeCount = this.activeCount.incrementAndGet();
                if (activeCount > 10 || activeCount < 0) {
                    throw new IllegalStateException("Active count is: " + activeCount);
                }

                int queueSize = queue.size();
                Logger.writeAssignmentTrace(String.format("%s dequeue from their queue. number of items: %d", poolName, queueSize));

                Logger.writeWebServerLog(task_picked, Thread.currentThread().getId());

                task.run();
                handledTasksCount++;

                activeCount = this.activeCount.decrementAndGet();
                if (activeCount > 10 || activeCount < 0) {
                    throw new IllegalStateException("Active count is: " + activeCount);
                }

                Logger.writeWebServerLog(task_finished, Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            Logger.writeWebServerLog(intrpt_msg, Thread.currentThread().getId());
        } catch (Exception e) {
            //TODO: consider a mechanism for reinstantiating new thread in case of lost ones.
            e.printStackTrace();
        }

    }
}
