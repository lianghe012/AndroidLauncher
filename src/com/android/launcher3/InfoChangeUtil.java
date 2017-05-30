package com.android.launcher3;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class InfoChangeUtil {
	
	private static HashMap<String, AppInfo> apps = new HashMap<String, AppInfo>();
	
	public static AppInfo getApplicationInfoByIntent(LauncherApplication context, PackageManager pm, List<ResolveInfo> apps, ShortcutInfo info){
		AppInfo appInfo = null;
		try {
	    	for(ResolveInfo rInfo : apps){
	    			appInfo = getApplicationInfo(pm, rInfo, new IconCache(context), new HashMap<Object, CharSequence>());
	    			if (appInfo!= null && appInfo.title.equals(info.title)){
	    				return appInfo;
	    			}
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appInfo;
	}
	
	private static AppInfo getApplicationInfo(PackageManager pm, ResolveInfo info, IconCache iconCache,
            HashMap<Object, CharSequence> labelCache){
		AppInfo temp = null;
//		temp = apps.get(info.activityInfo.packageName);
//		if(temp != null)
//			return temp;
//		temp = new ApplicationInfo(pm, info, iconCache, labelCache);
//		apps.put(info.activityInfo.packageName, temp);
		return temp;
	}
	
}
