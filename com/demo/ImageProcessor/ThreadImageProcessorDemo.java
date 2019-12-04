
public class ThreadImageProcessorDemo implements Runnable { 
    private String name; 
    public void setName(String name) { 
        this.name = name; 
    } 
    public void run() { 
        System.out.println("hello " + name); 
    } 
    public static void main(String[] args) { 
        ThreadImageProcessorDemo myThread = new ThreadImageProcessorDemo(); 
        myThread.setName("world"); 
        Thread thread = new Thread(myThread); 
        thread.start(); 

        System.out.println("主线程结束");
    } 
} 