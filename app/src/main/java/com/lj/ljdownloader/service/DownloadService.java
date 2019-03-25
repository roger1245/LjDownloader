package com.lj.ljdownloader.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.lj.ljdownloader.db.ThreadDAO;
import com.lj.ljdownloader.db.ThreadDAOImpl;
import com.lj.ljdownloader.entities.FileInfo;
import com.lj.ljdownloader.entities.ThreadInfo;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DownloadService extends Service {
    public static final String TAG = "DownloadService";
    public static final int MEG_UPDATE = 2;
    public static final int MSG_INIT = 1;
//    public static final int MSG_CONTINUE = 2;
//    public static final int MSG_STOP = 3;
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads/";
//    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    private Map<Integer, DownloadWorker> mWorkers = new LinkedHashMap<>();

    private FileInfo fileInfo;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            FileInfo fileInfo;
            DownloadWorker downloadWorker;
            switch (msg.what) {
                case MSG_INIT:
                    fileInfo  = (FileInfo) msg.obj;
                    downloadWorker = new DownloadWorker(DownloadService.this, fileInfo, mHandler);
                    Log.d(TAG, fileInfo.getId() + "");
                    mWorkers.put(fileInfo.getId(), downloadWorker);
                    downloadWorker.download();
                    break;
                case MEG_UPDATE:
                    fileInfo = (FileInfo) msg.obj;

//                case MSG_CONTINUE:
//                    fileInfo = (FileInfo) msg.obj;
//                    downloadWorker = new DownloadWorker(DownloadService.this, fileInfo);
//                    downloadWorker.download();
//                    break;
//                case MSG_STOP:
//                    fileInfo = (FileInfo) msg.obj;
//                    downloadWorker = mWorkers.get(fileInfo.getId());
//                    if (downloadWorker != null) {
//                        downloadWorker.isPause = true;
//                    }
//                    break;


            }
        }
    };
    public DownloadService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FileInfo mFileInfo;

        mFileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
        if (fileInfo == null) {
            fileInfo = mFileInfo;
        }
        if (ACTION_START.equals(intent.getAction())) {
//            ThreadDAO mDAO;
//            mDAO = new ThreadDAOImpl(DownloadService.this);
//            List<ThreadInfo> threadInfos = mDAO.getThreads(fileInfo.getUrl());
//            if (threadInfos.size() == 0) {
                InitThread initThread = new InitThread(fileInfo);
                initThread.start();
//            } else {
//                ThreadInfo threadInfo = threadInfos.get(0);
//                Log.d(TAG, threadInfos.get(0).toString());
//                fileInfo.setFinished(threadInfo.getFinished());
//                DownloadWorker downloadWorker = mWorkers.get(fileInfo.getId());
//                if (downloadWorker != null) {
//                    downloadWorker.isPause = false;
//                    downloadWorker.download();
//                }

//            }
//            if (fileInfo.getFinished() == 0) {
//                InitThread initThread = new InitThread(fileInfo);
//                initThread.start();
//            } else {
//
//            }

        }
        if (ACTION_STOP.equals(intent.getAction())) {
            DownloadWorker downloadWorker = mWorkers.get(fileInfo.getId());
            if (downloadWorker != null) {
                downloadWorker.isPause = true;
            }
        }
//        if (ACTION_UPDATE.equals(intent.getAction())) {
//            ThreadDAO mDAO;
//            mDAO = new ThreadDAOImpl(DownloadService.this);
//            List<ThreadInfo> threadInfos = mDAO.getThreads(fileInfo.getUrl());
//            ThreadInfo threadInfo = null;
//            Log.d(TAG, threadInfos.get(0).toString());
//
//        }

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
