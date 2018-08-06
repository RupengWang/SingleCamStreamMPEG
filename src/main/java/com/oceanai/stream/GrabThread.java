package com.oceanai.stream;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class GrabThread implements Runnable{
    private Logger logger = Logger.getLogger(GrabThread.class.getName());
    private FFmpegFrameGrabber grabber;
    private Java2DFrameConverter converter = new Java2DFrameConverter();
    private boolean running = false;
    private BlockingQueue<BufferedImage> bufferedImages;
    private int width, height;

    private GrabThread() {}

    /**
     *  抓图线程的构造函数
     * @param rtspURL 抓图的rtsp地址
     * @param bufferedImages 抓图缓存队列
     * @param width 抓图resize的宽度
     * @param height 抓图resize的高度
     */
    public GrabThread(String rtspURL, BlockingQueue<BufferedImage> bufferedImages, int width, int height) {
        this.bufferedImages = bufferedImages;
        this.width = width;
        this.height = height;
        grabber = new FFmpegFrameGrabber(rtspURL);
        grabber.setOption("rtsp_transport", "tcp");
        try {
            grabber.start();
            running = true;
        } catch (FrameGrabber.Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 停止grab线程
     */
    public void stop() {
        this.running = false;
    }


    @Override
    public void run() {
        Frame frame;
        int count = 0;
        BufferedImage bufferedImage;
        long start;
        logger.info("Start to grab frame");
        try {
            while (running) {
                start = System.currentTimeMillis();
                if ((frame = grabber.grabImage()) == null) {
                    continue;
                }
                if (bufferedImages == null) {
                    logger.info("BufferedImageList hasn't been init!");
                    throw new NullPointerException();
                }
                //对大图进行resize，可以提高处理效率
                bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                Graphics graphics = bufferedImage.getGraphics();
                graphics.drawImage(converter.getBufferedImage(frame), 0, 0, width, height, null);

                bufferedImages.put(bufferedImage);
                logger.info("Grab image " + count++  + " , time used " + (System.currentTimeMillis() - start) + " ms.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
