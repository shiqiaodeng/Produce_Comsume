package com.iot.video.app.spark.processor;

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
 * Thread test
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Class to consume incoming JSON messages from Kafka and process them using Spark Structured Streaming.
 *  
 * @author abaghel
 *
 */
public class VideoStreamProcessor {

	/**
     *  Thread test para
     */
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;
	private static final Long KEEP_ALIVE_TIME = 1L;
	

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
			//check previous state
			if (state.exists()) {
				existing = state.get();
			}
			/**
			 * Thread test program //classify image
			 */
			VideoEventData processed = null;
        /**
         * build ThreadPoolExecutor
         * 
         */


        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());

        List<Future<VideoEventData>> futureList = new ArrayList<>();
       
        ThreadImageProcessor callable = new ThreadImageProcessor(key,values,processedImageDir,existing); // bulid ImageProcessor thread
        
        for (int i = 0; i < 10; i++) {
            //submit
            Future<VideoEventData> future = executor.submit(callable);
           
            //return 
            futureList.add(future);
        }
        for (Future<VideoEventData> fut : futureList) {
            try {
				processed = fut.get();
				System.out.println("results:" + processed);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
		}
		
        //shutdown
		executor.shutdown();
			/**
			 * commit classify image
			 */
			//classify image
			//VideoEventData processed = ImageProcessor.process(key,values,processedImageDir,existing);
			
			//update last processed 
			if(processed != null){
				state.update(processed);
			}
			return processed;

		}}, Encoders.bean(VideoEventData.class), Encoders.bean(VideoEventData.class));

	//start
	 StreamingQuery query = processedDataset.writeStream()
		      .outputMode("update")
		      .format("console")
		      .start();
	 
	 //await
     query.awaitTermination();
	}
}


