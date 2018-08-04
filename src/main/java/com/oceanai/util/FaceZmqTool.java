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
    private FaceZmqTool() {}
    private static class FaceZmpToolManager {
        private static final FaceZmqTool instance = new FaceZmqTool();
    }
    public static FaceZmqTool getInstance() {
        return FaceZmpToolManager.instance;
    }

    public boolean detectInit(String url) {
        socket = context.createSocket(ZMQ.DEALER);
        return socket.connect(url);
    }

    private boolean send(String base64) {
        try {
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("api_key", "");
            jsonParam.put("interface", "5");
            jsonParam.put("image_base64", base64);
            return socket.send(jsonParam.toString().getBytes(ZMQ.CHARSET), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String receiveResult() {
        byte[] reply = socket.recv(0);
        String result = new String(reply, ZMQ.CHARSET);
        return result.replace("\u0000", "");
    }

    public List<SearchFeature> detect(String base64) {
        boolean sendResult = send(base64);
        if (sendResult) {
            String result = receiveResult();
            DetectResult detectResult = gson.fromJson(result, DetectResult.class);
            FaceFeature[] faceFeatures = detectResult.getResult();
            List<SearchFeature> searchFeatures = new ArrayList<>(detectResult.getFace_nums());
            for (int i = 0;i < detectResult.getFace_nums();++i) {
                FaceFeature faceFeature = detectResult.getResult()[i];
                int x1 = faceFeature.getLeft();
                int y1 = faceFeature.getTop();
                int x2 = x1 + faceFeature.getWidth();
                int y2 = y1 + faceFeature.getHeight();
                searchFeatures.add(new SearchFeature(x1, y1, x2, y2, 0, faceFeature.getScore(), faceFeature.getLandmark()));
            }
            return searchFeatures;
        } else {
            return null;
        }
    }
}
