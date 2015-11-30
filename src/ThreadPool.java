import java.nio.channels.IllegalSelectorException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jonathan Yaniv on 30/11/2015.
 * Copyright (c) 2015 Jonathan Yaniv. All rights reserved.
 */
public class ThreadPool {

    /* Constants */
    private static final String init_msg = "Thread pool initialized\n";
    private static final String task_add = "Task added, current number of tasks in queue: %d\n";
    private static final String capacity_err = "Cannot add task:\n%s\ndue to capacity issue, current count: %d/%d\n";
    private static final String null_err = "Cannot add NULL task.\n";
    private static final String not_running = "ThreadPool is not running\n";
    private static final String tPool_started = "Thread pool started, all threads started\n";
    private static final String tPool_terminated = "Thread pool termination process finished\n";
    private static final String tPool_terminating = "Thread pool termination process started\n";
    private static final String terminated_err = "Thread pool already terminated\n";
    private static final String started_err ="Thread pool already started.\n";

    /* Private fields */

    // the tasks queue
    private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>(queue_size);

    private static final int queue_size = 10;
    // the threads array
    private ArrayList<PoolThread> threads;
    private boolean isRunning;

    /**
     * ThreadPool initializer.
     *
     * @param numOfThreads the number of threads in the pool
     */
    public ThreadPool(int numOfThreads) {
        this.threads = new ArrayList<>(numOfThreads);

        for (int i = 0; i < numOfThreads; i++) {
            threads.add(new PoolThread(tasks));
        }

        System.out.printf(init_msg);
    }

    /**
     * This method adds a new task to the tasks queue
     *
     * @param task the task
     * @return true if task added successfully and false otherwise
     */
    public boolean addTask(Runnable task) throws IllegalStateException {
        if (!this.isRunning)
            throw new IllegalStateException(not_running);

        try {
            this.tasks.put(task);
        } catch (IllegalStateException e) {
            System.err.printf(capacity_err, task.toString(), tasks.size(), queue_size);
            e.printStackTrace();

            return false;
        } catch (ClassCastException e) {
            e.printStackTrace();

            return false;
        } catch (NullPointerException e) {
            System.err.printf(null_err);
            e.printStackTrace();

            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();

            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();

            return false;
        }

        System.out.printf(task_add, tasks.size());

        return true;
    }

    /**
     * Terminates the thread pool by terminating each of the threads running.
     */
    public void terminate() {
        if (this.isRunning) {
            System.out.printf(tPool_terminating);

            for (PoolThread t : threads) t.interrupt();

            System.out.printf(tPool_terminated);
        }

        System.err.printf(terminated_err);
    }

    /**
     * Starts the Thread pool, by starting each of the threads available.
     */
    public void start() {
        if (!this.isRunning) {
            for (Thread t : threads) t.start();

            this.isRunning = true;

            System.out.printf(tPool_started);
        } else {
            System.err.printf(started_err);
        }
    }
}
