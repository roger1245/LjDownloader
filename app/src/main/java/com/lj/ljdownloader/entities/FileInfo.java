package com.lj.ljdownloader.entities;

import java.io.Serializable;

public class FileInfo implements Serializable {
//    private int id;
    private String url;
//    private String fileName;
//    private int length;
//    private int finished;
//
    private String extend;

    public FileInfo(String url, String extend) {
        this.url = url;
        this.extend = extend;
    }

    //
//    public FileInfo(int id, String url, String fileName, int length, int finished, String extend) {
//        this.id = id;
//        this.url = url;
//        this.fileName = fileName;
//        this.length = length;
//        this.finished = finished;
//        this.extend = extend;
//    }
//
    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
//
//    public String getFileName() {
//        return fileName;
//    }
//
//    public void setFileName(String fileName) {
//        this.fileName = fileName;
//    }
//
//    public int getLength() {
//        return length;
//    }
//
//    public void setLength(int length) {
//        this.length = length;
//    }
//
//    public int getFinished() {
//        return finished;
//    }
//
//    public void setFinished(int finished) {
//        this.finished = finished;
//    }
//
//
//    @Override
//    public String toString() {
//        return id + "  " + url + "  " + fileName + "  " + length + "  " + finished + extend;
//    }
}
