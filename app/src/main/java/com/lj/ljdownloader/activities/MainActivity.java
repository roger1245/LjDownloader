package com.lj.ljdownloader.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.lj.ljdownloader.R;
import com.lj.ljdownloader.entities.FileInfo;
import com.lj.ljdownloader.service.DownloadService;

public class MainActivity extends AppCompatActivity {

    private Button start;
    private Button stop;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = findViewById(R.id.bt_start);
        stop = findViewById(R.id.bt_stop);
        progressBar = findViewById(R.id.progress_bar);
        initEvent();
    }

    private void initEvent() {
        final FileInfo fileInfo = new FileInfo(0, "http://download.kugou.com/download/kugou_pc", "kugou", 0, 0);
        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
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
