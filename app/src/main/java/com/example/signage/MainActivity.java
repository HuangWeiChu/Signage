package com.example.signage;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    //static String serverIp = "140.134.26.138";
    static String serverIp = Login.serverIp;
    static int serverPort = 5050;

    static String act = ""; //iconType
    SurfaceView mySurfaceView; // surfaceView聲明
    SurfaceHolder holder; // surfaceHolder聲明
    Camera myCamera; // 相機聲明
    String filePath = "//sdcard//DCIM//capture.jpg"; // 照片保存路徑

    boolean isClicked = false; // 是否點擊標識
    Gallery iconZone;
    Intent intent;
    Chirp chirp;
    File file;

    // 創建jpeg圖片回調數據對象
    Camera.PictureCallback jpeg = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            try {// 獲得圖片

                //將擷取後的圖像存至手機
                Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                File file = new File(filePath);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

                Matrix matrix = new Matrix();
                matrix.reset();
                matrix.postRotate(90);
                Bitmap bMapRotate = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                bm = bMapRotate;

                bm.compress(Bitmap.CompressFormat.JPEG, 100, bos); // 將圖片壓縮到流中
                bos.flush(); // 輸出
                bos.close(); // 關閉

                if (act.equals("0") || act.equals("5"))
                    intent.setClass(MainActivity.this, ThirdView.class);
                else
                    intent.setClass(MainActivity.this, SecondView.class);
                startActivity(intent);

                //關閉畫面
                //MainActivity.this.finish();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("<PAGE>", "MainActivity");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 無標題

        //開始執行chirp
        View view = findViewById(android.R.id.content);
        chirp = new Chirp(view,this);
        chirp.start();

        // 設置拍攝方向
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);

        // 取得控件
        mySurfaceView = findViewById(R.id.SurFaceView1);

        myDragEventListener mDragListen = new myDragEventListener();

        mySurfaceView.setOnDragListener(mDragListen);

        // 取得句柄
        holder = mySurfaceView.getHolder();

        // 添加回調
        holder.addCallback(this);

        // 設置類型
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        /*captureButton = (Button) findViewById(R.id.captureButton);
        captureButton.setOnClickListener(this);*/

        file = this.getFilesDir();

        // 設置監聽
        //mySurfaceView.setOnClickListener();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        iconZone = findViewById(R.id.iconZone);
        iconZone.setAdapter(new ImageAdapter(this));// Sets a long click listener for the ImageView using an anonymous listener object that

        intent = new Intent();
    }

    public class ImageAdapter extends BaseAdapter {
        private int[] icons = {
                R.drawable.icon_info,
                R.drawable.icon_ticket,
                R.drawable.icon_photo,
                R.drawable.icon_video,
                R.drawable.icon_music,
                R.drawable.icon_chat
        };

        private Context mCoNtext;

        public ImageAdapter(Context c) {
            mCoNtext = c;

            iconZone.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    String s = Integer.toString(position);
                    act = s;
                    Log.e("qqqqqqqqqqqq: ", act);
                    String file_name = s.substring(s.lastIndexOf("/") + 1, s.length()).toString();

                    ClipData dragData = ClipData.newPlainText(s, file_name);

                    View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);

                    view.startDrag(dragData,  // the data to be dragged
                            myShadow,  // the drag shadow builder
                            null,      // no need to use local data
                            0          // flags (not currently used, set to 0)
                    );
                    return false;
                }
            });
        }

        public int getCount() {
            return icons.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //顯示icon
            if (convertView == null) {
                convertView = LayoutInflater.from(mCoNtext).inflate(R.layout.gallery, null);
            }
            ImageView icon = convertView.findViewById(R.id.icon);

            switch (position) {
                case 0:
                    icon.setImageResource(icons[0]);
                    break;
                case 1:
                    icon.setImageResource(icons[1]);
                    break;
                case 2:
                    icon.setImageResource(icons[2]);
                    break;
                case 3:
                    icon.setImageResource(icons[3]);
                    break;
                case 4:
                    icon.setImageResource(icons[4]);
                    break;
                case 5:
                    icon.setImageResource(icons[5]);
                    break;
                default:
            }
            return convertView;
        }
    }

    //Set and Define OnDragListener
    private class myDragEventListener implements View.OnDragListener {

        public int dcx = 0, dcy = 0;
        public int picWidth = 480;
        public int picHeight = 640;

        // This is the method that the system calls when it dispatches a drag event to the
        // listener.
        public boolean onDrag(View view, DragEvent event) {
            // Defines a variable to store the action type for the incoming event
            final int action = event.getAction();
            // Handles each of the expected events
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // Determines if this View can accept the dragged data
                    if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        return (true);
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);

                    myCamera.autoFocus(afcb);
                    isClicked = true;

                    int width = size.x;
                    int height = size.y;

                    dcx = (int) (float) (event.getX() / width * picWidth);
                    dcy = (int) (float) (event.getY() / height * picHeight);

                    Bundle bundle = new Bundle();
                    bundle.putString("dcx", Integer.toString(dcx) );
                    bundle.putString("dcy", Integer.toString(dcy));
                    intent.putExtras(bundle);

                case DragEvent.ACTION_DRAG_ENDED:
                    break;

                default:
                    break;
            }
            return false;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        // TODO Auto-generated method stub
        // 設置參數並開始預覽

        Camera.Parameters params = myCamera.getParameters();

        params.setPictureFormat(PixelFormat.JPEG);
        params.setPreviewSize(640, 480);

        params.setPictureSize(640, 480);

        myCamera.setParameters(params);
        myCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        //開啟相機

        if (myCamera == null) {
            myCamera = Camera.open();
            myCamera.setDisplayOrientation(90);

            try {
                myCamera.setPreviewDisplay(holder);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        // 關閉預覽並釋放資源

        myCamera.stopPreview();
        myCamera.release();
        myCamera = null;
    }

    Camera.AutoFocusCallback afcb = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // TODO Auto-generated method stub
            if (success) {
                // 設置參數,並拍照
                Camera.Parameters params = myCamera.getParameters();
                params.setPictureFormat(PixelFormat.JPEG);

                params.setPreviewSize(640, 480);

                params.setPictureSize(640, 480);

                myCamera.setParameters(params);
                myCamera.takePicture(null, null, jpeg);
            }
        }
    };

    // Chirp setting
    @Override
    protected void onResume() {
        super.onResume();
        chirp.startSdk();
    }

    @Override
    protected void onPause() {
        super.onPause();
        chirp.chirpConnect.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            chirp.chirpConnect.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        chirp.stopSdk();
    }
}
