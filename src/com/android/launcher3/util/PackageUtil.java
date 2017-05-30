package com.android.launcher3.util;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;


public class PackageUtil {
	
	public static String getTopPackageName(Context context){
		try {
			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE); 
			List<RunningTaskInfo> forGroundActivity = activityManager.getRunningTasks(1); 
			RunningTaskInfo currentActivity; 
			currentActivity = forGroundActivity.get(0);
			return currentActivity.topActivity.getPackageName(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
