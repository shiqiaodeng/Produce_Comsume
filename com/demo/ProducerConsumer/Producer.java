

/**
 * 生产者
 * @version 1.1 2019-11-29
 * @author Shiqiao Deng 
 */
public class Producer implements Runnable{

    private Resource resource;

    public Producer(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            resource.create();
        }

    }
}