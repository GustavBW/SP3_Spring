package gbw.sp3.OpcClient.AsyncEventLoop;

import java.util.Queue;

public class LoadDistributor {

    private EnrichedThread[] pool = new EnrichedThread[0];

    /**
     * Removes as many runnables from the queue
     * as there is threads available in the distributor.
     * Then executes all.
     * @param runnables Total queue of all runnables.
     * @return how many runnables was able to be removed from the queue
     */
    public int distribute(Queue<EnrichedRunnable<?>> runnables)
    {
        int count = 0;
        for( EnrichedThread thread : pool ) {
            if(verifyRun(thread, runnables.peek())){
                thread.setNewTarget(runnables.poll());
                count++;
            }
        }
        return count;
    }

    /**
     * Determines wether or not this thread is available AND
     * if the runnable should be run or is a valid runnable in the
     * first place.
     * @param thread
     * @param runnable
     * @return
     */
    private boolean verifyRun(EnrichedThread thread, EnrichedRunnable<?> runnable)
    {
        return thread.isAvailable()
                && runnable != null
                && !runnable.isRunning.get()
                && !runnable.isDone.get();
    }

    /**
     * Shuts down the current thread pool and boots a new one
     * with the specified size.
     * @param size
     */
    public void setPoolSize(int size)
    {
        setPoolSize(size,500);
    }
    public void setPoolSize(int size, int maxDelay)
    {
        shutdown(maxDelay);
        pool = new EnrichedThread[size];
        for (int i = 0; i < pool.length; i++)
        {
            pool[i] = new EnrichedThread();
        }
    }

    /**
     * Shuts down all active threads in pool
     * immediatly with no error mitigation
     */
    public void shutdown()
    {
        shutdown(0);
    }

    public void shutdown(int msecs)
    {
        for(EnrichedThread thread : pool) {
            thread.shutdown(msecs);
        }
    }
}
