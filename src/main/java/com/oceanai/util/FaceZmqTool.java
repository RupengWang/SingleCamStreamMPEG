package com.oceanai.util;

import com.google.gson.Gson;
import com.oceanai.model.DetectResult;
import com.oceanai.model.FaceFeature;
import com.oceanai.model.SearchFeature;
import org.codehaus.jettison.json.JSONObject;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.ArrayList;
import java.util.List;

public class FaceZmqTool {
    private ZContext context = new ZContext();
    private  ZMQ.Socket socket;
    private Gson gson = new Gson();
    private int minFace;
    private FaceZmqTool() {}
    private static class FaceZmpToolManager {
        private static final FaceZmqTool instance = new FaceZmqTool();
    }

    /**
     * 只有唯一一个实例
     * @return
     */
    public static FaceZmqTool getInstance() {
        return FaceZmpToolManager.instance;
    }

    //初始化服务地址
    public boolean detectInit(String url) {
        socket = context.createSocket(ZMQ.DEALER);
        return socket.connect(url);
    }

    /**
     * 发送json请求
     * @param base64
     * @param minFace
     * @return
     */
    private boolean send(String base64, int minFace) {
        try {
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("api_key", "");
            jsonParam.put("interface", "5");
            jsonParam.put("image_base64", base64);
            jsonParam.put("minface_size", minFace);
            return socket.send(jsonParam.toString().getBytes(ZMQ.CHARSET), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 接收处理结果
     * @return
     */
    private String receiveResult() {
        byte[] reply = socket.recv(0);
        String result = new String(reply, ZMQ.CHARSET);
        return result.replace("\u0000", "");
    }


    public List<SearchFeature> detect(String base64, int minFace) {
        if (socket == null) {
            return null;
        }
        boolean sendResult = send(base64, minFace);
        if (sendResult) {
            String result = receiveResult();

            DetectResult detectResult = gson.fromJson(result, DetectResult.class);
            List<SearchFeature> searchFeatures = new ArrayList<>(detectResult.getFace_nums());
            for (int i = 0;i < detectResult.getFace_nums();++i) {
                FaceFeature faceFeature = detectResult.getResult()[i];
                int x1 = faceFeature.getLeft();
                int y1 = faceFeature.getTop();
                int x2 = x1 + faceFeature.getWidth();
                int y2 = y1 + faceFeature.getHeight();
                searchFeatures.add(new SearchFeature(x1, y1, x2, y2, faceFeature.getScore(), faceFeature.getQuality(), faceFeature.getSideFace() , faceFeature.getLandmark()));
            }
            return searchFeatures;
        } else {
            return null;
        }
    }
}
