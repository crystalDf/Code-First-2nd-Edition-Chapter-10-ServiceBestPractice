package com.star.servicebestpractice;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String DOWNLOAD_URL =
//            "https://raw.githubusercontent.com/guolindev/eclipse/master/eclipse-inst-win64.exe";

            "http://video.dispatch.tc.qq.com/q00154aw8j4.mp4?vkey=FF7566E3D553C3E9321959B27CCC2B22BD7F2B13ED46EDE9F13B6E1DDCA19FE017281D1CD37B0AEFE13D20F47C96D82D2C22FCB9EDD711E245B8DA798C8C0FEA758C5337E7020272000E411DF9809B5822474B7707E22D4889EC3E759789B79D4C33A4BFDD640164";

    private static final int PERMISSION_REQUEST_CODE = 0;

    private DownloadService.DownloadBinder mDownloadBinder;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private Button mStartDownload;
    private Button mPauseDownload;
    private Button mCancelDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartDownload = findViewById(R.id.start_download);
        mPauseDownload = findViewById(R.id.pause_download);
        mCancelDownload = findViewById(R.id.cancel_download);

        mStartDownload.setOnClickListener(v -> {
            if (mDownloadBinder == null) {
                return;
            }

            mDownloadBinder.startDownload(DOWNLOAD_URL);
        });

        mPauseDownload.setOnClickListener(v -> {
            if (mDownloadBinder == null) {
                return;
            }

            mDownloadBinder.pauseDownload();
        });

        mCancelDownload.setOnClickListener(v -> {
            if (mDownloadBinder == null) {
                return;
            }

            mDownloadBinder.cancelDownload();
        });

        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if ((grantResults.length > 0) &&
                        (grantResults[0] != PackageManager.PERMISSION_GRANTED)) {

                    Toast.makeText(MainActivity.this,
                            "Permission Denied",
                            Toast.LENGTH_LONG).show();

                    finish();
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mServiceConnection);
    }
}
