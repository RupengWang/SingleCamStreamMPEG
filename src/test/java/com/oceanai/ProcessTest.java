package com.oceanai;

import com.oceanai.util.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;

public class ProcessTest implements Runnable {

    private boolean running = false;
    private BlockingQueue<BufferedImage> bufferedImages;

    public ProcessTest(BlockingQueue<BufferedImage> bufferedImages) {
        this.bufferedImages = bufferedImages;

        running = true;

    }



    @Override
    public void run() {
        BufferedImage bufferedImage;
        ImageProcessor imageProcessor;
        imageProcessor = new ImageProcessor();
        int count = 0;
        long start;
        try {
            while (running) {
                start = System.currentTimeMillis();
                bufferedImage = bufferedImages.take();
                bufferedImage = imageProcessor.process(bufferedImage);
                ImageUtils.saveToFile(bufferedImage, "D:\\OceanAI\\gitlab\\SingleCamStreamMPEG\\resources\\", "" + count++, "jpg");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
