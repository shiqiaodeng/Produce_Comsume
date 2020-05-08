package com.iot.video.app.spark.processor;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;

import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.iot.video.app.spark.util.VideoEventData;

/**
 * Class to extract frames from video using OpenCV library and process using TensorFlow.
 * 
 * @author abaghel
 *
 */
public class ImageProcessor implements Serializable {	
	private static final Logger logger = Logger.getLogger(ImageProcessor.class);	
	
	//load native lib
	static {
		 System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	/**
	 * Method to process images
	 * 
	 * @param camId camera Id
	 * @param frames list of VideoEventData
	 * @param outputDir directory to save image files
	 * @return last processed VideoEventData 
	 * @throws Exception
	 */
	public static VideoEventData process(String camId, ArrayList<VideoEventData> sortedList, String outputDir,
										 VideoEventData previousProcessedEventData) throws Exception {
		VideoEventData currentProcessedEventData = new VideoEventData();
		Mat frame = null;
		double imageWidth = 640;
		double imageHeight = 480;
		Size sz = new Size(imageWidth, imageHeight);
		int frameCount = 0;
		//iterate and classify every 10th frame
		try{
			for (VideoEventData eventData : sortedList) {
				frame = getMat(eventData);
				Imgproc.resize(frame, frame, sz);
				frameCount++;
				if(frameCount == 10){
					MatOfByte bytemat = new MatOfByte();
					Imgcodecs.imencode(".jpg", frame, bytemat);
					byte[] bytes = bytemat.toArray();
					String match = ImageClassifier.classifyImage(bytes);
					saveImageAndData(frame, eventData, match, outputDir);
					frameCount = 0;
				}
				currentProcessedEventData = eventData;
			}
		}catch(Exception e){
			logger.warn(" 分类错误 ");
			e.printStackTrace();
		}

		return currentProcessedEventData;
	}
	
	//Get Mat from byte[]
	private static Mat getMat(VideoEventData ed) throws Exception{
		 Mat mat = new Mat(ed.getRows(), ed.getCols(), ed.getType());
		 mat.put(0, 0, Base64.getDecoder().decode(ed.getData()));   
		 return mat;
	}
	
	//Save image file
	private static void saveImageAndData(Mat mat, VideoEventData ed, String match, String outputDir) throws IOException{
		String imagePath = outputDir+ed.getCameraId()+"-T-"+ed.getTimestamp().getTime()+".jpg";
		logger.warn("Saving images to "+imagePath);
		boolean result = Imgcodecs.imwrite(imagePath, mat);
		if(!result){
			logger.error("Couldn't save images to path "+outputDir+".Please check if this path exists. This is configured in processed.output.dir key of property file.");
		}
		String matchPath = outputDir+ed.getCameraId()+"-T-"+ed.getTimestamp().getTime()+".txt";
		logger.warn("Saving classification result to "+imagePath);
		Files.write(Paths.get(matchPath), match.getBytes());
	}
}
