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

import static com.oceanai.util.ImageUtils.draw;

public class ProcessThread implements Runnable {

    private Logger logger = Logger.getLogger(ProcessThread.class.getName());
    private Base64.Encoder encoder = Base64.getEncoder();

    //追踪相关参数
    private ImageType<GrayU8> imageType ;
    //private Quadrilateral_F64[] locations;
    //private TrackerObjectQuad[] trackers;
    private GrayU8 currentBoof;

    private FaceZmqTool faceZmqTool = FaceZmqTool.getInstance(); //人脸检测(通过ZeroMQ调用人脸检测API)
    private BlockingQueue<BufferedImage> bufferedImages;
    private BlockingQueue<BufferedImage> processedImages;
    private int minFace;
    private boolean running = false;

    private ProcessThread(){}

    /**
     *  处理线程构造函数
     * @param bufferedImages 抓图缓冲队列
     * @param recordImages 推流缓冲队列
     * @param minFace 最小人脸检测尺寸
     */
    public ProcessThread(BlockingQueue<BufferedImage> bufferedImages,BlockingQueue<BufferedImage> recordImages, int minFace) {
        this.bufferedImages = bufferedImages;
        this.processedImages = recordImages;
        this.minFace = minFace;
        String ZMQ_ADDRESS = System.getenv("ZMQ_ADDRESS");
        if (ZMQ_ADDRESS == null || ZMQ_ADDRESS.equals("")) {
            ZMQ_ADDRESS = "tcp://192.168.1.11:5559";
        }
        faceZmqTool.detectInit(ZMQ_ADDRESS);
        imageType = FactoryTrackerObjectQuad.circulant(null, GrayU8.class).getImageType();
        running = true;
    }

    /**
     * 开启处理线程
     */
    public void start() {
        this.running = true;
    }

    /**
     * 停止处理线程
     */
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
        Color color = new Color(Integer.parseInt("21A4FF", 16));
        logger.info("Start to process frame");
        try {
            Quadrilateral_F64[] locations = null;
            TrackerObjectQuad[] trackers = null;
            while (running) {
                start = System.currentTimeMillis();
                try {
                    Thread.sleep((long) 0.5);//先释放资源，避免cpu占用过高
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                bufferedImage = bufferedImages.take();
                //每一秒(25帧)检测一次人脸，后面24帧使用追踪算法追踪
                if (count++ % 25 == 0) {
                    bytes = ImageUtils.imageToBytes(bufferedImage, "jpg");
                    searchFeatureList = faceZmqTool.detect(encoder.encodeToString(bytes), 80);
                    if (searchFeatureList == null) {
                        logger.info("FaceZmqTool hasn't been init!");
                        break;
                    }
                    if (!searchFeatureList.isEmpty()) {
                        locations = new Quadrilateral_F64[searchFeatureList.size()];
                        trackers = new TrackerObjectQuad[searchFeatureList.size()];
                        graphics2D = bufferedImage.createGraphics();
                        int size = searchFeatureList.size();
                        for (int j = 0; j < size; j++) {
                            SearchFeature searchFeature = searchFeatureList.get(j);
                            if (searchFeature.width < minFace || searchFeature.height < minFace) {
                                System.out.println("Face number is " + searchFeatureList.size());
                                //searchFeatureList.remove(j);
                                searchFeatureList.set(j, null);
                                System.out.println("Remove one face.");
                                System.out.println("Face number is " + searchFeatureList.size());
                                continue;
                            }
                            SearchFeature.BBox bbox = searchFeature.bbox;
                            box = new Rectangle(bbox.left_top.x, bbox.left_top.y, bbox.right_down.x - bbox.left_top.x, bbox.right_down.y - bbox.left_top.y);
                            draw(graphics2D, box, color);
                        }

                        faceTrackingInit(trackers, locations, bufferedImage, searchFeatureList);
                        logger.info("Detect " + searchFeatureList.size() + " faces from frame " + count + " time used " + (System.currentTimeMillis() - start) + " remaining " + bufferedImages.size());
                        graphics2D.dispose();
                    }
                } else {
                    if (!searchFeatureList.isEmpty() || locations == null || trackers == null) {
                        graphics2D = bufferedImage.createGraphics();
                        GrayU8 currentBoof = imageType.createImage(bufferedImage.getWidth(), bufferedImage.getHeight());
                        ConvertBufferedImage.convertFrom(bufferedImage, currentBoof, true);
                        for (int n = 0; n < searchFeatureList.size(); n++) {
                            if (searchFeatureList.get(n) == null) {
                                continue;
                            }
                            trackers[n].process(currentBoof, locations[n]); //内存泄漏
                            box = new Rectangle((int) locations[n].getA().getX(), (int) locations[n].getA().getY(), (int) (locations[n].getC().getX() - locations[n].getA().getX()), (int) (locations[n].getC().getY() - locations[n].getA().getY()));
                            draw(graphics2D, box, color);
                        }

                        logger.info("Track one frame " + count + " time used " + (System.currentTimeMillis() - start));
                        graphics2D.dispose();
                        currentBoof = null;
                    }
                }
                processedImages.offer(bufferedImage);
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
    private void faceTrackingInit(TrackerObjectQuad[] trackers, Quadrilateral_F64[] locations, BufferedImage bufferedImage, List<SearchFeature> searchFeatures) {
        logger.info("Tracker start to init.");
        GrayU8 currentBoof = imageType.createImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        ConvertBufferedImage.convertFrom(bufferedImage, currentBoof, true);
        /*locations = new Quadrilateral_F64[searchFeatures.size()];
        trackers = new TrackerObjectQuad[searchFeatures.size()];*/

        for (int i = 0; i < searchFeatures.size(); i++) {
            trackers[i] = FactoryTrackerObjectQuad.circulant(null, GrayU8.class);
            if (searchFeatures.get(i) == null) {
                trackers[i] = null;
                locations[i] = null;
            } else {
                SearchFeature.BBox bbox = searchFeatures.get(i).bbox;
                locations[i] = new Quadrilateral_F64(bbox.left_top.x, bbox.left_top.y, bbox.right_down.x, bbox.left_top.y, bbox.right_down.x, bbox.right_down.y, bbox.left_top.x, bbox.right_down.y);
                trackers[i].initialize(currentBoof, locations[i]);
            }
        }
        currentBoof = null;
    }
}
