import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

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
    private static final String not_running = "ThreadPool is not running\n";
    private static final String tPool_started = "Thread pool started, all threads started\n";
    private static final String tPool_terminated = "Thread pool termination process finished\n";
    private static final String tPool_terminating = "Thread pool termination process started\n";
    private static final String terminated_err = "Thread pool already terminated\n";
    private static final String started_err ="Thread pool already started.\n";

    /* Private fields */
    private static final int queue_size = 10;
    // the tasksQueue queue
    private LinkedBlockingQueue<Runnable> tasksQueue = new LinkedBlockingQueue<>(queue_size);
    // the threads array
    private ArrayList<ClientThread> threads;
    private boolean isRunning;

    /**
     * ThreadPool initializer.
     *
     * @param numOfThreads the number of threads in the pool
     */
    public ThreadPool(int numOfThreads) {
        this.threads = new ArrayList<>(numOfThreads);

        for (int i = 0; i < numOfThreads; i++) {
            threads.add(new ClientThread(tasksQueue));
        }

        System.out.printf(init_msg);
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

        System.out.printf(task_added, tasksQueue.size());

        return true;
    }

    /**
     * Terminates the thread pool by terminating each of the threads running.
     */
    public synchronized void terminate() {
        if (this.isRunning) {
            System.out.printf(tPool_terminating);

            // Stopping all the treads even in the middle of a run
            for (ClientThread t : threads) t.interrupt();

            System.out.printf(tPool_terminated);

            // Indicate that the threads are not running any more
            this.isRunning = false;
        } else {
            System.err.printf(terminated_err);
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

            System.out.printf(tPool_started);
        } else {
            System.err.printf(started_err);
        }
    }
}
