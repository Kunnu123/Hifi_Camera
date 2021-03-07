package com.hificamera.thecamhi.base;

import java.io.BufferedReader;  
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;  
import java.io.InputStreamReader;  

import com.hificamera.R;
  
import android.content.Context;  
import android.os.Environment;


public class LogcatHelper {

    private static LogcatHelper INSTANCE = null;
    private static String PATH_LOGCAT;
    private LogDumper mLogDumper = null;
    private int mPId;
    private String appName="CamHi";
    private Context context;

    public void init(Context context) {


        File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
        File sargetFolder = new File(rootFolder.getAbsolutePath() + "/Snapshot/");
        //	File targetFolder=new File(yargetFolder.getAbsolutePath()+"/"+getTimeForNow()+"/");
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }
        if (!sargetFolder.exists()) {
            sargetFolder.mkdirs();
        }

        PATH_LOGCAT = sargetFolder.getAbsoluteFile().toString();

        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static LogcatHelper getInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = new LogcatHelper(context);
        }
        return INSTANCE;
    }

    private LogcatHelper(Context context) {
        this.context=context;
        init(context);
        mPId = android.os.Process.myPid();
    }

    public void start() {
        if (mLogDumper == null)
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
        mLogDumper.start();
    }

    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    private class LogDumper extends Thread {

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPID;
        private FileOutputStream out = null;

        public LogDumper(String pid, String dir) {
            mPID = pid;
            appName=context.getResources().getString(R.string.app_name);
            File file=
                    new File(dir, appName+"-"
                            + MyDate.getDateEN2() + ".log");

            try {
                file.createNewFile();
                out = new FileOutputStream(file);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            cmds = "logcat  | grep \"(" + mPID + ")\"";

        }

        public void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {
            try {

                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(
                        logcatProc.getInputStream()), 1024);
                String line = null;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (out != null && line.contains(mPID)) {
                        out.write((MyDate.getDateEN() + "  " + line + "\n")
                                .getBytes());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }

            }

        }

    }

}  