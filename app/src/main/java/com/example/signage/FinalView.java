package com.example.signage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class FinalView extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("<PAGE>","----FinalView----");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);
        getWindow().setBackgroundDrawableResource(R.drawable.background);

        AlertDialog.Builder showCapture = new AlertDialog.Builder(this);
        showCapture.setMessage("Download completion");
        showCapture.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setClass(FinalView.this, MainActivity.class);
                startActivity(intent);
            }
        });
        showCapture.show();
    }
}
