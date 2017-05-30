package com.android.launcher3.util;

import java.lang.reflect.Method;

import android.os.IBinder;
import android.os.ServiceManager;

public class SystemUIUtil {
	
	public static void removeNavigationBar(){
		try {
    		Class mclass0 = Class.forName("com.android.internal.statusbar.IStatusBarService");
    		Class mclass = Class.forName("com.android.internal.statusbar.IStatusBarService$Stub");
    		Method method = mclass.getMethod("asInterface", new Class[] {IBinder.class});
    		Object obj = method.invoke(mclass, new Object[]{ServiceManager.getService("statusbar")});
    		Method method2 = mclass0.getMethod("removeNavigationBar", new Class[] {});
    		Object obj2 = method2.invoke(obj, new Object[]{});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
