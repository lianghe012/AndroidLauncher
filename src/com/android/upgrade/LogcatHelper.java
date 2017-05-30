package com.android.upgrade;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Environment;
// 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
// 显示当前mPID程序的 E和W等级的日志.
// cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
// cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息
//cmds = "logcat -s " + tag;// 打印标签过滤信息
// cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";

/**
 * 读取log工具类
 */
public class LogcatHelper {

    private String PATH_LOGCAT;
    private LogDumper mLogDumper = null;
    private int mPId;
    private String tag;
    private Context mContext;
    Map<String, String> mData = new HashMap<String, String>();

    /**
     * 初始化目录
     * */
    public void init(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "LogMessage";
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = context.getFilesDir().getAbsolutePath() + File.separator + "LogMessage";
        }
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    public LogcatHelper(Context context, String t) {
        this.tag = t;
        mPId = android.os.Process.myPid();
        mContext = context;
        init(context);
    }

    /**
     * @开启打印log线程
     */
    public void start() {
        if (mLogDumper == null)
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
        mLogDumper.start();
    }

    /**
     * @停止log打印
     */
    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    File myFile ;
    private class LogDumper extends Thread {

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPID;
        private FileOutputStream out = null;

        public LogDumper(String pid, String dir) {
            mPID = pid;
            try {
                myFile = new File(dir, tag + ".log");
                out = new FileOutputStream(myFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            cmds = "logcat -s " + tag;// 打印标签过滤信息
        }

        public void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
                String line = null;
                if (myFile==null) {
                    myFile = new File(PATH_LOGCAT, tag + ".log");
                    out = new FileOutputStream(myFile);
                }
                if (myFile!=null) {
                    if (myFile.length()>(1024*1024*4)) {
                        myFile.delete();
                        myFile = new File(PATH_LOGCAT, tag + ".log");
                        myFile.createNewFile();
                        out = new FileOutputStream(myFile);
                    }
                }
                
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (out != null && line.contains(mPID)) {
                        String logMsg = "\n" + MyDate.getDateEN() + " " + line;
                        out.write((logMsg).getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (mReader != null) {
                        mReader.close();
                        mReader = null;
                    }
                    if (logcatProc != null) {
                        logcatProc.destroy();
                        logcatProc = null;
                    }
                    if (out != null) {
                        out.close();
                        out = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据路径读取本地log文件
     * 
     * @param context
     * @param path
     * @return
     * @throws IOException
     */
    private static String readFileByPath(Context context, String path) throws IOException {
        File file = new File(path);
        InputStream is = new FileInputStream(file);
        byte[] bytes = new byte[1024];
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        int length = -1;
        while ((length = is.read(bytes)) != -1) {
            arrayOutputStream.write(bytes, 0, length);
        }
        is.close();
        arrayOutputStream.close();
        String text = new String(arrayOutputStream.toByteArray());
        return text;
    }

    /**
     * 根据tag读取log信息
     * 
     * @param tag
     */
    private void readLogByTag(String tag) {
        try {
            String PATH = PATH_LOGCAT + "/" + tag + ".log";
            File f = new File(PATH_LOGCAT);
            File ff = new File(PATH);
            if (!f.exists()) {
                f.mkdirs();
                if (!ff.exists()) {
                    ff.createNewFile();
                }
            }
            String myLogs = readFileByPath(mContext, PATH);
            // myTextView.setText(myLogs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查看升级lo
     * @param tag
     */
    public void lookUpdateLog(String tag) {
        readLogByTag(tag);
    }

}
