package com.lj.ljdownloader.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lj.ljdownloader.R;
//import com.lj.ljdownloader.db.ThreadDAO;
//import com.lj.ljdownloader.db.ThreadDAOImpl;
import com.lj.ljdownloader.entities.FileInfo;
//import com.lj.ljdownloader.entities.ThreadInfo;
import com.lj.ljdownloader.service.DownloadService;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private Button start;
    private Button stop;
    private Button pause;
    private ProgressBar progressBar;
    private FileInfo fileInfo;
    private IntentFilter intentFilter;
    private UIUpdateBroadcastReceiver receiver;
    private EditText editText;
    private EditText extend;

    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = findViewById(R.id.bt_start);
        stop = findViewById(R.id.bt_stop);
        pause = findViewById(R.id.bt_pause);
        editText = findViewById(R.id.edit_url);
        extend = findViewById(R.id.edit_extend);
        progressBar = findViewById(R.id.progress_bar);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        initEvent();
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
        bindService(intent, connection, BIND_AUTO_CREATE);
        intentFilter = new IntentFilter();
        intentFilter.addAction("UPDATE_UI_BROADCAST");
        receiver = new UIUpdateBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        unregisterReceiver(receiver);
    }

    private void initEvent() {
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String url = editText.getText().toString();
                String extendStr = extend.getText().toString();
                if (!"".equals(url.trim()) && !"".equals(extendStr.trim())) {
                    fileInfo = new FileInfo(url, extendStr);
                    Log.d(TAG, "url != null + " + url + extendStr);
                    downloadBinder.startDownload(fileInfo);

                }

            }
        });
        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                downloadBinder.cancelDownload();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadBinder.pauseDownload();
            }
        });
    }
    class UIUpdateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra("progress", 0);
            progressBar.setProgress(progress);
        }
    }
}
