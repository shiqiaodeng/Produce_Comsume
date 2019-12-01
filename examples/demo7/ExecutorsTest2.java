/**
 * 链接：https://blog.csdn.net/wildpal/article/details/53436766
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorsTest2 {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ////线程数
        int num = 10;
        //CountDownLatch是一个同步辅助类也可以使用AtomicInteger替代
        CountDownLatch doneSignal = new CountDownLatch(num);
        ExecutorService pool = Executors.newFixedThreadPool(num);
        for(int i=0;i<num;i++)
         //在未来某个时间执行给定的命令
            pool.execute(new WorkerRunnable(doneSignal, i));
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            //关闭线程池
            pool.shutdown();
        }
         //子线程执行完毕，可以开始后续任务处理了
        System.out.println("所有任务执行完毕");

    }

}

class WorkerRunnable implements Runnable {
       private final CountDownLatch doneSignal;
       private final int i;
       WorkerRunnable(CountDownLatch doneSignal, int i) {
          this.doneSignal = doneSignal;
          this.i = i;
       }
       public void run() {
          //子线程执行任务
          try{
              doWork(i);
          } catch (Exception e) {
            e.printStackTrace();
          }
          //任务执行完毕递减锁存器的计数
          doneSignal.countDown();
       }

       void doWork(int i) {
           System.out.println("这是第"+(i+1)+"个任务");
       }
     }