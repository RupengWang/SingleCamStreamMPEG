package com.oceanai.main;

import com.oceanai.stream.GrabThread;
import com.oceanai.stream.ProcessThread;
import com.oceanai.stream.RecorderThread;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class StreamMain {
    public static void main(String[] args) {
        if (args.length < 2) {
            return;
        }
        String rtspURL = args[0];
        String recordURL = args[1];
        BlockingQueue<BufferedImage> bufferedImages = new LinkedBlockingDeque<>();
        BlockingQueue<BufferedImage> recordIamges = new LinkedBlockingDeque<>();
        int width = 1920;
        int height = 1080;
        Thread grab = new Thread(new GrabThread(rtspURL, bufferedImages, width, height));
        Thread process = new Thread(new ProcessThread(bufferedImages, recordIamges));
        //Thread record = new Thread(new RecorderThread(recordURL, recordIamges, width, height));
        //record.start();
        process.start();
        grab.start();
    }
}
