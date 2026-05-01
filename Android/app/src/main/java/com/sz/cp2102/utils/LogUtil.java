package com.sz.cp2102.utils;

import android.os.Environment;
import android.util.Log;

import com.sz.cp2102.fragment.LogFragment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class LogUtil {

    private static final int LEV_D = 1;
    private static final int LEV_W = 2;

    public static String getFilePath(String tag) {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();

        Log.e("currentTimeMillis",filePath+tag);
        return filePath+tag  ;
    }

    /**
     * Use a constant to decide whether to write logs and avoid creating many junk log files
     * @param msg Log message to write
     */
    public static void writerlog(String msg) {
        if (LEV_W == 2) {
            // Destination file path
            final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            FileWriter fw = null;
            BufferedWriter bw = null;

            try {
                //Create directory
                File dir = new File(filePath, "cp2102Log");
                if (!dir.exists()) {
                    dir.mkdir();
                }
                long time = System.currentTimeMillis();
                SimpleDateFormat sDateFormat =  new  SimpleDateFormat("yyyyMMddhhmmss");   // This format can be customized
                String  date =  sDateFormat.format(time  );
                //Create file
                Log.e("currentTimeMillis",date);
                File file = new File(dir, date.trim()+".txt");
                if (!file.exists()) {
                    file.createNewFile();
                }
                //Write to log file
                fw = new FileWriter(file, true);
                bw = new BufferedWriter(fw);
                bw.write( msg + "\n");
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }


    }


    // Data directory
    private final String dataFile = "bleLog";

    /**
     * Create directory
     * */
    private static File makeDataFile() {
        File file = null;
        final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        try {
            file =  new File(filePath, "bleLog");
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
        return  file;
    }

    /**
     * Get all files under the dataFile directory
     * */
    public static ArrayList<logFile> getAllDataFileName(){
        try{


        // Directory path
        String collectionPath = makeDataFile().getPath();

        ArrayList<logFile> fileList = new ArrayList<>();

        File file = new File(collectionPath);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                System.out.println("文     件：" + tempList[i].getName());
                // tempList[i].toString();// Path
                // tempList[i].getName();// File name
                // File name
                logFile logFile=new logFile();
                String fileName = tempList[i].getName();
                logFile.setName(fileName);
                logFile.setPath(tempList[i].getPath());
                    // File size
                    // String fileSize = FileSizeUtil.getAutoFileOrFilesSize(tempList[i].toString());
                    fileList.add(logFile);
            }
        }

        return fileList;
        }catch (Exception e){
            return new ArrayList<logFile>();
        }
    }
}
