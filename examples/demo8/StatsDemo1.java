import java.text.SimpleDateFormat;
import java.util.concurrent.*;
import java.util.*;
/**
 * 链接：https://it.baiked.com/dev/2155.html
 * 多任务并行+线程池统计
 * 创建者 科帮网 https://blog.52itstyle.com
 * 创建时间    2018年4月17日
 */
public class StatsDemo1 {
    final static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    
    final static String startTime = sdf.format(new Date());
    
    /**
     * IO密集型任务  = 一般为2*CPU核心数（常出现于线程中：数据库数据交互、文件上传下载、网络数据传输等等）
     * CPU密集型任务 = 一般为CPU核心数+1（常出现于线程中：复杂算法）
     * 混合型任务  = 视机器配置和复杂度自测而定
     */
    private static int corePoolSize = Runtime.getRuntime().availableProcessors();
    /**
     * public ThreadPoolExecutor(int corePoolSize,int maximumPoolSize,long keepAliveTime,
     *                           TimeUnit unit,BlockingQueue<Runnable> workQueue)
     * corePoolSize用于指定核心线程数量
     * maximumPoolSize指定最大线程数
     * keepAliveTime和TimeUnit指定线程空闲后的最大存活时间
     * workQueue则是线程池的缓冲队列,还未执行的线程会在队列中等待
     * 监控队列长度，确保队列有界
     * 不当的线程池大小会使得处理速度变慢，稳定性下降，并且导致内存泄露。如果配置的线程过少，则队列会持续变大，消耗过多内存。
     * 而过多的线程又会 由于频繁的上下文切换导致整个系统的速度变缓——殊途而同归。队列的长度至关重要，它必须得是有界的，这样如果线程池不堪重负了它可以暂时拒绝掉新的请求。
     * ExecutorService 默认的实现是一个无界的 LinkedBlockingQueue。
     */
    private static ThreadPoolExecutor executor  = new ThreadPoolExecutor(corePoolSize, corePoolSize+1, 10l, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1000));
    
    public static void main(String[] args) throws InterruptedException {
        List<Future<String>> resultList = new ArrayList<Future<String>>(); 
        //使用submit提交异步任务，并且获取返回值为future
        resultList.add(executor.submit(new Stats("任务A", 1000)));
        resultList.add(executor.submit(new Stats("任务B", 1000)));
        resultList.add(executor.submit(new Stats("任务C", 1000)));
        resultList.add(executor.submit(new Stats("任务D", 1000)));
        resultList.add(executor.submit(new Stats("任务E", 1000)));
         //遍历任务的结果
        for (Future<String> fs : resultList) { 
            try { 
                System.out.println(fs.get());//打印各个线任务执行的结果，调用future.get() 阻塞主线程，获取异步任务的返回结果
            } catch (InterruptedException e) { 
                e.printStackTrace(); 
            } catch (ExecutionException e) { 
                e.printStackTrace(); 
            } finally { 
                //启动一次顺序关闭，执行以前提交的任务，但不接受新任务。如果已经关闭，则调用没有其他作用。
                executor.shutdown(); 
            } 
        } 
        System.out.println("所有的统计任务执行完成:" + sdf.format(new Date()));
    }

    static class Stats implements Callable<String>  {
        String statsName;
        int runTime;

        public Stats(String statsName, int runTime) {
            this.statsName = statsName;
            this.runTime = runTime;
        }

        public String call() {
            try {
                System.out.println(statsName+ " do stats begin at "+ startTime);
                //模拟任务执行时间
                Thread.sleep(runTime);
                System.out.println(statsName + " do stats complete at "+ sdf.format(new Date()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return call();
        }
    }
}
