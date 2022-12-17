package gbw.sp3.OpcClient.AsyncEventLoop;

import org.springframework.lang.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntryPoint implements Runnable{

   private static final EntryPoint instance = new EntryPoint();
   private static volatile Queue<EnrichedRunnable<?>> queue = new ConcurrentLinkedQueue<>();
   private static final AtomicBoolean applicationIsRunning = new AtomicBoolean(true);
   private static final Thread manager = new Thread(instance);
   private static final LoadDistributor distributor = new LoadDistributor();

   private static long RETRY_DELAY_MSECS = 100L;

   static {
      Runtime.getRuntime().addShutdownHook(
           new Thread(() -> {
              shutdown();
              synchronized (instance) {
                 instance.notify();
              }
              try {
                 manager.join();
              } catch (InterruptedException e) {
                 e.printStackTrace();
              }
           })
      );
   }

   public static <T> EnrichedRunnable<T> async(iFunction<T> runnable)
   {
      EnrichedRunnable<T> enriched = new EnrichedRunnable<>(runnable);
      queue.add(enriched);
      synchronized(instance) {
         instance.notify();
      }
      return enriched;
   }

   public static <T> T await(EnrichedRunnable<T> runnable)
   {
      return runnable.get();
   }

   @Override
   public void run()
   {
      long msecs = RETRY_DELAY_MSECS;

      synchronized (this) {
         while(applicationIsRunning.get()){
            try {
               this.wait(msecs, 0);

               distributor.distribute(queue);

               if (!queue.isEmpty()) {
                  msecs = RETRY_DELAY_MSECS;
               } else {
                  msecs = 0L;
               }

            } catch (InterruptedException e) {
               System.err.println("| FATAL | EntryPoint."
                       + Thread.currentThread().getStackTrace()[0].getLineNumber()
                       + " Manager was unexpectedly interrupted");
            }
         }
      }

   }

   public static void setRetryDelay(long msecs)
   {
      RETRY_DELAY_MSECS = msecs;
   }

   public static void initialize()
   {
      initialize(1);
   }

   public static void initialize(int poolSize)
   {
      distributor.setPoolSize(poolSize);
      manager.start();
   }

   private static void shutdown()
   {
      applicationIsRunning.set(false);
      distributor.shutdown();
   }
}
