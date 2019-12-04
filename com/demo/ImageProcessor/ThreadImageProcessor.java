import java.util.concurrent.Callable;

public class ThreadImageProcessor implements Callable<String> {
    private String key;
    // private Iterator<VideoEventData> values;     // 实际参数values 类型
    private Integer values;
    // private GroupState<VideoEventData> state;    // 实际参数state 类型
    private String outputDir;
    private Integer state;
    
    public ThreadImageProcessor(String key, Integer values, String outputDir, Integer state){
        this.key = key;
        this.values = values;
        this.outputDir = outputDir;
        this.state = state;
    }
    // public ThreadImageProcessor(String key){
    //     this.key = key;
    //     // this.values = values;
    //     // this.outputDir = outputDir;
    //     // this.state = state;
    // }
    // 实际执行代码,返回参数类型<VideoEventData>
    // @Override
    // public VideoEventData call() throws Exception {         
    //     VideoEventData processed = ImageProcessor.process(key,values,processedImageDir,existing);   
    //     //返回ImageProcessor.process处理结果
    //     return processed;   
    // }
    
    // 模拟执行代码,返回参数类型integer
    @Override
    public String call() throws Exception {  
        //返回ImageProcessor.process处理结果
        Integer processed = Imitation_ImageProcessor.process(key,values,outputDir,state);   
        String str = processed.toString();
        return str + Thread.currentThread().getName();
    }
    
    // // 参数设置
    // public void setKey(String key) { 
    //     this.key = key;
    // }

    // public void setValues(Integer values) { 
    //     this.values = values;
    // }

    // public void setOutputDir(String outputDir) { 
    //     this.outputDir = outputDir;
    // }

    // public void setState(Integer state) { 
    //     this.state = state;
    // }
}