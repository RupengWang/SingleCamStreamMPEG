package com.oceanai;

import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import com.oceanai.model.SearchFeature;
import com.oceanai.util.*;
import georegression.struct.shapes.Quadrilateral_F64;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

public class Streamer implements Runnable{
    private Logger logger = Logger.getLogger(Streamer.class.getName());
    private ImageType<GrayU8> imageType ;
    private Quadrilateral_F64[] locations;
    private TrackerObjectQuad[] trackers;
    private GrayU8 currentBoof;
    private FFmpegFrameGrabber grabber;
    private FFmpegFrameRecorder frameRecoder;
    private FaceZmqTool faceZmqTool = FaceZmqTool.getInstance();
    private Java2DFrameConverter converter = new Java2DFrameConverter();
    private Base64.Encoder encoder = Base64.getEncoder();
    private int width, height;
    private int bitrate;
    private int minFace = 20;
    private boolean running = false;
    public Streamer(String rtspURL, int width, int height) {
        this.width = width;
        this.height = height;
        imageType = FactoryTrackerObjectQuad.circulant(null, GrayU8.class).getImageType();
        faceZmqTool.detectInit("tcp://192.168.1.11:5559");
        grabber = new FFmpegFrameGrabber(rtspURL);
        frameRecoder = new FFmpegFrameRecorder("http://192.168.1.11:8081/test", 0);
        grabber.setOption("rtsp_transport", "tcp");

        frameRecoder.setVideoCodecName("mpeg1video");
        frameRecoder.setFormat("mpegts");
        frameRecoder.setImageWidth(width);
        frameRecoder.setImageHeight(height);
        //frameRecoder.setVideoBitrate(10000);
        frameRecoder.setVideoQuality(0.8);
        frameRecoder.setFrameRate(25);
    }

    public void start() {
        try {
            running = true;
            grabber.start();
            frameRecoder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return running;
    }
    @Override
    public void run() {
        Frame frame;
        int count = 0;
        List<SearchFeature> searchFeatureList = new ArrayList<>(0);
        //BufferedImage bi;
        BufferedImage bufferedImage ;
        Graphics2D graphics2D;
        Graphics graphics;
        byte[] bytes;
        Rectangle box;
        long start;
        //BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        //System.out.println("Start grab frame");
        logger.info("Start grab frame");
        try {
            while (running) {
                start = System.currentTimeMillis();
                if ((frame = grabber.grabImage()) == null) {
                    continue;
                }

                if (count % 25 == 0) {
                    bufferedImage = converter.convert(frame);
                    /*bi = converter.convert(frame);
                    graphics = bufferedImage.getGraphics();
                    graphics.drawImage(bi, 0, 0, width, height, null);*/
                    graphics2D = bufferedImage.createGraphics();

                    bytes = ImageUtils.imageToBytes(bufferedImage, "jpg");

                    searchFeatureList = faceZmqTool.detect(encoder.encodeToString(bytes), minFace);
                    if (!searchFeatureList.isEmpty()) {
                        faceTrackingInit(bufferedImage, searchFeatureList);
                        for (int j = 0; j < searchFeatureList.size(); j++) {
                            SearchFeature.BBox bbox = searchFeatureList.get(j).bbox;
                            box = new Rectangle(bbox.left_top.x, bbox.left_top.y, bbox.right_down.x - bbox.left_top.x, bbox.right_down.y - bbox.left_top.y);
                            draw(graphics2D, box, Color.YELLOW);
                        }
                        frame = converter.convert(bufferedImage);
                        //ImageUtils.saveToFile(bufferedImage, "D:\\OceanAI\\gitlab\\RealtimeStream\\resources\\", "" + count, "jpg");
                        //System.out.println("Detect faces from frame " + count + " time used " + (System.currentTimeMillis() - start));
                        logger.info("Detect faces from frame " + count + " time used " + (System.currentTimeMillis() - start));
                        ImageUtils.saveToFile(bufferedImage, "D:\\OceanAI\\gitlab\\SingleCamStreamMPEG\\resources\\", "" + count, "jpg");
                        graphics2D.dispose();
                        //graphics.dispose();
                    }
                    ++count;
                } else {
                    if (!searchFeatureList.isEmpty()) {
                        /*bi = converter.convert(frame);
                        graphics = bufferedImage.getGraphics();
                        graphics.drawImage(bi, 0, 0, width, height, null);*/
                        bufferedImage = converter.convert(frame);
                        graphics2D = bufferedImage.createGraphics();

                        ConvertBufferedImage.convertFrom(bufferedImage, currentBoof, true);
                        for (int n = 0; n < searchFeatureList.size(); n++) {
                            trackers[n].process(currentBoof, locations[n]);
                            box = new Rectangle((int) locations[n].getA().getX(), (int) locations[n].getA().getY(), (int) (locations[n].getC().getX() - locations[n].getA().getX()), (int) (locations[n].getC().getY() - locations[n].getA().getY()));
                            draw(graphics2D, box, Color.YELLOW);
                        }
                        //ImageUtils.saveToFile(bufferedImage, "D:\\OceanAI\\gitlab\\RealtimeStream\\resources\\", "" + count, "jpg");

                        frame = converter.convert(bufferedImage);
                        //System.out.println("Track one frame " + count + " time used " + (System.currentTimeMillis() - start));
                        logger.info("Track one frame " + count + " time used " + (System.currentTimeMillis() - start));
                        ImageUtils.saveToFile(bufferedImage, "D:\\OceanAI\\gitlab\\SingleCamStreamMPEG\\resources\\", "" + count, "jpg");
                        //graphics.dispose();
                        graphics2D.dispose();
                    }
                    ++count;
                }
                //Thread.sleep(40);

                //frameRecoder.record(frame);
                //System.out.println("Record frame " + count + " time used " + (System.currentTimeMillis() - start));
                //System.out.print("is running " + running);
            }
            grabber.stop();
            frameRecoder.stop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            running = false;
        }
    }

    public boolean stop() {
        running = false;
        return true;
    }

    /**
     * 初始化追踪器，追踪器个数与人脸树相同
     * @param bufferedImage 图片对象
     * @param searchFeatures 检测到的人脸信息
     */
    private void faceTrackingInit(BufferedImage bufferedImage, List<SearchFeature> searchFeatures) {
        BufferedImage currentImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage
                .TYPE_3BYTE_BGR);
        //logger.log(FileLogger.TYPE.INFO, "Tracker start to init.");
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


    private GrayU8 convert(BufferedImage currentImage, GrayU8 currentBoof, BufferedImage next) {
        currentImage.createGraphics().drawImage(next, 0, 0, null);
        ConvertBufferedImage.convertFrom(currentImage, currentBoof, true);
        return currentBoof;
    }

    public static void main(String[] args) {
        Streamer streamer = new Streamer("rtsp://admin:123456@192.168.1.69:554/unicast/c1/s0/live", 1920, 1080);
        streamer.start();
        new Thread(streamer).start();
        //Thread.sleep(40000);
        //streamer.stop();
    }
}
