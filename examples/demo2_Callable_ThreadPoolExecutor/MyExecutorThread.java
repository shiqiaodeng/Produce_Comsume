import java.util.concurrent.Callable;

public class MyExecutorThread implements Callable {

    @Override
    public Object call() throws Exception {
        System.out.println("Thread: " + Thread.currentThread().getName() + " begin...");
        Thread.sleep(5 * 1000);
        System.out.println("Thread: " + Thread.currentThread().getName() + " end...");
        return null;
    }
}