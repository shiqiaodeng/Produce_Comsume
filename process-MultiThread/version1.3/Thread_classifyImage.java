package com.iot.video.app.spark.processor;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import com.iot.video.app.spark.util.VideoEventData;

/**
 * 并行代码
 */
public class Thread_classifyImage implements Runnable{
    private static final Logger logger = Logger.getLogger(Thread_classifyImage.class);        // 打印日志
    private String camId;           // camera id
    private ArrayList<VideoEventData> total_frame;      // 总处理数据帧
    private String outputDir;       // 结果输出文件目录
    private VideoEventData previousProcessedEventData; // 上一次处理的VideoEventData
    private int thread_num;     // 线程数量
    private int thread_rank;    // 当前线程名称编号
    private VideoEventData processed = null;  // 返回值
    /* 构造器传递参数 */
    public Thread_classifyImage(String camId, ArrayList<VideoEventData> total_frame, String outputDir,
                         VideoEventData previousProcessedEventData, int thread_rank, int thread_num){
        this.camId = camId;
        this.total_frame = total_frame;
        this.outputDir = outputDir;
        this.previousProcessedEventData = previousProcessedEventData;
        this.thread_rank = thread_rank;
        this.thread_num = thread_num;
    }

    public void run(){
        try{
            Thread t = Thread.currentThread();
            String thread_name = t.getName();
            logger.warn(" thread " + Integer.toString(thread_rank) + " of " + Integer.toString(thread_num));     // 打印当前线程

            /* 数据分割 */
            ArrayList<VideoEventData> local_frame = new ArrayList<VideoEventData>();    // 局部处理数据
            int len = total_frame.size();    // 获取数据大小
            int d = len / thread_num;       // 划分数据比例
            int start = thread_rank * d;    // 子线程处理数据起始位置
            int end = 0 ;                       // 子线程处理数据结束位置
            if (thread_rank < (thread_num - 1)) {     // 前n-1 个线程通过线程编号划分数据
                end = (thread_rank + 1) * d - 1;     // 按比例划分
            } else {
                end = len - 1;                  // 最后一个线程可能无法按比例划分
            }
            int len_local = end - start + 1;        // 局部数据帧长度
            /* 测试数据划分是否正确 */
            logger.warn("len = " + Integer.toString(len) + " d = " + Integer.toString(d) +
                    " start = " + Integer.toString(start) + " end = " + Integer.toString(end) +
                    " len_local = " + Integer.toString(len_local) + " thread_rank = " + Integer.toString(thread_rank));

            for (int i = 0; i < len_local; i++) {           // 局部存储数据
                local_frame.add(total_frame.get(i + start));
            }
            logger.warn("Thread " + Integer.toString(thread_rank) + " local_frame_size " + Integer.toString(local_frame.size()));
            /* 执行 ImageProcessor  */
            processed = ImageProcessor.process(camId, local_frame, outputDir, previousProcessedEventData);
        }catch(Exception e){
            logger.warn("Thread " + Integer.toString(thread_rank) + " running failed ");
            e.printStackTrace();
        }
    }
    // 返回处理结果
    public VideoEventData get_processed(){
        return processed;
    }
}
