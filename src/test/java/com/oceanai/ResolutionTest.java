package com.oceanai;

import com.google.gson.internal.bind.MapTypeAdapterFactory;
import com.oceanai.util.ImageUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.util.Map;

public class ResolutionTest {
    public static void main(String[] args) throws Exception{
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("rtsp://admin:123456@192.168.1.69:554/h264/ch1/main/av_stream");
        grabber.setOption("rtsp_transport", "tcp");
        grabber.start();
        System.out.println(grabber.getImageWidth() + " " + grabber.getImageHeight());
        Frame frame = grabber.grabImage();
        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage bufferedImage = converter.convert(frame);
        ImageUtils.saveToFile(bufferedImage, "D:\\OceanAI\\gitlab\\SingleCamStreamMPEG\\resources\\", "test", "jpg");
        grabber.setImageWidth(1080);
        grabber.setImageHeight(720);
        System.out.println(grabber.getImageWidth() + " " + grabber.getImageHeight());
        frame = grabber.grabImage();
        converter = new Java2DFrameConverter();
        bufferedImage = converter.convert(frame);
        ImageUtils.saveToFile(bufferedImage, "D:\\OceanAI\\gitlab\\SingleCamStreamMPEG\\resources\\", "test1", "jpg");


    }
}
