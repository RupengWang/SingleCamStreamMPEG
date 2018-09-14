package com.oceanai;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.*;

public class OpencvGrabberTest {
    public static void main(String[] args) throws Exception {
        FrameGrabber grabber = new FFmpegFrameGrabber("rtsp://admin:iec123456@192.168.1.71:554/h264/ch1/stream3/av_stream");
        grabber.start();
        opencv_core.IplImage frame;
        int count = 0;

        while (grabber.grab() != null) {
            System.out.println("Grabber one frame, total " + count++);
        }
    }
}
