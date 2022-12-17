package gbw.sp3.OpcClient.AsyncEventLoop;

import java.util.concurrent.atomic.AtomicBoolean;

public class EnrichedThread implements Runnable{

    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicBoolean shouldBeRunning = new AtomicBoolean(true);
    private final Thread thread = new Thread();
    private EnrichedRunnable<?> currentRunnable;

    public EnrichedThread()
    {
        thread.start();
    }

    public synchronized void setNewTarget(EnrichedRunnable<?> runnable)
    {
        running.set(true);
        currentRunnable = runnable;
        this.notify();
    }

    @Override
    public void run()
    {
        while(shouldBeRunning.get()){
            try{
                thread.wait();
                running.set(true);
                if(currentRunnable != null){
                    currentRunnable.run(Thread.currentThread());
                }
                running.set(false);
            }catch (InterruptedException ignored){}
        }
    }

    public boolean isAvailable()
    {
        return !running.get();
    }

    public void shutdown(int allowedMaximumDelay)
    {
        shouldBeRunning.set(false);
        if(allowedMaximumDelay == 0){
            thread.interrupt();
        }

        try {
            thread.join(allowedMaximumDelay);
        } catch (InterruptedException e) {
            thread.interrupt();
        }
    }
}
