package com.oceanai;

import com.oceanai.model.SearchFeature;
import com.oceanai.util.FaceZmqTool;
import com.oceanai.util.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

import static com.oceanai.util.ImageUtils.draw;

public class BoxTest {

    /**
     * 绘制矩形框
     * @param graphics2D
     * @param box
     */
    public static void draw(Graphics2D graphics2D, Rectangle box, Color color) {

        graphics2D.setColor(color);
        Point2D point2DA = new Point((int)box.getX(), (int)box.getY());
        Point2D point2DB = new Point((int)(box.getX() + box.getWidth()), (int)box.getY());
        Point2D point2DC = new Point((int)(box.getX() + box.getWidth()), (int)(box.getY() + box.getHeight()));
        Point2D point2DD = new Point((int)box.getX(), (int)(box.getY() + box.getHeight()));

        double width = box.getWidth();
        double height = box.getHeight();

        Line2D lineA_1 = new Line2D.Double(point2DA.getX(),point2DA.getY(),point2DA.getX()+width/4, point2DA.getY());
        Line2D lineA_2 = new Line2D.Double(point2DA.getX(), point2DA.getY(), point2DA.getX(), point2DA.getY() + height/4);
        Line2D lineB_1 = new Line2D.Double(point2DB.getX(), point2DB.getY(), point2DB.getX() - width/4, point2DA.getY());
        Line2D lineB_2 = new Line2D.Double(point2DB.getX(), point2DB.getY(), point2DB.getX(), point2DA.getY() + height/4);
        Line2D lineC_1 = new Line2D.Double(point2DC.getX(), point2DC.getY(), point2DC.getX(), point2DC.getY() - height/4);
        Line2D lineC_2 = new Line2D.Double(point2DC.getX(), point2DC.getY(), point2DC.getX() - width/4, point2DC.getY());
        Line2D lineD_1 = new Line2D.Double(point2DD.getX(), point2DD.getY(), point2DD.getX() + width/4, point2DD.getY());
        Line2D lineD_2 = new Line2D.Double(point2DD.getX(), point2DD.getY(), point2DD.getX(), point2DD.getY() - height/4);

        graphics2D.setStroke(new BasicStroke(1));
        graphics2D.draw(box);
        graphics2D.setStroke(new BasicStroke(4));
        graphics2D.draw(lineA_1);
        graphics2D.draw(lineA_2);
        graphics2D.draw(lineB_1);
        graphics2D.draw(lineB_2);
        graphics2D.draw(lineC_1);
        graphics2D.draw(lineC_2);
        graphics2D.draw(lineD_1);
        graphics2D.draw(lineD_2);
    }

    public static void main(String[] args) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File("D:\\OceanAI\\gitlab\\SingleCamStreamMPEG\\src\\test\\resources\\test.jpg"));
        FaceZmqTool faceZmqTool = FaceZmqTool.getInstance();
        byte[] bytes = ImageUtils.imageToBytes(bufferedImage, "jpg");
        Base64.Encoder encoder = Base64.getEncoder();
        String base64 = encoder.encodeToString(bytes);
        faceZmqTool.detectInit("tcp://192.168.1.11:5559");
        List<SearchFeature> searchFeatureList = faceZmqTool.detect(base64, 5);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        Rectangle box;
        for (int j = 0; j < searchFeatureList.size(); j++) {
            SearchFeature.BBox bbox = searchFeatureList.get(j).bbox;
            box = new Rectangle(bbox.left_top.x, bbox.left_top.y, bbox.right_down.x - bbox.left_top.x, bbox.right_down.y - bbox.left_top.y);
            draw(graphics2D, box, Color.BLUE);
        }
        graphics2D.dispose();
        ImageUtils.saveToFile(bufferedImage, "D:\\OceanAI\\gitlab\\SingleCamStreamMPEG\\src\\test\\resources\\", "boxed", "jpg");
    }
}
