package com.iot.video.app.spark.processor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.api.java.function.MapGroupsWithStateFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.KeyValueGroupedDataset;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.streaming.GroupState;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import com.iot.video.app.spark.util.PropertyFileReader;
import com.iot.video.app.spark.util.VideoEventData;

/**
 * Class to consume incoming JSON messages from Kafka and process them using Spark Structured Streaming.
 *  
 * @author abaghel
 *
 */
public class VideoStreamProcessor {
  private static final Logger logger = Logger.getLogger(VideoStreamProcessor.class);	
	
  public static void main(String[] args) throws Exception {
	//Read properties
	Properties prop = PropertyFileReader.readPropertyFile();
	
	//SparkSesion
	SparkSession spark = SparkSession
		      .builder()
		      .appName("VideoStreamProcessor")
		      .master(prop.getProperty("spark.master.url"))
		      .getOrCreate();	
	
	//directory to save image files with motion detected
	final String processedImageDir = prop.getProperty("processed.output.dir");
	logger.warn("Output directory for saving processed images is set to "+processedImageDir+". This is configured in processed.output.dir key of property file.");
	
	//create schema for json message
	StructType schema =  DataTypes.createStructType(new StructField[] { 
			DataTypes.createStructField("cameraId", DataTypes.StringType, true),
			DataTypes.createStructField("timestamp", DataTypes.TimestampType, true),
			DataTypes.createStructField("rows", DataTypes.IntegerType, true),
			DataTypes.createStructField("cols", DataTypes.IntegerType, true),
			DataTypes.createStructField("type", DataTypes.IntegerType, true),
			DataTypes.createStructField("data", DataTypes.StringType, true)
			});
	

	//Create DataSet from stream messages from kafka
    Dataset<VideoEventData> ds = spark
      .readStream()
      .format("kafka")
      .option("kafka.bootstrap.servers", prop.getProperty("kafka.bootstrap.servers"))
      .option("subscribe", prop.getProperty("kafka.topic"))
      .option("kafka.max.partition.fetch.bytes", prop.getProperty("kafka.max.partition.fetch.bytes"))
      .option("kafka.max.poll.records", prop.getProperty("kafka.max.poll.records"))
      .load()
      .selectExpr("CAST(value AS STRING) as message")
      .select(functions.from_json(functions.col("message"),schema).as("json"))
      .select("json.*")
      .as(Encoders.bean(VideoEventData.class)); 
    
    //key-value pair of cameraId-VideoEventData
	KeyValueGroupedDataset<String, VideoEventData> kvDataset = ds.groupByKey(new MapFunction<VideoEventData, String>() {
		@Override
		public String call(VideoEventData value) throws Exception {
			return value.getCameraId();
		}
	}, Encoders.STRING());
		
	//process
	Dataset<VideoEventData> processedDataset = kvDataset.mapGroupsWithState(new MapGroupsWithStateFunction<String, VideoEventData, VideoEventData,VideoEventData>(){
		@Override
		public VideoEventData call(String key, Iterator<VideoEventData> values, GroupState<VideoEventData> state) throws Exception {
			logger.warn("CameraId="+key+" PartitionId="+TaskContext.getPartitionId());
			VideoEventData existing = null;
			VideoEventData processed = null;
			//check previous state
			if (state.exists()) {
				existing = state.get();
			}

			/* classify image */
			logger.warn("图像分类程序开始运行");
			/* 对数据存储，并按照采样时间排序 */
			//Add frames to list
			ArrayList<VideoEventData> sortedList = new ArrayList<VideoEventData>();
			int i = 0;
			while(values.hasNext()){
				sortedList.add(values.next());
				i += 1;
			}
			//previous processed frame
			if (existing != null) {
				logger.warn("cameraId=" + key + " previous processed timestamp=" + existing.getTimestamp());
				sortedList.add(existing);
			}
			//sort frames by timestamp
			sortedList.sort(Comparator.comparing(VideoEventData::getTimestamp));
			logger.warn("cameraId="+ key +" total frames=" + sortedList.size());

			/* 并行执行 */
			logger.warn("并行执行");
			int thread_num = 5;		// 线程数量
			ArrayList<Thread_classifyImage> Thread_List = new ArrayList<Thread_classifyImage>();	// 初始化Runnable
			for(int proc = 0; proc < thread_num; proc++ ){
				Thread_List.add(new Thread_classifyImage(key, sortedList, processedImageDir, existing, proc, thread_num));
			}
			for(int j = 0; j < thread_num; j ++){
				Thread t = new Thread(Thread_List.get(j));
				t.start();		// 调用start方法，就会执行 Thread_classifyImage run方法；
				if( j == thread_num - 1){ // 主线程需要等待最后一个子线程处理完成，得到最后一个线程返回的processed
					t.join();
				}
			}
			logger.warn("所有子线程运行结束");
			processed = Thread_List.get(thread_num - 1).get_processed();		// 返回最后一个线程处理的processed
			/* 串行执行 */
//			processed = ImageProcessor.process(key, sortedList, processedImageDir, existing);

			//update last processed
			if(processed != null){
				logger.warn(" processed != null ");
				state.update(processed);
			}
			return processed;
		}}, Encoders.bean(VideoEventData.class), Encoders.bean(VideoEventData.class));
	//start
	 StreamingQuery query = processedDataset.writeStream()
		      .outputMode("update")
		      .format("console")
		      .start();
	 logger.warn("start");
	 //await
     query.awaitTermination();
     logger.warn("awit");
	}
}


