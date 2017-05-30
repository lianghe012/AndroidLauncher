package com.android.launcher3;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * ChangeReason 判断是否是待机断开的HDMI
 * false 手动拔掉HDMI
 * true  待机断开HDMI
 */

public class HdmiReceiver extends BroadcastReceiver {
    final String PS_APK_CLASS_NAME = LauncherApplication.appPsClassPkg + ".XCCenterActivity";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.HDMI_PLUGGED".equals(intent.getAction())) {
            LauncherApplication.Hdmistate = intent.getBooleanExtra("state", false);
           boolean changeReason = intent.getBooleanExtra("ChangeReason", false);
           Log.e("HdmiReceiver"," changeReason = "+changeReason);
            if (LauncherApplication.Hdmistate) {
            	Log.e("HdmiReceiver","in");
                if (!isCurrentPkg(context)) {
                    ComponentName componentName = new ComponentName(LauncherApplication.appPsPkgname, PS_APK_CLASS_NAME);
                    Intent apk_intent = new Intent();
                    apk_intent.setComponent(componentName);
                    apk_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(apk_intent);
                }
            } else {
               	Log.e("HdmiReceiver","out changeReason = "+changeReason);
                if (!changeReason) {
                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
                    Method method;
                    try {
                        method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage",
                                String.class);
                        method.invoke(am, LauncherApplication.appPsPkgname);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    
    private boolean isCurrentPkg(Context context){
		String activityName = null;
		try {
			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE); 
			List<RunningTaskInfo> forGroundActivity = activityManager.getRunningTasks(1); 
			RunningTaskInfo currentActivity; 
			currentActivity = forGroundActivity.get(0);
			activityName = currentActivity.topActivity.getPackageName(); 
			if(activityName.contains(LauncherApplication.appPsPkgname)){
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}
    
    // 判断XCCenterActivity在不在最顶层
//    @SuppressWarnings("deprecation")
//    private boolean isTopActivity(Context context) {
//        boolean isTop = false;
//        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
//        String className = cn.getClassName();
//        if (className.contains("XCCenterActivity")) {
//            isTop = true;
//        }
//        return isTop;
//    }

}
