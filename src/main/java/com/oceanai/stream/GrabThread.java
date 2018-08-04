package com.oceanai.stream;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class GrabThread implements Runnable{
    private Logger logger = Logger.getLogger(GrabThread.class.getName());
    private FFmpegFrameGrabber grabber;
    private Java2DFrameConverter converter = new Java2DFrameConverter();
    private boolean running = false;
    private BlockingQueue<BufferedImage> bufferedImages;

    private GrabThread() {}

    public GrabThread(String rtspURL, BlockingQueue<BufferedImage> bufferedImages, int width, int height) {
        this.bufferedImages = bufferedImages;
        grabber = new FFmpegFrameGrabber(rtspURL);
        grabber.setOption("rtsp_transport", "tcp");
        try {
            grabber.start();
            running = true;
        } catch (FrameGrabber.Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void stop() {
        this.running = false;
    }


    @Override
    public void run() {
        Frame frame;
        int count = 0;
        //BufferedImage bufferedImage;
        long start;
        logger.info("Start to grab frame");
        try {
            while (running) {
                start = System.currentTimeMillis();
                try {
                    Thread.sleep((long) 0.5);//先释放资源，避免cpu占用过高
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if ((frame = grabber.grabImage()) == null) {
                    continue;
                }
                //bufferedImage = converter.convert(frame);
                if (bufferedImages == null) {
                    logger.info("BufferedImageList hasn't been init!");
                    throw new NullPointerException();
                }

                bufferedImages.put(converter.convert(frame));
                logger.info("Grab image " + count++  + " , time used " + (System.currentTimeMillis() - start) + " ms.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
