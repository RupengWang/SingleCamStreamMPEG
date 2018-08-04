package com.oceanai.stream;

import com.oceanai.util.ImageUtils;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class RecorderThread implements Runnable {

    private Logger logger = Logger.getLogger(RecorderThread.class.getName());
    private FFmpegFrameRecorder frameRecorder;
    private BlockingQueue<BufferedImage> bufferedImages;
    private Java2DFrameConverter converter = new Java2DFrameConverter();
    private boolean running = false;
    public RecorderThread(String recordURL, BlockingQueue<BufferedImage> bufferedImages, int width, int height) {
        this.bufferedImages = bufferedImages;
        try{
            frameRecorder = new FFmpegFrameRecorder(recordURL, 0);
            frameRecorder.setVideoCodecName("mpeg1video");
            frameRecorder.setFormat("mpegts");
            frameRecorder.setImageWidth(width);
            frameRecorder.setImageHeight(height);
            //frameRecoder.setVideoBitrate(10000);
            frameRecorder.setVideoQuality(0.8);
            frameRecorder.setFrameRate(25);
            frameRecorder.start();
            running = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void start() {
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        BufferedImage bufferedImage;
        long start;
        int count = 0;
        try {
            logger.info("Start to record frame.");
            while (running) {
                start = System.currentTimeMillis();
                try {
                    Thread.sleep((long) 0.5);//先释放资源，避免cpu占用过高
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                bufferedImage = bufferedImages.take();

                frameRecorder.record(converter.convert(bufferedImage));
                logger.info("Record one frame , time used " + (System.currentTimeMillis() - start) + "ms" + " remaining " + bufferedImages.size());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
