package gbw.sp3.OpcClient.AsyncEventLoop;

import gbw.sp3.OpcClient.AsyncEventLoop.iFunction;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class EnrichedRunnable<T> {

    public volatile AtomicBoolean isDone = new AtomicBoolean(false);
    public volatile AtomicBoolean isRunning = new AtomicBoolean(false);
    private T returnObject;
    public iFunction<T> runnable;
    public Thread runner;

    public EnrichedRunnable(iFunction<T> runnable){
        Objects.requireNonNull(runnable);
        this.runnable = runnable;
    }

    public void run(Thread runner) {
        this.runner = runner;
        isRunning.set(true);
        returnObject = runnable.execute();

        synchronized (this) {
            this.notify();
        }

        isDone.set(true);
        isRunning.set(false);
    }

    public synchronized T get()
    {
        if(isRunning.get()) {
            try {
                Thread.currentThread().wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else {
            run(Thread.currentThread());
        }
        return returnObject;
    }
}