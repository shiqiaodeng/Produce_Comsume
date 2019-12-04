 
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
 
/**
 * 功能概要：
 * 
 * @author linbingwen
 * @since 2016年5月31日
 */
public class FutrueTaskTest2 {
 
	public static void main(String[] args) {
		System.out.println("main Thread begin at:"+ System.nanoTime());
		ExecutorService executor = Executors.newCachedThreadPool();
		MyTask2 task1 = new MyTask2("1");
		MyTask2 task2 = new MyTask2("2");
		Future<Integer> result1 = executor.submit(task1);
		Future<Integer> result2 = executor.submit(task2);
		executor.shutdown();
 
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
 
class MyTask2 implements Callable<Integer> {
	private String name;
	
	public MyTask2(String name) {
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