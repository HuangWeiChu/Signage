package com.example.signage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;

public class SendImage extends Activity {

    String mixType = ThirdView.mixType; // Mix face Type
    String bufRecv;                     // 接收到的訊息類型
    String facePath = "//sdcard//DCIM//face.jpg"; // 照片保存路徑
    String mixPath = "//sdcard//DCIM//mix.jpg"; // 照片保存路徑

    static Socket clientSocket;	            // 客戶端socket
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("<PAGE>", "SendImage");
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.background);

        Thread t = new Thread(readData);
        t.start();

        String img_address = facePath;
        Bitmap bmp = BitmapFactory.decodeFile(img_address); //利用BitmapFactory去取得剛剛拍照的圖像

        ImageView image = new ImageView(this);

        // 使用Matrix物件 進行圖片縮小
        Matrix matrix = new Matrix();

        // 獲得原圖的寬高
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        // 縮小為0.5倍
        float scaleWidth = (float) 0.5;
        float scaleHeight = (float) 0.5;

        // 設定物件x,y方向的縮放比例
        matrix.postScale(scaleWidth, scaleHeight);

        // 設定物件逆時針旋轉90度
        matrix.postRotate(-90.0F);

        // 得到新的圖片
        Bitmap newbmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix,true);

        // 重新載入 imageView
        image.setImageBitmap(newbmp);



        AlertDialog.Builder showCapture = new AlertDialog.Builder(this);
        showCapture.setMessage("Send image");
        showCapture.setView(image);
        showCapture.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Message mes = new Message();
                mes.what = 0;
                handler.sendMessage(mes);

                if (clientSocket.isConnected()) {
                    if (new File(facePath).exists()) {
                        try {
                            byte[] buffer = new byte[2048];
                            int bytesRead;

                            //將人臉圖像傳至PC
                            RandomAccessFile fileOutStream = new RandomAccessFile(facePath, "r");
                            fileOutStream.seek(0);

                            String bufSend = "faceChange";
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
                    progressDialog = new ProgressDialog(SendImage.this);
                    progressDialog.setTitle("Mixing face...");
                    progressDialog.setMessage("Please wait");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setIndeterminate(false);
                    progressDialog.setProgress(100);
                    progressDialog.show();
                    break;
                case 1:
                    progressDialog.dismiss();
                    AlertDialog.Builder recognizeDialog = new AlertDialog.Builder(SendImage.this);
                    recognizeDialog.setTitle("Mix result");
                    recognizeDialog.setMessage("Success");
                    recognizeDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(SendImage.this, ThirdView.class);
                            startActivity(intent);
                            //關閉畫面
                            SendImage.this.finish();
                            dialog.dismiss();
                            Log.e("[TEST]","finish");
                        }
                    });
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

                BufferedInputStream bis = new BufferedInputStream(clientSocket.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                while (clientSocket.isConnected()) {
                    bufRecv = br.readLine();
                    Log.e("[bufRecv]", bufRecv);

                    if (bufRecv != null && bufRecv.equals("type") ) {
                        Log.e(">>>>>result",mixType);

                        //clientSocket.getOutputStream().write("phone".getBytes());

//                        Message mes = handler.obtainMessage();
//                        mes.what = 1;
//                        handler.sendMessage(mes);
                    }
                    if (bufRecv != null && bufRecv.equals("startWrite")) {

                        String size; //檔案size
                        size = br.readLine();
                        Log.e("[Size]", size);

                        /*do {
                            size = br.readLine();
                            Log.e("[size]",size);
                        } while (size == null || size.equals("startSend"));*/

                        RandomAccessFile fileOutStream = new RandomAccessFile(mixPath, "rwd");
                        fileOutStream.setLength(Integer.valueOf(size));
                        fileOutStream.seek(0);

                        int readSize = Integer.parseInt(size); //轉換成int
                        byte[] buffer = new byte[102400]; //存放接收到的檔案
                        int bytesWrite; //每次寫入bytes
                        int writeSize = 0; //已寫入sizes

//                        hSize = readSize;
//                        Message mes = new Message();
//                        mes.what = 0;
//                        handler.sendMessage(mes);

                        Log.e("[Progress]","* Start writing file *");
                        while (writeSize < readSize) {
                            bytesWrite = bis.read(buffer);
                            fileOutStream.write(buffer, 0, bytesWrite);
                            writeSize += bytesWrite;
                            Log.e("load",writeSize + "/" + readSize);

//                            hLoad = writeSize;
//                            mes = handler.obtainMessage();
//                            mes.what = 1;
//                            handler.sendMessage(mes);
                            //Log.e("[TEST]","sendMessage");
                            //Thread.sleep(100);
                        }
                        Log.e("[Progress]","* Write completion *");
                        fileOutStream.close();
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
        SendImage.this.finish();
    }
}