package com.oceanai;

import com.oceanai.stream.GrabThread;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class StreamTest {
    public static void main(String[] args) {
        if (args.length < 1) {
            return;
        }
        String rtspURL = args[0];
        BlockingQueue<BufferedImage> bufferedImages = new LinkedBlockingDeque<>();
        int width = 1280;
        int height = 720;
        Thread grab = new Thread(new GrabThread(rtspURL, bufferedImages, width, height));
        Thread process = new Thread(new ProcessTest(bufferedImages));
        process.start();
        grab.start();
    }
}
