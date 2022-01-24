package com.hms.global;

import android.os.Environment;

import java.io.File;

/**
 * Created by hgsky on 2017-11-22.
 */

public class PmsGlobal {

//    public static String SAVE_DIR = "/Storage/sdcard0/Download/";
    public static String SAVE_OUT_DIR = "/Storage/extSdCard/Download/";
    public static String SAVE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
//    public static String SAVE_DIR = "/mnt/sdcard/Download/";
//    public static String SAVE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
//    public static String SAVE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
//    public static File SAVE_DIR = Environment.getRootDirectory();

//    public static String SAVE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/Download/";

}
