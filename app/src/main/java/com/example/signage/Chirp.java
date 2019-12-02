package com.example.signage;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
//import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.chirp.connect.ChirpConnect;
import io.chirp.connect.interfaces.ConnectEventListener;
import io.chirp.connect.models.ChirpConnectState;
import io.chirp.connect.models.ChirpError;

public class Chirp {
    private String TAG = "[Chirp]";

    private String CHIRP_APP_KEY = "ae4E5CF6E32CfDddEBe26Bf0C";
    private String CHIRP_APP_SECRET = "6bED19BC782BA5D20CA9eb7b4d9BfaBDEfB1cd5A9a5AB6f26C";
    private String CHIRP_APP_CONFIG = "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";

    private Context parentContext;
    private View parentLayout;

    private View goodsView;
    private String setName;
    private String setContent;
    private int setImage;
    private String setUrl;

    ChirpConnect chirpConnect;

    public Chirp (View view, Context context){
        this.parentLayout = view;
        this.parentContext = context;
    }

    public void start(){
        chirpConnect = new ChirpConnect(parentContext, CHIRP_APP_KEY, CHIRP_APP_SECRET);
        ChirpError setConfigError = chirpConnect.setConfig(CHIRP_APP_CONFIG);
        if (setConfigError.getCode() > 0) {
            Log.e(TAG, setConfigError.getMessage());
        }
        chirpConnect.setListener(connectEventListener);
    }

    ConnectEventListener connectEventListener = new ConnectEventListener() {

        @Override
        public void onSending(byte[] data, int channel) {
        }

        @Override
        public void onSent(byte[] data, int channel) {
        }

        @Override
        public void onReceiving(int channel) {
            //Snackbar snackbar = Snackbar.make(parentLayout, "Receiving goods information", Snackbar.LENGTH_LONG);
            //snackbar.show();
        }

        @Override
        public void onReceived(byte[] data, int channel) {
            String chirpRecv = "null";
            if (data != null) {
                chirpRecv = new String(data);
            }

            // 初始goods畫面
            LayoutInflater inflater = LayoutInflater.from(parentContext);
            goodsView = inflater.inflate(R.layout.goods_dialog,null);
            TextView goodsName = goodsView.findViewById(R.id.goodsName);
            TextView goodsContent = goodsView.findViewById(R.id.goodsContent);
            ImageView goodsImage = goodsView.findViewById(R.id.goodsImage);

            // 開始判斷收到的內容
            switch (chirpRecv) {
                case "R1":
                    setName = "Earphone";
                    setContent ="以舒適的貼合感\n" +
                                "享受高音質聆聽\n" +
                                "不易纏繞打結的波浪刻紋導線";
                    setImage = R.drawable.r1;
                    setUrl = "https://24h.pchome.com.tw/prod/DCAY5I-A9008ZJA2";
                    break;
                case "R2":
                    setName = "Sunglasses";
                    setContent ="男女都適合的百搭潮款太陽眼鏡\n" +
                                "簡練的款式和不凡品質\n" +
                                "採用鮮豔色彩搭配前衛元素";
                    setImage = R.drawable.r2;
                    setUrl = "https://detail.youzan.com/show/goods?alias=2x5hkm6q08ysu";
                    break;
                case "W1":
                    setName = "Vans T-Shirt";
                    setImage = R.drawable.w1;
                    setUrl = "https://www.luxbmx.com/vans-off-the-wall-t-shirt-white-black";
                    break;
                case "W2":
                    setName = "Switch";
                    setImage = R.drawable.w2;
                    setUrl = "https://www.nintendo.com/switch/buy-now/";
                    break;
                case "U1":
                    setName = "Nike Free Running";
                    setImage = R.drawable.u1;
                    setUrl = "https://stockx.com/nike-free-rn-black-white-anthracite";
                    break;
                case "U2":
                    setName = "Flamingo swim shorts";
                    setImage = R.drawable.u2;
                    setUrl = "https://tuclothing.sainsburys.co.uk/p/Mini-Me-Green-%26-Pink-Flamingo-Print-Swim-Shorts/134584451-Green";
                    break;
                case "T1":
                    setName = "Swim trunks";
                    setImage = R.drawable.goods_swim_trunks;
                    setUrl = "https://www.amazon.ae/Sebaby-Floral-Pockets-Tropical-Summer/dp/B07THVKKQH?th=1&psc=1";
                    break;
                case "T2":
                    setName = "Sunscreen";
                    setImage = R.drawable.goods_sunscreen;
                    setUrl = "https://www.youtube.com/";
                    break;
            }

            // 建立AlertDialog
            final AlertDialog.Builder chirp = new AlertDialog.Builder(parentContext);
            chirp.setTitle("Goods information:");

            // 設定goods畫面
            goodsName.setText(setName);
            goodsContent.setText(setContent);
            goodsImage.setImageResource(setImage);
            chirp.setView(goodsView);

            // 設定按鈕
            chirp.setPositiveButton("離開", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // 關閉dialog
                    //dialogInterface.dismiss();
                    dialogInterface.cancel();
                }
            });
            chirp.setNegativeButton("查看詳情", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // 轉跳到購買網頁
                    Uri uri = Uri.parse(setUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    parentContext.startActivity(intent);
                }
            });
            chirp.show();
        }

        @Override
        public void onStateChanged(int oldState, int newState) {
            if (newState == ChirpConnectState.CHIRP_CONNECT_STATE_NOT_CREATED.getCode()) {
                updateStatus("NotCreated");
            } else if (newState == ChirpConnectState.CHIRP_CONNECT_STATE_STOPPED.getCode()) {
                updateStatus("Stopped");
            } else if (newState == ChirpConnectState.CHIRP_CONNECT_STATE_PAUSED.getCode()) {
                updateStatus("Paused");
            } else if (newState == ChirpConnectState.CHIRP_CONNECT_STATE_RUNNING.getCode()) {
                updateStatus("Running");
            } else if (newState == ChirpConnectState.CHIRP_CONNECT_STATE_SENDING.getCode()) {
                updateStatus("Sending");
            } else if (newState == ChirpConnectState.CHIRP_CONNECT_STATE_RECEIVING.getCode()) {
                updateStatus("Receiving");
            } else {
                updateStatus(newState + "");
            }
        }

        @Override
        public void onSystemVolumeChanged(float oldVolume, float newVolume) {
        }
    };

    public void updateStatus(final String newStatus) {
        //Snackbar snackbar = Snackbar.make(parentLayout, "Chirp: " + newStatus, Snackbar.LENGTH_LONG);
        //snackbar.show();
    }

    public void stopSdk() {
        ChirpError error = chirpConnect.stop();
        if (error.getCode() > 0) {
            //Log.e(TAG, error.getMessage());
            return;
        }
    }

    public void startSdk() {
        ChirpError error = chirpConnect.start();
        if (error.getCode() > 0) {
            Log.e(TAG, error.getMessage());
            return;
        }
    }
}