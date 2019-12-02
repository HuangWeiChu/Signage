package com.example.signage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.FileOutputStream;

public class SecondView extends Activity {

    int idcx,idcy;
    String fileOriginal = "//sdcard//DCIM//capture.jpg"; // 照片保存路徑
    String fileTransfer = "//sdcard//DCIM//captureFix.jpg"; // 照片保存路徑

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("<PAGE>", "SecondView");
        super.onCreate(savedInstanceState);
        //執行臉部偵測的部分and裁切(?)
        setContentView(new FaceView(this));
        getWindow().setBackgroundDrawableResource(R.drawable.background);
    }

    public class FaceView extends View {
        int imageWidth, imageHeight;
        int numberOfFace = 10;
        //自訂能偵測出的臉孔數目
        FaceDetector myFaceDetect;
        FaceDetector.Face[] myFace;
        float myEyesDistance;
        int numberOfFaceDetected;
        public AlertDialog prsd;
        Bitmap myBitmap;
        int dis_s,dis_t;
        int r_new =0;
        int s_new =0;

        public FaceView(Context context) {
            super(context);

            BitmapFactory.Options BFO = new BitmapFactory.Options();
            BFO.inPreferredConfig = Bitmap.Config.RGB_565;
            //由於FaceDetector只能處理RGB565格式的圖片 , 所以我們要對圖片進行前處理的動作

            myBitmap = BitmapFactory.decodeFile(fileOriginal, BFO);
            //從getResources()中的物件找出在res/drawable裡的圖片，並以RGB565處理解譯成Bitmap物件

            imageWidth = myBitmap.getWidth();
            imageHeight = myBitmap.getHeight();
            myFaceDetect = new FaceDetector(imageWidth, imageHeight, numberOfFace);
            //Note that the width of the image must be even.
            //Doc中有描述上面這句 , 不過不是很清楚用意

            myFace = new FaceDetector.Face[numberOfFace];
            //宣告一個FaceDetector.Face陣列 , 準備儲存所偵測到的臉孔位置資訊
            //所以陣列容量必定要和之前自訂的臉孔可偵測數目一樣

            Bundle bundle = getIntent().getExtras();

            String dcx = bundle.getString("dcx");
            String dcy = bundle.getString("dcy");

            idcx = Integer.parseInt(dcx);
            idcy = Integer.parseInt(dcy);

            numberOfFaceDetected = myFaceDetect.findFaces(myBitmap, myFace);

            //偵測人臉失敗
            if (numberOfFaceDetected == 0) {

                AlertDialog.Builder detectDialog = new AlertDialog.Builder(SecondView.this);
                detectDialog.setTitle("Failure detection!");
                detectDialog.setNegativeButton("Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setClass(SecondView.this, MainActivity.class);
                        startActivity(intent);
                        //關閉畫面
                        SecondView.this.finish();
                    }
                });
                detectDialog.show();
            } else {
                dis_s = (idcx * idcx) + (idcy * idcy);
                int dis_tem = dis_s;
                for (int i = 0; i < numberOfFaceDetected; i++) {
                    //將每個偵測出來的臉孔當畫上框框
                    FaceDetector.Face face = myFace[i];
                    PointF myMidPoint = new PointF();
                    face.getMidPoint(myMidPoint);
                    //兩眼中心點的point
                    myEyesDistance = face.eyesDistance();
                    //兩眼之間的距離

                    int r, s, k;
                    int mx, my;

                    myEyesDistance = face.eyesDistance();
                    mx = (int) myMidPoint.x;
                    my = (int) myMidPoint.y;

                    r = (int) (myMidPoint.x - (myEyesDistance * 2));
                    s = (int) (myMidPoint.y - (myEyesDistance * 2));
                    k = (int) (myEyesDistance);

                    dis_t = (idcx - mx) * (idcx - mx) + (idcy - my) * (idcy - my); // 照像位置與人臉距離

                    if (dis_t < dis_tem) {
                        r_new = r;
                        s_new = s;
                        dis_tem = dis_t;
                    }
                    try {
                        //將擷取後的人臉圖像存至手機
                        Bitmap newbm = Bitmap.createBitmap(myBitmap, r_new, s_new, (4 * k), (4 * k));
                        FileOutputStream fop;
                        fop = new FileOutputStream(fileTransfer);
                        newbm.compress(Bitmap.CompressFormat.JPEG, 90, fop);
                        fop.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent();
                    intent.setClass(SecondView.this, Recognize.class);
                    startActivity(intent);
                    //關閉畫面
                    SecondView.this.finish();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SecondView.this.finish();
    }
}
