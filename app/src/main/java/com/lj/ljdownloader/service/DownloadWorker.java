package com.lj.ljdownloader.service;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

//import com.lj.ljdownloader.db.ThreadDAO;
//import com.lj.ljdownloader.db.ThreadDAOImpl;
//import com.lj.ljdownloader.entities.FileInfo;
//import com.lj.ljdownloader.entities.ThreadInfo;

import com.lj.ljdownloader.entities.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class DownloadWorker {
    public static final int INVALID_URL = 0;
    public static final int DOWNLOAD_ALREADY = 1;
    public static final int DOWNLOAD_SUCCESS = 2;

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    private boolean isCanceled = false;
    private boolean isPause = false;
    private Handler handler;
    private FileInfo fileInfo;
    public static final String TAG = "DownloadWorker";
    private Context mContext;
//    private Context mContext = null;
//    private FileInfo mFileInfo = null;
//    private ThreadDAO mDAO = null;
//    private int mFinished;


    public DownloadWorker( FileInfo fileInfo, Handler handler, Context mContext) {
        this.mContext = mContext;
//        this.mFileInfo = mFileInfo;
        this.fileInfo = fileInfo;
        this.handler = handler;
//        mDAO = new ThreadDAOImpl(mContext);
    }

    public void download() {

        new DownloadThread(fileInfo.getUrl(), fileInfo.getExtend()).start();

//        List<ThreadInfo> threadInfos = mDAO.getThreads(mFileInfo.getUrl());
//        ThreadInfo threadInfo = null;
//        Log.d(TAG, mFileInfo.toString());
//        if (threadInfos.size() == 0) {
//            threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
//
//        } else {
//            threadInfo = threadInfos.get(0);
//        }
//        new DownloadThread(threadInfo).start();
    }

    class DownloadThread extends Thread {
//        private ThreadInfo mThreadInfo = null;
//        public DownloadThread(ThreadInfo mThreadInfo) {
//            this.mThreadInfo = mThreadInfo;
//        }
        private String downloadUrl = null;
        private String extend = null;
        public DownloadThread(String downloadUrl, String extend) {
            this.downloadUrl = downloadUrl;
            this.extend = extend;
        }

        @Override
        public void run() {
            long downloadedLength = 0;
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LjDownloader/";
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(directory + fileName + extend);
            if (file.exists()) {
                downloadedLength = file.length();
            }
//            if (!mDAO.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
//                mDAO.insertThread(mThreadInfo);
//            }
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            InputStream inputStream = null;
            try {
                long contentLength = getContentLength(downloadUrl);
                if (contentLength == 0) {
                    //网址无效
                    handler.obtainMessage(INVALID_URL).sendToTarget();
                    return;
                } else if (contentLength == downloadedLength) {
                    //下载已完成
                    handler.obtainMessage(DOWNLOAD_ALREADY).sendToTarget();
                    return;
                }
                URL url = new URL(downloadUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Range", "bytes=" + downloadedLength + "-" + contentLength);
//                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName() + mFileInfo.getExtend());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(downloadedLength);
//                mFinished += mThreadInfo.getFinished();
//                Intent intent = new Intent("ACTION_UPDATE");
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    inputStream = connection.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    int total = 0;
//                    long time = System.currentTimeMillis();
                    while ((len = inputStream.read(buffer)) != -1) {
                        if (isCanceled) {
                            //cancel
                            if (file.exists()) {
                                file.delete();
                            }
                            return;
                        } else if (isPause) {
                            //pause
                            return;
                        } else {
                            raf.write(buffer, 0, len);
                            total += len;
                            int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                            //更新progressbar
                            Intent intent = new Intent("UPDATE_UI_BROADCAST");
                            intent.putExtra("progress", progress);
                            Log.d(TAG, progress + "");
                            mContext.sendBroadcast(intent);


                        }
                    }
                    //下载成功
                    handler.obtainMessage(DOWNLOAD_SUCCESS).sendToTarget();





//                        mFinished += len;
//                        if (System.currentTimeMillis() - time > 500) {
////                            time = System.currentTimeMillis();
//                            mDAO.updateThread(mFileInfo.getUrl(), mThreadInfo.getId(), mFinished);
//                            Log.d(TAG, String.valueOf(mFinished));
//                            Log.d(TAG, mThreadInfo.toString());
//                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
//                            mContext.sendBroadcast(intent);
//                            if (isPause) {
//                                handler.obtainMessage(DownloadService.MEG_UPDATE, mFileInfo).sendToTarget();
//                                return;
//                            }
//                        }
//                        if (isPause) {
//                            Log.d(TAG, "this should not appear");
//                            mDAO.updateThread(mFileInfo.getUrl(), mThreadInfo.getId(),mFinished);
//                            return;
//                        }

//                    intent.putExtra("finished", 100);
//                    mContext.sendBroadcast(intent);
//                    mDAO.deleteThread(mFileInfo.getUrl(), mFileInfo.getId());
                }
            } catch (Exception e ) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (raf != null) {
                        raf.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private long getContentLength(String downloadUrl) throws IOException {
            URL url = new URL(downloadUrl);
            HttpURLConnection connection;
            connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");
            int length = -1;
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                length = connection.getContentLength();
            }
            if (length <= 0) {
                return 0;
            } else {
                return length;
            }
//            File dir = new File(DOWNLOAD_PATH);
//            if (!dir.exists()) {
//                dir.mkdir();
//            }
//            mFileInfo.setLength(length);
//            File file = new File(dir, mFileInfo.getFileName() + mFileInfo.getExtend());
//            randomAccessFile = new RandomAccessFile(file, "rwd");
//            randomAccessFile.setLength(length);
//            mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();
        }
    }


}
