import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
 
//import org.springframework.scheduling.config.Task;
 
/**
 * 功能概要：
 * 
 * @author linbingwen
 * @since 2016年5月31日
 */
public class FutrueTaskTest {
 
	public static void main(String[] args) {
		//采用直接启动线程的方法
		System.out.println("main Thread begin at:"+ System.nanoTime());
		MyTask task1 = new MyTask("1");
        FutureTask<Integer> result1 = new FutureTask<Integer>(task1);
        Thread thread1 = new Thread(result1);
        thread1.start();
        
		MyTask task2 = new MyTask("2");
        FutureTask<Integer> result2 = new FutureTask<Integer>(task2);
        Thread thread2 = new Thread(result2);
        thread2.start();
 
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		try {
			System.out.println("task1返回结果:"  + result1.get());
			System.out.println("task2返回结果:"  + result2.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
 
		System.out.println("main Thread finish at:"+ System.nanoTime());
		
	}
}
 
class MyTask implements Callable<Integer> {
	private String name;
	
	public MyTask(String name) {
		this.name = name;
	}
	
	@Override
	public Integer call() throws Exception {
		System.out.println("task"+ name + "开始进行计算");
		Thread.sleep(3000);
		int sum = new Random().nextInt(300);
		int result = 0;
		for (int i = 0; i < sum; i++)
			result += i;
		return result;
	}
 
}