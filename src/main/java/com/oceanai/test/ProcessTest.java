package com.oceanai.test;

import com.oceanai.util.ImageUtils;

import java.awt.*;
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
        Graphics2D graphics2D;
        int count = 0;
        long start;
        try {
            while (running) {
                start = System.currentTimeMillis();
                bufferedImage = bufferedImages.take();
                graphics2D = bufferedImage.createGraphics();
                graphics2D.setColor(Color.RED);
                graphics2D.setStroke(new BasicStroke(4));
                //graphics2D.draw(new Rectangle());
                graphics2D.drawRect(100, 100, 300, 200);
                ImageUtils.saveToFile(bufferedImage, "D:\\OceanAI\\gitlab\\SingleCamStreamMPEG\\resources\\", "" + count++, "jpg");
                System.out.println("Time used " + (System.currentTimeMillis() - start) + "ms" + " remaining " + bufferedImages.size());
                graphics2D.dispose();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
