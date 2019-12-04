import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadImageProcessorTest {
    /**
     *  
     */
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;
    private static final Long KEEP_ALIVE_TIME = 1L;
    
    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        /**
         * 
         */
        String key = "keywords";
        // private Iterator<VideoEventData> values;     // 实际参数values 类型
        Integer values = 10;
        // private GroupState<VideoEventData> state;    // 实际参数state 类型
        String outputDir = "E:\\湖大生活\\项目开发\\大数据项目_2019下半年\\github\\Produce_Comsume\\com\\demo\\ImageProcessor";
        Integer state = 1;

        /**
         * 使用阿里巴巴推荐的创建线程池的方式
         * 通过ThreadPoolExecutor构造函数自定义参数创建
         * 
         */
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());

        List<Future<String>> futureList = new ArrayList<>();
       
        ThreadImageProcessor callable = new ThreadImageProcessor(key, values, outputDir, state); // 创建ImageProcessor 线程
        
        for (int i = 0; i < 10; i++) {
            //提交任务到线程池
            Future<String> future = executor.submit(callable);
            //Future<String> future = executor.submit(new ThreadImageProcessor(key, values, outputDir, state));
            //将返回值 future 添加到 list，我们可以通过 future 获得 执行 Callable 得到的返回值
            futureList.add(future);
        }
        for (Future<String> fut : futureList) {
            try {
                System.out.println("返回结果：" + fut.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        //关闭线程池
        executor.shutdown();

        //System.out.println("主线程结束");
    }
}