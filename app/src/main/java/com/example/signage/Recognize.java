package com.example.signage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;

public class Recognize extends Activity {

    static String type = "";             // MusicFestival Type
    String bufRecv;                     // 接收到的訊息類型
    String result = "";                 // 辨識結果
    String filePath = "//sdcard//DCIM//captureFix.jpg"; // 照片保存路徑

    Socket clientSocket;	            // 客戶端socket
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("<PAGE>", "Recognize");
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.background);

        Thread t = new Thread(readData);
        t.start();

        BitmapFactory.Options BFO = new BitmapFactory.Options();
        BFO.inPreferredConfig = Bitmap.Config.RGB_565;
        //由於FaceDetector只能處理RGB565格式的圖片 , 所以我們要對圖片進行前處理的動作

        Bitmap myBitmap = BitmapFactory.decodeFile(filePath, BFO);
        //從getResources()中的物件找出在res/drawable裡的圖片，並以RGB565處理解譯成Bitmap物件

        ImageView image = new ImageView(this);
        image.setImageBitmap(myBitmap);

        AlertDialog.Builder showCapture = new AlertDialog.Builder(this);
        showCapture.setMessage("Capture image");
        showCapture.setView(image);
        showCapture.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Message mes = new Message();
                mes.what = 0;
                handler.sendMessage(mes);

                if (clientSocket.isConnected()) {
                    if (new File(filePath).exists()) {
                        try {
                            byte[] buffer = new byte[2048];
                            int bytesRead;

                            //將人臉圖像傳至PC
                            RandomAccessFile fileOutStream = new RandomAccessFile(filePath, "r");
                            fileOutStream.seek(0);

                            String bufSend = "face";
                            String size = fileOutStream.length() + "\n";
                            clientSocket.getOutputStream().write(bufSend.getBytes());
                            clientSocket.getOutputStream().write(size.getBytes());

                            Thread.sleep(1000);

                            if (bufRecv != null && bufRecv.equals("startSend")) {
                                Log.e("[Progress]","* Start sending file *");
                                while ((bytesRead = fileOutStream.read(buffer)) != -1) {
                                    clientSocket.getOutputStream().write(buffer, 0, bytesRead);
                                }
                                Log.e("[Progress]","* Send completion *");
                                fileOutStream.close();
                                bufRecv = "";
                            }
                        } catch (IOException ioe) {
                            Log.e("[Exception]", "IOException");
                            ioe.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("[Error]", "File doesn't exist");
                    }
                }
                dialog.dismiss();
            }
        });
        showCapture.show();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 0:
                    progressDialog = new ProgressDialog(Recognize.this);
                    progressDialog.setTitle("Recognizing...");
                    progressDialog.setMessage("Please wait");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setIndeterminate(false);
                    progressDialog.setProgress(100);
                    progressDialog.show();
                    break;
                case 1:
                    progressDialog.dismiss();
                    AlertDialog.Builder recognizeDialog = new AlertDialog.Builder(Recognize.this);
                    recognizeDialog.setTitle("Recognize result");
                    if (result != null && !result.equals("ERROR")) {
                        type = result;
                        recognizeDialog.setMessage(result);
                        recognizeDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setClass(Recognize.this, ThirdView.class);
                                startActivity(intent);
                                //關閉畫面
                                Recognize.this.finish();
                                dialog.dismiss();
                                Log.e("[TEST]","finish");
                            }
                        });
                    } else if (result.equals("ERROR")){
                        recognizeDialog.setMessage("Failed.");
                        recognizeDialog.setNegativeButton("Try again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setClass(Recognize.this, MainActivity.class);
                                startActivity(intent);
                                //關閉畫面
                                Recognize.this.finish();
                                dialog.dismiss();
                            }
                        });
                    }
                    recognizeDialog.show();
                    break;
                default:
            }
            super.handleMessage(msg);
        }
    };

    private Runnable readData = new Runnable() {
        public void run() {
            try {
                InetAddress serverIp = InetAddress.getByName(MainActivity.serverIp);
                clientSocket = new Socket(serverIp, MainActivity.serverPort);

                BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                while (clientSocket.isConnected()) {
                    bufRecv = br.readLine();
                    Log.e("[bufRecv]", bufRecv);

                    if (bufRecv != null && !bufRecv.equals("startSend") ) {
                        result = bufRecv;
                        Log.e(">>>>>result",result);

                        Message mes = handler.obtainMessage();
                        mes.what = 1;
                        handler.sendMessage(mes);
                    }
                }
            }
            catch (IOException ioe) {
                Log.e("[Exception]", "IOException");
                ioe.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Recognize.this.finish();
    }
}