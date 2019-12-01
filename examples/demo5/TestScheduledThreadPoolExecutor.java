import java.util.concurrent.Executors;  
import java.util.concurrent.ExecutorService; 

public class TestScheduledThreadPoolExecutor {

    public static void main(String[] args) {

        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

        exec.scheduleAtFixedRate(new Runnable() {//每隔一段时间就触发异常

                      @Override

                      publicvoid run() {

                           //throw new RuntimeException();

                           System.out.println("================");

                      }

                  }, 1000, 5000, TimeUnit.MILLISECONDS);

        exec.scheduleAtFixedRate(new Runnable() {//每隔一段时间打印系统时间，证明两者是互不影响的

                      @Override

                      publicvoid run() {

                           System.out.println(System.nanoTime());

                      }

                  }, 1000, 2000, TimeUnit.MILLISECONDS);

    }

}