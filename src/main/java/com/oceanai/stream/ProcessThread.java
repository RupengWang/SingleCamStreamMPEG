package com.oceanai.stream;

import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import com.oceanai.model.SearchFeature;
import com.oceanai.util.FaceZmqTool;
import com.oceanai.util.ImageUtils;
import georegression.struct.shapes.Quadrilateral_F64;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class ProcessThread implements Runnable {

    private Logger logger = Logger.getLogger(ProcessThread.class.getName());
    private Base64.Encoder encoder = Base64.getEncoder();
    private ImageType<GrayU8> imageType ;
    private Quadrilateral_F64[] locations;
    private TrackerObjectQuad[] trackers;
    private GrayU8 currentBoof;
    private FaceZmqTool faceZmqTool = FaceZmqTool.getInstance();
    private BlockingQueue<BufferedImage> bufferedImages;
    private BlockingQueue<BufferedImage> processedImages;
    private FFmpegFrameRecorder frameRecorder;
    private boolean running = false;

    private ProcessThread(){}
    public ProcessThread(BlockingQueue<BufferedImage> bufferedImages,BlockingQueue<BufferedImage> recordImages) {
        this.bufferedImages = bufferedImages;
        this.processedImages = recordImages;
        faceZmqTool.detectInit("tcp://192.168.1.11:5559");
        imageType = FactoryTrackerObjectQuad.circulant(null, GrayU8.class).getImageType();
        running = true;
    }

    public void start() {
        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    @Override
    public void run() {
        BufferedImage bufferedImage;
        Graphics2D graphics2D;
        byte[] bytes;
        Rectangle box;
        long start;
        int count = 0;
        List<SearchFeature> searchFeatureList = new ArrayList<>(0);
        boolean trackWell = true;
        logger.info("Start to process frame");
        try {
            while (running) {
                start = System.currentTimeMillis();
                try {
                    Thread.sleep((long) 0.5);//先释放资源，避免cpu占用过高
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                bufferedImage = bufferedImages.take();
                if (count % 25 == 0) {
                    graphics2D = bufferedImage.createGraphics();
                    bytes = ImageUtils.imageToBytes(bufferedImage, "jpg");
                    searchFeatureList = faceZmqTool.detect(encoder.encodeToString(bytes));
                    if (!searchFeatureList.isEmpty()) {
                        faceTrackingInit(bufferedImage, searchFeatureList);
                        for (int j = 0; j < searchFeatureList.size(); j++) {
                            SearchFeature.BBox bbox = searchFeatureList.get(j).bbox;
                            box = new Rectangle(bbox.left_top.x, bbox.left_top.y, bbox.right_down.x - bbox.left_top.x, bbox.right_down.y - bbox.left_top.y);
                            draw(graphics2D, box, Color.YELLOW);
                        }
                        logger.info("Detect " + searchFeatureList.size() + " faces from frame " + count + " time used " + (System.currentTimeMillis() - start) + " remaining " + bufferedImages.size());
                        //ImageUtils.saveToFile(bufferedImage, "D:\\OceanAI\\gitlab\\SingleCamStreamMPEG\\resources\\", "" + count, "jpg");
                        //graphics2D.dispose();
                    }
                    ++count;
                } else {
                    if (!searchFeatureList.isEmpty()) {
                        graphics2D = bufferedImage.createGraphics();
                        ConvertBufferedImage.convertFrom(bufferedImage, currentBoof, true);
                        int status = 0;
                        for (int n = 0; n < searchFeatureList.size(); n++) {
                            if (trackers[n].process(currentBoof, locations[n])) {
                                status++;
                            }
                            box = new Rectangle((int) locations[n].getA().getX(), (int) locations[n].getA().getY(), (int) (locations[n].getC().getX() - locations[n].getA().getX()), (int) (locations[n].getC().getY() - locations[n].getA().getY()));
                            draw(graphics2D, box, Color.RED);
                        }
                        if (status == searchFeatureList.size()) {
                            trackWell = true;
                        } else {
                            trackWell = false;
                        }

                        //logger.info("Track one frame " + count + " time used " + (System.currentTimeMillis() - start));
                        //ImageUtils.saveToFile(bufferedImage, "D:\\OceanAI\\gitlab\\SingleCamStreamMPEG\\resources\\", "" + count, "jpg");
                        //graphics.dispose();
                        //graphics2D.dispose();
                    }
                    ++count;
                }
                /*graphics2D = bufferedImage.createGraphics();
                graphics2D.setColor(Color.RED);
                graphics2D.setStroke(new BasicStroke(4));
                //graphics2D.draw(new Rectangle());
                graphics2D.drawRect(100, 100, 300, 200);
                ImageUtils.saveToFile(bufferedImage, "D:\\OceanAI\\gitlab\\SingleCamStreamMPEG\\resources\\", "" + count++, "jpg");*/
                processedImages.put(bufferedImage);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 初始化追踪器，追踪器个数与人脸树相同
     * @param bufferedImage 图片对象
     * @param searchFeatures 检测到的人脸信息
     */
    private void faceTrackingInit(BufferedImage bufferedImage, List<SearchFeature> searchFeatures) {
        logger.info("Tracker start to init.");
        currentBoof = imageType.createImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        ConvertBufferedImage.convertFrom(bufferedImage, currentBoof, true);
        locations = new Quadrilateral_F64[searchFeatures.size()];
        trackers = new TrackerObjectQuad[searchFeatures.size()];

        for (int i = 0; i < searchFeatures.size(); i++) {
            trackers[i] = FactoryTrackerObjectQuad.circulant(null, GrayU8.class);
            SearchFeature.BBox bbox = searchFeatures.get(i).bbox;
            locations[i] = new Quadrilateral_F64(bbox.left_top.x, bbox.left_top.y, bbox.right_down.x, bbox.left_top.y, bbox.right_down.x, bbox.right_down.y, bbox.left_top.x, bbox.right_down.y);
            trackers[i].initialize(currentBoof, locations[i]);
        }
    }

    /**
     * 绘制矩形框
     * @param graphics2D
     * @param box
     */
    public void draw(Graphics2D graphics2D, Rectangle box, Color color) {
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

}
