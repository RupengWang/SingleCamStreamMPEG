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

    /**
     * 推流线程构造函数
     * @param recordURL 推流目的地址
     * @param bufferedImages
     * @param width 推流分辨率宽
     * @param height 推流分辨率高
     */
    public RecorderThread(String recordURL, BlockingQueue<BufferedImage> bufferedImages, int width, int height) {
        this.bufferedImages = bufferedImages;
        try{
            frameRecorder = new FFmpegFrameRecorder(recordURL, 0);
            frameRecorder.setVideoCodecName("mpeg1video"); //mpeg1编码,jsmpeg目前只支持mpeg1解码
            frameRecorder.setFormat("mpegts"); //
            frameRecorder.setImageWidth(width);
            frameRecorder.setImageHeight(height);
            frameRecorder.setVideoQuality(0.8); //图像质量
            frameRecorder.setFrameRate(25); //播放帧率
            frameRecorder.start();
            running = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 开启服务
     */
    public void start() {
        this.running = true;
    }

    /**
     * 停止服务
     */
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
