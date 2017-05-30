/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;

import com.android.launcher3.util.FactoryInstallUtil;
import com.android.upgrade.GetMac;
import com.android.upgrade.GlobalConsts;
import com.android.upgrade.XCDirectory;

public class LauncherApplication extends Application {
	
	public static int restartTimes = 0;
	public static boolean copyComple = false;
	public static boolean isStartInit = false;
	public static boolean tvApp1905_1Path = false;
//	public static CustomDialog customDialog = null;
	public static LauncherApplication application;
	public static boolean Hdmistate = false;
	public static String appPsClassPkg = "com.android.launcherxc1905";//代码包名
	public static String appPsPkgname = "com.android.launcher1905";//程序包名
	public static boolean isFactoryModel = false;
	public static boolean isFactoryInstallModel = false;
	public static boolean WallpaperFlag = false;
	
    @Override
    public void onCreate() {
        super.onCreate();
    	JPushInterface.setDebugMode(true);
	    JPushInterface.init(this);
        application = this;
        initVariable();
        LauncherAppState.setApplicationContext(this);
        LauncherAppState.getInstance();
        initInstallApps();
        
        //2016-1-12 yf 新增 用于获取 MAC 地址
        GetMac.getMacAddress();
        startExceptionRecord();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LauncherAppState.getInstance().onTerminate();
    }
    
    private void initVariable() {
		XCDirectory.initDirectory(this);
		String rom_name = "ro.xcbuild.display.id";
		String rom_version = "ro.xcbuild.version.num";
		String rom_sn = "ro.serialno";
		String rom_type = "ro.build.type";
		GlobalConsts.ROM_NAME = get(rom_name);
		GlobalConsts.ROM_VERSION = get(rom_version);
//		GlobalConsts.ROM_NAME = "S805-rel";
//		GlobalConsts.ROM_VERSION = "1.0.0";
		GlobalConsts.ROM_SN = get(rom_sn);
		GlobalConsts.ROM_TYPE = get(rom_type);
		Log.d("TAG", GlobalConsts.ROM_NAME + "/" + GlobalConsts.ROM_VERSION + "/" + GlobalConsts.ROM_SN + "/" + GlobalConsts.ROM_TYPE);
	}
	
	public String get(String key) {
	    Class<?> clazz;
	    try {
	        clazz = Class.forName("android.os.SystemProperties");
	        Method method = clazz.getDeclaredMethod("get", String.class);
	        return (String) method.invoke(clazz.newInstance(), key);
	        } catch (ClassNotFoundException e) {
	            e.printStackTrace();
	        } catch (NoSuchMethodException e) {
	            e.printStackTrace();
	        } catch (IllegalAccessException e) {
	            e.printStackTrace();
	        } catch (IllegalArgumentException e) {
	            e.printStackTrace();
	        } catch (InvocationTargetException e) {
	            e.printStackTrace();
	        } catch (InstantiationException e) {
	            e.printStackTrace();
	        }
	        return "";
	}
	
//	public static Handler handler = new Handler(){
//    	public void handleMessage(Message msg) {
//    		if(msg.what == 1){
////    			if(customDialog != null && customDialog.isShowing()){
////    				try {
////     					customDialog.cancel();
////        				customDialog = null;	
////					} catch (Exception e) {
////						e.printStackTrace();
////					}
////    			}
////    			 customDialog = new CustomDialog(getApplicationContext(), R.layout.dialog_layout, R.style.DialogTheme);
////    			 customDialog.setCanceledOnTouchOutside(false);
////    			 customDialog.setCanceledOnTouchOutside(false);
////    			if(customDialog == null)
////    				return;
////    			 customDialog.show();
//    		}else if(msg.what == 2){
////    			LauncherApplication.copyComple = true;
////    			LauncherApplication.isStartInit = false;
////    			if(customDialog == null)
////    				return;
////    			if(customDialog != null && customDialog.isShowing()){
////    				try {
////    					customDialog.cancel();
////        				customDialog = null;	
////					} catch (Exception e) {
////						e.printStackTrace();
////					}
////    			}
//    		} else if(msg.what == 3){
////    			handler.sendEmptyMessage(1);
////    			if(handler.hasMessages(2)) {
////    				handler.removeMessages(2);
////    				handler.sendEmptyMessageDelayed(2, 30 * 1000);
////    			}
//    		}
//    	};
//    };
    
    private void initInstallApps(){
    	Log.e("super_hw", "LauncherApplication_initInstallApps()");
    	try {
    		new FactoryInstallUtil(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * @deprecated
     * */
    private void initInstallAppsOld(){
    	Log.e("super_hw", "LauncherApplication_startInstallApps()");
    		new Thread(new Runnable() {
				public void run() {
					try {
//						Thread.sleep(5 * 1000);
						String strCommand = "preinstall.sh";
						Runtime.getRuntime().exec(strCommand);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
    }
    
	public static void START_GAME_BY_XC(final Context context) {
			try {
				  ComponentName componentName = new ComponentName(LauncherApplication.appPsPkgname, LauncherApplication.appPsClassPkg + ".XCCenterActivity");
		          Intent apk_intent = new Intent();
		          apk_intent.setComponent(componentName);
		          apk_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		          context.startActivity(apk_intent);
			} catch (Exception e) {
				
			}
		
	}

	private void startExceptionRecord() {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				try {
					Log.e("super_hw", "myException");
					List<String> exceptionMsgs = new ArrayList<String>();
					String strErr = null;
					if(ex == null)
						return;
					strErr = ex.toString();
					if(strErr == null || strErr.trim().length() <= 0)
						return;
					exceptionMsgs.add(strErr);
					if(ex.getStackTrace() == null)
						return;
					for(StackTraceElement stack : ex.getStackTrace()){
						strErr = "	at " + stack.getClassName() + "." + stack.getMethodName()
								+ "(" + stack.getFileName() + ":" + stack.getLineNumber() + ")";
						exceptionMsgs.add(strErr);
					}
					saveExceptionInfo(exceptionMsgs);
				} catch (Exception e) {
					e.printStackTrace();
				}
				ex.printStackTrace();
			}
		});
	}

	private void saveExceptionInfo(List<String> exceptionMsgs){
		final String exceptionRecord = "/sdcard/Exceptions.txt";
		File file = new File(exceptionRecord);
		try {
			if(!file.exists() || file.length() >= (50 << 10))
				file.createNewFile();
			String strContent = "";
			// TODO init exception time
			long time = System.currentTimeMillis();
			strContent = "------------------ " + new Date(time).toString() + " -----------------";
			for(String str : exceptionMsgs){
				strContent += ("\n" + str);
			}
			strContent += "\n------------------ version:" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName + " -----------------\n\n";
			appendLocalRecord(exceptionRecord, strContent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void appendLocalRecord(String fileName, String content) {
		try {
			FileWriter writer = new FileWriter(fileName, true);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}