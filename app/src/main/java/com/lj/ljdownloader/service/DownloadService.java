package com.lj.ljdownloader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.lj.ljdownloader.entities.FileInfo;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends Service {
    public static final String TAG = "DownloadService";
//    public static final int ACTION_UPDATE = 2;
    public static final int MSG_INIT = 1;
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads/";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";

    private FileInfo fileInfo;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FileInfo fileInfo;
            DownloadWorker downloadWorker;
            switch (msg.what) {
                case MSG_INIT:
                    fileInfo  = (FileInfo) msg.obj;
                    downloadWorker = new DownloadWorker(DownloadService.this, fileInfo);
                    downloadWorker.download();
                    break;
            }
        }
    };
    public DownloadService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
        if (ACTION_START.equals(intent.getAction())) {
            InitThread initThread = new InitThread(fileInfo);
            initThread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    class InitThread extends Thread {
        private FileInfo mFileInfo = null;
        public InitThread(FileInfo mFileInfo) {
            this.mFileInfo = mFileInfo;
        }
        public void run() {
            Log.d(TAG, fileInfo.toString());
            HttpURLConnection connection = null;
            RandomAccessFile randomAccessFile = null;
            try {
                URL url = new URL(mFileInfo.getUrl());
                connection = (HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                int length = -1;
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    length = connection.getContentLength();
                }
                if (length <= 0) {
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                mFileInfo.setLength(length);
                File file = new File(dir, mFileInfo.getFileName() + mFileInfo.getExtend());
                randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.setLength(length);
                mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
                try {
                    randomAccessFile.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
