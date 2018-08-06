package com.oceanai.main;

import com.oceanai.stream.GrabThread;
import com.oceanai.stream.ProcessThread;
import com.oceanai.stream.RecorderThread;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class StreamOriginMain {
    public static void main(String[] args) {
        if (args.length != 2) {
            return;
        }
        String rtspURL = args[0];
        String recordURL = args[1];
        BlockingQueue<BufferedImage> bufferedImages = new LinkedBlockingDeque<>();
        int width = 1280;
        int height = 720;

        //record origin frame directly
        Thread grab = new Thread(new GrabThread(rtspURL, bufferedImages, width, height));

        //you can add another thread to process the frame you grab from bufferedImages list.

        Thread record = new Thread(new RecorderThread(recordURL, bufferedImages, width, height));
        record.start();
        grab.start();
    }
}
