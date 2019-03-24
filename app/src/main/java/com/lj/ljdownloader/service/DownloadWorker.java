package com.lj.ljdownloader.service;

import android.content.Context;
import android.content.Intent;

import com.lj.ljdownloader.db.ThreadDAO;
import com.lj.ljdownloader.db.ThreadDAOImpl;
import com.lj.ljdownloader.entities.FileInfo;
import com.lj.ljdownloader.entities.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class DownloadWorker {
    private Context mContext = null;
    private FileInfo mFileInfo = null;
    private ThreadDAO mDAO = null;
    private int mFinished;
    public boolean isPause = false;

    public DownloadWorker(Context mContext, FileInfo mFileInfo) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        mDAO = new ThreadDAOImpl(mContext);
    }

    public void download() {
        List<ThreadInfo> threadInfos = mDAO.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (threadInfos.size() == 0) {
            threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);

        } else {
            threadInfo = threadInfos.get(0);
        }
        new DownloadThread(threadInfo).start();
    }

    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo = null;
        public DownloadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }

        @Override
        public void run() {
            if (!mDAO.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mDAO.insertThread(mThreadInfo);
            }
            HttpURLConnection connection = null;
            RandomAccessFile raf = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                connection.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                mFinished += mThreadInfo.getFinished();
                Intent intent = new Intent("ACTION_UPDATE");
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    inputStream = connection.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = inputStream.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                        mFinished += len;
                        if (System.currentTimeMillis() - time > 500) {
                            time = System.currentTimeMillis();
                            mDAO.updateThread(mFileInfo.getUrl(), mThreadInfo.getId(), mFinished);
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                            mContext.sendBroadcast(intent);
                            if (isPause) {
                                return;
                            }
                        }
                        if (isPause) {
                            mDAO.updateThread(mFileInfo.getUrl(), mThreadInfo.getId(),mFinished);
                            return;
                        }
                    }
                    intent.putExtra("finished", 100);
                    mContext.sendBroadcast(intent);
                    mDAO.deleteThread(mFileInfo.getUrl(), mFileInfo.getId());
                }
            } catch (Exception e ) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
                try {
                    raf.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
