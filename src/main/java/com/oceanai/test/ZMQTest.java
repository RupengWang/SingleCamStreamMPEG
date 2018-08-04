package com.oceanai.test;

import com.oceanai.model.SearchFeature;
import com.oceanai.util.FaceZmqTool;
import com.oceanai.util.ImageUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Base64;
import java.util.List;

public class ZMQTest {
    public static void main(String[] args) throws Exception {
        //  Socket to talk to server
        System.out.println("Connecting to server");
        FaceZmqTool faceZmqTool = FaceZmqTool.getInstance();
        faceZmqTool.detectInit("tcp://192.168.1.11:5559");
        BufferedImage bufferedImage = ImageIO.read(new File("D:\\OceanAI\\resources\\images\\2.jpg"));
        byte[] bytes = ImageUtils.imageToBytes(bufferedImage, "jpg");
        Base64.Encoder encoder = Base64.getEncoder();
        String base64 = encoder.encodeToString(bytes);

        List<SearchFeature> searchFeatures = faceZmqTool.detect(base64);

        System.out.println(searchFeatures.size());
    }
}
