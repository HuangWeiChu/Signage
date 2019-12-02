package com.example.signage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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

import static android.content.ContentValues.TAG;

public class ThirdView extends Activity {
    String icon = MainActivity.act; //Icon Type
    String select = Recognize.type; // MusicFestival Type
    String urlString = "";

    String bufSend;      // 送出iconType
    String bufRecv;      // 接收到的訊息類型
    String fileExt;      // file extension
    String filePath;      // file filePath
    String facePath = "//sdcard//DCIM//face.jpg"; // 照片保存路徑

    static String mixType = "";
    boolean isBoard = false;
    boolean isPhone = false;

    static Socket clientSocket;	// 客戶端socket
    ProgressDialog progressDialog;
    int hLoad,hSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("<PAGE>", "ThirdView");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        getWindow().setBackgroundDrawableResource(R.drawable.background);

        //Thread t = new Thread(readData);
        //t.start();

        Log.e("[Select]",select);
        Log.e("[Icon]",icon);

        switch (icon) {
            case "0":
                bufSend = "info";
                break;
            case "1":
                bufSend = "ticket";
                break;
            case "2":
                bufSend = "photo";
                fileExt = ".jpg";
                break;
            case "3":
                bufSend = "video";
                fileExt = ".mp4";
                break;
            case "4":
                bufSend = "music";
                fileExt = ".mp3";
                break;
            case "5":
                bufSend = "txt";
                break;
        }
        //執行功能
        if (icon.equals("0")) {
            //取消官網功能
            Log.e("[Progress]","<---icon(" + icon + ")--->");

            /*方法1
            Intent intent = new Intent(); //呼叫照相機
            intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
            startActivity(intent);
            */

            //設定檔名
            File tmpFile = new File(facePath);
            Uri outputFileUri = Uri.fromFile(tmpFile);

            Intent intent =  new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);    //利用intent去開啟android本身的照相介面
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(intent, 0);


        } else if (icon.equals("6")) {
            //取消官網功能
            Log.e("[Progress]","<---icon(" + icon + ")--->");
            switch (select) {
                case "Rock":
                    urlString = "https://hohaiyan.ntpc.gov.tw/1-index.html";
                    break;
                case "Wake up":
                    urlString = "https://www.wakeupfestival.com.tw/#/";
                    break;
                case "Ultra":
                    urlString = "https://ultramusicfestival.com/gallery/";
                    break;
                case "Tomorrowland":
                    urlString = "https://www.tomorrowland.com/global/";
                    break;
            }
            Uri uri = Uri.parse(urlString);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (icon.equals("1")) {
            Log.e("[Progress]","<---icon(" + icon + ")--->");
            switch (select) {
                case "Rock":
                    urlString = "Free";
                    break;
                case "Wake up":
                    urlString = "https://www.wakeup-shop.com";
                    break;
                case "Ultra":
                    urlString = "https://ultramusicfestival.com/tickets/miami/";
                    break;
                case "Tomorrowland":
                    urlString = "https://www.tomorrowland.com/en/festival/tickets";
                    break;
            }
            Uri uri = Uri.parse(urlString);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (icon.equals("2") || icon.equals("3") || icon.equals("4")) {
            Log.e("[Progress]","<---icon(" + icon + ")--->");
            Thread t = new Thread(readData);
            t.start();
            socketSend(bufSend);
        } else if (icon.equals("5")) {
            Log.e("[Progress]","<---icon(" + icon + ")--->");
            Intent intent = new Intent();
            intent.setClass(ThirdView.this, Passtxt.class);
            startActivity(intent);
        }
        //關閉畫面
        //ThirdView.this.finish();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Log.e("[TE...0ST]","what(0)");
                    progressDialog = new ProgressDialog(ThirdView.this);
                    progressDialog.setTitle("Downloading...");
                    progressDialog.setMessage("Please wait");
                    //progressDialog.setMessage("Please wait...\nSaving in " + filePath);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setIndeterminate(false);
                    progressDialog.setProgress(100);
                    //progressDialog.show();
                    break;
                case 1:
                    Log.e("[Download]",hLoad + "/" + hSize);
                    if (hLoad == hSize) {
                        Log.e("[Download]","Finish");
                        Intent intent = new Intent();
                        intent.setClass(ThirdView.this, FinalView.class);
                        startActivity(intent);
                        //關閉畫面
                        ThirdView.this.finish();
                        progressDialog.dismiss();
                    } else
                        progressDialog.setProgress((int) ((float) hLoad / hSize * 100));
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
                    filePath = "//sdcard//DCIM//" + select + fileExt;

                    Log.e("[bufRecv]",bufRecv);
                    Log.e("[filePath]",filePath);

                    if (bufRecv != null && bufRecv.equals("startSend")) {
                        clientSocket.getOutputStream().write(select.getBytes());

                        String size; //檔案size
                        size = br.readLine();
                        Log.e("[Size]", size);

                        /*do {
                            size = br.readLine();
                            Log.e("[size]",size);
                        } while (size == null || size.equals("startSend"));*/

                        RandomAccessFile fileOutStream = new RandomAccessFile(filePath, "rwd");
                        fileOutStream.setLength(Integer.valueOf(size));
                        fileOutStream.seek(0);

                        int readSize = Integer.parseInt(size); //轉換成int
                        byte[] buffer = new byte[102400]; //存放接收到的檔案
                        int bytesWrite; //每次寫入bytes
                        int writeSize = 0; //已寫入sizes

                        hSize = readSize;
                        Message mes = new Message();
                        mes.what = 0;
                        handler.sendMessage(mes);

                        Log.e("[Progress]","* Start writing file *");
                        while (writeSize < readSize) {
                            bytesWrite = bis.read(buffer);
                            fileOutStream.write(buffer, 0, bytesWrite);
                            writeSize += bytesWrite;
                            //Log.e("load",writeSize + "/" + readSize);

                            hLoad = writeSize;
                            mes = handler.obtainMessage();
                            mes.what = 1;
                            handler.sendMessage(mes);
                            //Log.e("[TEST]","sendMessage");
                            Thread.sleep(100);
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
            catch ( Exception e) {
                e.printStackTrace();
            }
        }
    };

    public static void socketSend(String send) {
        Log.e("[Progress]","socketSend");
        try {
            Thread.sleep(1000);
            if (clientSocket.isConnected()) {
                try {
                    clientSocket.getOutputStream().write(send.getBytes());
                } catch (IOException ioe) {
                    Log.e("[Exception]", "IOException");
                    ioe.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "--- ThirdView(onDestroy)  ---");
        /*
        Intent intent = new Intent();
        intent.setClass(ThirdView.this, SendImage.class);
        startActivity(intent);
        */
        ThirdView.this.finish();
    }

    //開啟相機後的動作
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "--- onActivityResult  ---");
        if (resultCode == RESULT_OK) {
            Log.e(TAG, "--- resultCode  ---");

            String img_address = facePath;
            Bitmap bmp = BitmapFactory.decodeFile(img_address); //利用BitmapFactory去取得剛剛拍照的圖像

            ImageView imageView = new ImageView(this);

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
            imageView.setImageBitmap(newbmp);

            AlertDialog.Builder showCapture = new AlertDialog.Builder(this);
            showCapture.setMessage("Capture image");
            showCapture.setView(imageView);
            // 設定按鈕
            showCapture.setPositiveButton("看板顯示", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    isBoard = true;
                    mixType = "board";

                    Intent intent = new Intent();
                    intent.setClass(ThirdView.this, SendImage.class);
                    startActivity(intent);
                }
            });
            showCapture.setNegativeButton("手機顯示", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    isPhone = true;
                    mixType = "phone";

                    Intent intent = new Intent();
                    intent.setClass(ThirdView.this, SendImage.class);
                    startActivity(intent);
                }
            });
            showCapture.show();
        }
    }

}
