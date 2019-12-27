package com.iot.video.app.spark.processor;

import java.util.Iterator;
import java.util.Properties;

import java.util.concurrent.Callable;

import com.iot.video.app.spark.util.VideoEventData;
import com.iot.video.app.spark.processor.ImageProcessor;


public class ThreadImageProcessor implements Callable<VideoEventData> {
    private String key;
    private Iterator<VideoEventData> values;     // parameter values type
    private String processedImageDir;    // parameter processedImageDir  type
    private VideoEventData existing;
    
    public ThreadImageProcessor(String key, Iterator<VideoEventData> values, String processedImageDir, VideoEventData existing){
        this.key = key;
        this.values = values;
        this.processedImageDir = processedImageDir;
        this.existing = existing;
    }

    // actual running code,return parameter <VideoEventData>
    @Override
    public VideoEventData call() throws Exception {         
        VideoEventData processed = ImageProcessor.process(key,values,processedImageDir,existing);   
        System.out.println("the thread: " + Thread.currentThread().getName());
        return processed;       //return ImageProcessor.process results
    }
}