package Root;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 12/6/2015.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class ThreadPool {

    /* Constants */
    private static final String init_msg = "Thread pool initialized\n";
    private static final String task_added = "Task added, current number of tasksQueue in queue: %d\n";
    private static final String capacity_err = "Cannot add task:\n%s\ndue to capacity issue, current handledTasksCount: %d/%d\n";
    private static final String null_err = "Cannot add NULL task.\n";
    private static final String not_running = "Root.ThreadPool is not running\n";
    private static final String tPool_started = "Thread pool started, all threads started\n";
    private static final String tPool_terminated = "Thread pool termination process finished\n";
    private static final String tPool_terminating = "Thread pool termination process started\n";
    private static final String terminated_err = "Thread pool already terminated\n";
    private static final String started_err = "Thread pool already started.\n";

    /* Private fields */
    private static final int queue_size = 10000;
    private final AtomicInteger activeCount;
    // the tasksQueue queue
    private LinkedBlockingQueue<Runnable> tasksQueue = new LinkedBlockingQueue<>(queue_size);
    // the threads array
    private ArrayList<ClientThread> threads;
    private boolean isRunning;

    /**
     * Root.ThreadPool initializer.
     *
     * @param numOfThreads the number of threads in the pool
     * @param poolName     - the name of the pool for the tracing
     */
    public ThreadPool(int numOfThreads, String poolName) {
        this.threads = new ArrayList<>(numOfThreads);

        // holds the number of active threads
        this.activeCount = new AtomicInteger(0);

        for (int i = 0; i < numOfThreads; i++) {
            threads.add(new ClientThread(this.tasksQueue, poolName, this.activeCount));
        }

        Logger.writeInfo(init_msg);
    }

    /**
     * This method adds a new task to the tasksQueue queue
     *
     * @param task the task
     * @return true if task added successfully and false otherwise
     */
    public boolean addTask(Runnable task) throws IllegalStateException {
        if (!this.isRunning)
            throw new IllegalStateException(not_running);

        try {
            // non-blocking method, can't hold the server thread if queue is full
            // This queue is non-blocking because the capacity is {@link Integer#MAX_VALUE},
            // in case there are more then {@link Integer#MAX_VALUE} there is a need to drop the new
            // connections.
            this.tasksQueue.add(task);
        } catch (IllegalStateException e) {
            System.err.printf(capacity_err, task.toString(), tasksQueue.size(), queue_size);

            return false;
        } catch (ClassCastException e) {
            e.printStackTrace();

            return false;
        } catch (NullPointerException e) {
            System.err.printf(null_err);

            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();

            return false;
        }

        Logger.writeVerbose(task_added, tasksQueue.size());

        return true;
    }

    /**
     * This method adds a new task to the tasksQueue queue.
     * If no free space in queue, the method blocks until space is available.
     *
     * @param task the task
     * @return true if task added succuessfully and false in case thread intruppted
     * @throws IllegalStateException
     */
    public boolean addTaskBlocking(Runnable task) throws IllegalStateException{
        if (!this.isRunning) {
            throw new IllegalStateException(not_running);
        }

        boolean taskAdded = false;
        try {
            this.tasksQueue.put(task);
            taskAdded = true;
        } catch (InterruptedException e) {

            e.printStackTrace();
        }

        return taskAdded;
    }

    /**
     * Terminates the thread pool by terminating each of the threads running.
     */
    public synchronized void terminate() {
        if (this.isRunning) {
            Logger.writeVerbose(tPool_terminating);

            // Stopping all the treads even in the middle of a run
            for (ClientThread t : threads) t.interrupt();

            Logger.writeVerbose(tPool_terminated);

            // Indicate that the threads are not running any more
            this.isRunning = false;
        } else {
            Logger.writeError(terminated_err);
        }
    }

    /**
     * Starts the Thread pool, by starting each of the threads available.
     */
    public synchronized void start() {
        if (!this.isRunning) {

            // Going over the threads and starting them
            for (Thread t : threads) t.start();

            // Indicate that the threads are running
            this.isRunning = true;

            Logger.writeVerbose(tPool_started);
        } else {
            Logger.writeError(started_err);
        }
    }

    public boolean isQueueEmpty() {
        return this.tasksQueue.isEmpty();
    }

    /**
     * @return true if all threads are not active and queue is empty
     */
    public boolean isActive() {
        return this.activeCount.get() > 0 || !this.isQueueEmpty();
    }
}
