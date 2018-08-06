package com.oceanai;

import com.oceanai.util.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ImageProcessor {
    public BufferedImage process(BufferedImage bufferedImage) {
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setColor(Color.RED);
        graphics2D.setStroke(new BasicStroke(4));
        //graphics2D.draw(new Rectangle());
        graphics2D.drawRect(100, 100, 300, 200);
        graphics2D.dispose();
        return bufferedImage;
    }
}
