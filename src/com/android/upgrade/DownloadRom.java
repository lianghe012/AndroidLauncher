package com.android.upgrade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;

public class DownloadRom {
	private Context context;
	private String dUrl;
	private String path;
	private String fileName;
	private String version;
	private SharedPreferences preferences;
	private Handler handler;
	private int totleSize;
	private RomUpgradeData data;
	private int type = 0;//0:升级包下载,1:md5下载
	
	public DownloadRom(Context context ,String url,String path,String fileName,String version,Handler handler,int type,RomUpgradeData data){
		this.context = context;
		this.dUrl = url;
		this.path = path;
		this.fileName = fileName;
		this.version = version;
		this.handler = handler;
		this.data = data;
		this.type = type;
		preferences = context.getSharedPreferences("rom_version", Context.MODE_PRIVATE);
	}
	
	private void init(){
				HttpURLConnection connection = null;
				try {
					URL url = new URL(dUrl);
					connection = (HttpURLConnection)url.openConnection();
					connection.setConnectTimeout(30000);
					connection.setReadTimeout(30000);
					connection.setRequestMethod("GET");
					totleSize = connection.getContentLength();
					Log.e("TAG", totleSize + "------------------------");
					if (type == 0) {
						FileUtils.writeIn(path, "UpdateInfo.txt", version + "\n", false);
						FileUtils.writeIn(path, "UpdateInfo.txt", totleSize + "\n", true);
					}
				} catch (MalformedURLException e) {
					GlobalConsts.ROM_DOWNLOADING = false;
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					GlobalConsts.ROM_DOWNLOADING = false;
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					connection.disconnect();
				}
	}
	
    public void download() {
        new Thread() {
            public void run() {
                if (type == 0) {
                }
                init();
                if (totleSize > 0) {
                    //判断存储空间是否足够
                    if (readSystem(totleSize)) {
                        GlobalConsts.ROM_DOWNLOADING = true;
                        File file = new File(path + fileName);
                        String locationVersion = getLocationVersion();
                        int locationSize = getLocationTotleSize();
                        if (locationVersion.equals(version) && locationSize == totleSize && type == 0) {
                            Log.e("upgrdeRom", locationVersion + "/" + version + "/" + locationSize + "/" + totleSize);
                            if (file.exists() && file.length() == totleSize) {
                                Message msg = handler.obtainMessage();
                                msg.what = 600;
                                handler.sendMessage(msg);
                                if (type == 0) {
                                    // GlobalConsts.ROM_DOWNLOADED = true;
                                }
                                return;
                            }
                        } else {
                            if (file.exists()) {
                                file.delete();
                            }
                        }

                        HttpURLConnection connection = null;
                        InputStream in = null;
                        RandomAccessFile randomAccessFile = null;
                        try {
                            int completeSize = 0;
                            if (file.exists()) {
                                completeSize = (int) file.length();
                            }
                            Log.e("upgrdeRom", dUrl + "--------------------url");
                            URL url = new URL(dUrl);
                            connection = (HttpURLConnection) url.openConnection();
                            connection.setConnectTimeout(30000);
                            connection.setReadTimeout(30000);
                            connection.setRequestMethod("GET");
                            connection.setRequestProperty("RANGE", "bytes=" + completeSize + "-" + (totleSize - 1));

                            Log.e("upgrdeRom", connection.getContentLength() + "--------------------totleSize");
                            if (type == 0) {
                                setVersion(version);
                                setSize(totleSize);
                            }
                            randomAccessFile = new RandomAccessFile(file, "rw");
                            randomAccessFile.seek(completeSize);
                            in = connection.getInputStream();
                            byte[] buffer = new byte[1024 * 4];
                            int length = -1;
                            while ((length = in.read(buffer)) != -1) {
                                randomAccessFile.write(buffer, 0, length);
                                completeSize += length;
                                // Log.e("TAG", completeSize + "");
                            }
                            Message msg = handler.obtainMessage();
                            if (type == 0) {
                                msg.what = 600;
                            } else {
                                msg.what = 601;
                                msg.obj = data;
                            }
                            handler.sendMessage(msg);
                            if (type == 0) {
                                // GlobalConsts.ROM_DOWNLOADED = true;
                            }
                        } catch (MalformedURLException e) {
                            Log.e("upgrdeRom", " MalformedURLException");
//                            if (logCatHelper != null) {
//                                logCatHelper.stop();
//                            }
                            // if (type == 0) {
                            // GlobalConsts.HAS_NEW_VERSION = true;
                            // }
                            GlobalConsts.ROM_DOWNLOADING = false;
                            e.printStackTrace();
                        } catch (IOException e) {
                            Log.e("upgrdeRom", " IOException");
//                            if (logCatHelper != null) {
//                                logCatHelper.stop();
//                            }
                            // if (type == 0) {
                            // GlobalConsts.HAS_NEW_VERSION = true;
                            // }
                            GlobalConsts.ROM_DOWNLOADING = false;
                            e.printStackTrace();
                        } finally {
                            try {
                                if (type == 0) {
                                }
                                in.close();
                                randomAccessFile.close();
                            } catch (Exception e2) {
                            }
                            connection.disconnect();
                        }
                    }else {//剩余存储空间不够
                        Message msg = handler.obtainMessage();
                        msg.what = 606;
                        msg.arg1= clearSize;
                        handler.sendMessage(msg);
                        return;
                    }
                }
            }
            private int clearSize = 0;
            /**
             * 获取系统剩余可用空间并和rom包进行比较
             * 如果空间够用返回true
             * 空间不够返回false
             * 存储空间不存在返回false
             */
            private boolean readSystem(long romSize) {
                String state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    File sdcardDir = Environment.getExternalStorageDirectory();
                    StatFs sf = new StatFs(sdcardDir.getPath());
                    long blockSize = sf.getBlockSize();
                    long availCount = sf.getAvailableBlocks();
                    long canUse = availCount * blockSize;
                    long romCountSize = romSize + 50 * 1024 * 1024;
                    // 判断可用大小
                    if (canUse >= romCountSize) {
                        return true;
                    } else {
                        clearSize = (int)((romCountSize -canUse)/1024/1024);
                        return false;
                    }
                }else {
                    return false;
                }
            }  
        }.start();
    }
	
	private String getLocationVersion(){
		String version = preferences.getString("version", "0.0.0");
		return version;
	}
	private int getLocationTotleSize(){
		int size = preferences.getInt("totle_size", 0);
		return size;
	}
	
	private void setVersion(String version){
		Editor editor = preferences.edit();
		editor.putString("version", version);
		editor.commit();
	}
	
	private void setSize(int size){
		Editor editor = preferences.edit();
		editor.putInt("totle_size", size);
		editor.commit();
	}
}
