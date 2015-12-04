import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Jonathan Yaniv and Nitsan Bracha on 30/11/2015.
 */
public class CustomThread extends Thread {

    /* Constants */
    private static final String intrpt_msg = "Thread-%d was interrupted.\n";
    private static final String init_msg = "Thread constructed\n";
    private static final String started_msg = "Thread-%d started\n";
    private static final String task_picked = "Thread-%d picked a task\n";
    private static final String task_finished = "Thread-%d finished running task\n";
    private static final String task_pop = "Thread-%d popping task\n";

    // the tasks queue
    private LinkedBlockingQueue<Runnable> queue;

    public int count = 0;

    /**
     * Constructs a new pool thread.
     * @param queue the tasks queue.
     */
    public CustomThread(LinkedBlockingQueue<Runnable> queue) {
        super();
        this.queue = queue;

        System.out.printf(init_msg);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        System.out.printf(started_msg, Thread.currentThread().getId());
        while (true) {
            try {
                // get the next task and handle it
                System.out.printf(task_pop, Thread.currentThread().getId());
                Runnable task = queue.take();

                System.out.printf(task_picked, Thread.currentThread().getId());

                task.run();
                count++;

                System.out.printf(task_finished, Thread.currentThread().getId());
            } catch (InterruptedException e) {
                System.out.printf(intrpt_msg, Thread.currentThread().getId());
//                e.printStackTrace();

                break;
            }
        }
    }
}
