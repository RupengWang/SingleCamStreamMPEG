package com.oceanai.main;

import com.oceanai.stream.GrabThread;
import com.oceanai.stream.ProcessThread;
import com.oceanai.stream.RecorderThread;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class StreamMain {
    public static void main(String[] args) {
        if (args.length < 3) {
            return;
        }
        String rtspURL = args[0];
        String recordURL = args[1];
        int minFace = Integer.valueOf(args[2]);

        BlockingQueue<BufferedImage> bufferedImages = new LinkedBlockingDeque<>(100);
        BlockingQueue<BufferedImage> recordIamges = new LinkedBlockingDeque<>(100);
        int width = 1280;
        int height = 720;

        GrabThread grabThread = new GrabThread(rtspURL, bufferedImages, width, height);
        Point point = grabThread.start();
        if (point != null) {
            int widthFrame = point.x;
            int heightFrame = point.y;
            minFace = minFace * width * height / (widthFrame * heightFrame);
        }
        Thread grab = new Thread(grabThread, "Grabber");
        Thread process = new Thread(new ProcessThread(bufferedImages, recordIamges, minFace), "Processor");
        Thread record = new Thread(new RecorderThread(recordURL, recordIamges, width, height), "Recorder");

        record.start();
        process.start();
        grab.start();
    }
}
