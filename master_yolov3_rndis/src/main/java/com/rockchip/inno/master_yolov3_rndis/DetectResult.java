package com.rockchip.inno.master_yolov3_rndis;

import org.opencv.core.Point;
import org.opencv.core.Scalar;

/**
 * Created by cxh on 2019/8/21
 * E-mail: shon.chen@rock-chips.com
 */
public class DetectResult {
    private static final String[] CLASSES = {"person", "bicycle", "car", "motorbike ", "aeroplane ", "bus ", "train", "truck ", "boat", "traffic light",
            "fire hydrant", "stop sign ", "parking meter", "bench", "bird", "cat", "dog ", "horse ", "sheep", "cow",
            "elephant", "bear", "zebra ", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee",
            "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard",
            "tennis racket", "bottle", "wine glass", "cup", "fork", "knife ", "spoon", "bowl", "banana", "apple",
            "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza ", "donut", "cake", "chair", "sofa",
            "pottedplant", "bed", "diningtable", "toilet ", "tvmonitor", "laptop  ", "mouse    ", "remote ", "keyboard ",
            "cell phone", "microwave ", "oven ", "toaster", "sink", "refrigerator ", "book", "clock", "vase",
            "scissors ", "teddy bear ", "hair drier", "toothbrush "};

    float x = 0;
    float y = 0;
    float width = 0;
    float height = 0;
    float scores = 0;
    int classes = 0;

    org.opencv.core.Point pt1 = new Point();
    org.opencv.core.Point textPt = new Point();
    org.opencv.core.Point pt2 = new Point();
    org.opencv.core.Scalar color = new Scalar(0, 0, 255);

    public Scalar getColor() {
        return color;
    }

    public void initPoint(int width, int height) {
        pt1.x = this.x * width;                //top
        pt1.y = this.y * height;               //left
        pt2.x = pt1.x + this.width * width;        //right
        pt2.y = pt1.y + this.height * height;       //bottom
        textPt = pt1.clone();
        textPt.x = textPt.x - 6;
        textPt.y = textPt.y - 10;
//        Log.d("222222222",
//                "x = " + x
//                        + "    y = " + y
//                        + "    this.width =" + this.width
//                        + "    this.height = " + this.height);
//        Log.d("33333",
//                "pt1.x  = " + pt1.x
//                        + "    pt1.y " + pt1.y
//                        + "    pt2.x =" + pt2.x
//                        + "    pt2.y = " + pt2.y);
    }

    public Point getPt1() {
        return pt1;
    }

    public Point getTextPt() {
        return textPt;
    }

    public Point getPt2() {
        return pt2;
    }

    DetectResult(float[] boxes, float scores, long classes) {
        x = boxes[0];
        y = boxes[1];
        width = boxes[2];
        height = boxes[3];
        this.scores = scores;
        this.classes = (int) classes;

    }

    public float getScores() {
        return scores;
    }

    public String getClassesName() {
        return classes < CLASSES.length ? CLASSES[classes] : "";
    }

    @Override
    public String toString() {
        return "DetectResult{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", scores=" + scores +
                ", classes=" + classes +
                '}';
    }
}

