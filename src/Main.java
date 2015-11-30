/**
 * Created by Jonathan Yaniv on 30/11/2015.
 * Copyright (c) 2015 Jonathan Yaniv. All rights reserved.
 */
public class Main {
    static int i;

    public static void main(String[] args) {


        ThreadPool tPool = new ThreadPool(10);
        tPool.start();

        for (i = 0; i < 100000; i++) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    System.out.println(i);
                }
            };

            tPool.addTask(r);
        }

        tPool.terminate();
    }
}
