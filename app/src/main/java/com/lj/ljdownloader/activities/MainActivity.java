package com.lj.ljdownloader.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.lj.ljdownloader.R;
import com.lj.ljdownloader.db.ThreadDAO;
import com.lj.ljdownloader.db.ThreadDAOImpl;
import com.lj.ljdownloader.entities.FileInfo;
import com.lj.ljdownloader.entities.ThreadInfo;
import com.lj.ljdownloader.service.DownloadService;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button start;
    private Button stop;
    private ProgressBar progressBar;
    private FileInfo fileInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = findViewById(R.id.bt_start);
        stop = findViewById(R.id.bt_stop);
        progressBar = findViewById(R.id.progress_bar);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        initEvent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
//                if ()
        }
    }

    private void initEvent() {
        fileInfo = new FileInfo(0, "http://downapp.baidu.com/baidusearch/AndroidPhone/11.5.0.10/1/757p/20190302185442/baidusearch_AndroidPhone_11-5-0-10_757p.apk?", "kugou", 0, 0, ".apk");
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);

//                ThreadDAO mDAO;
//                mDAO = new ThreadDAOImpl(MainActivity.this);
//                List<ThreadInfo> threadInfos = mDAO.getThreads(fileInfo.getUrl());
//                ThreadInfo threadInfo = null;
//                Log.d(TAG, threadInfos.get(0).toString());
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });
    }
}
