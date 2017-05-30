package com.android.launcher3.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherApplication;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

public class FactoryInstallUtil {
	private final String initInstallFlagFile = "initInstall";
	private final String infoFilePath = "/system/preinstall/preinstall.txt";
//	private final String infoFilePath = "/sdcard/super_hw/preinstall.txt";
	private String flagFileDir = null;
	private String flagFilePath = null;
	private int initTimes = 0;

	public FactoryInstallUtil(Application application) {
		startFactoryInstall(application);
	}
	public void startFactoryInstall(final Application application){
		try {
			flagFileDir = "/data/data/" + application.getPackageName() + "/files/";
//			flagFileDir = "/sdcard/" + "/super_hw/";
			flagFilePath = flagFileDir + initInstallFlagFile;
			if(isLocalInitInstallFlag(application))
				return;
			final List<ApkInfo> appInfos = getLocalApkInfo();
			if (isAllAppsInstalled(application, appInfos)) {
				initInstallAppSuccess(null);
				return;
			}
			// TODO 开始预安装
			LauncherApplication.isFactoryInstallModel = true;
			new Thread(){
				public void run() {
					try {
						sleep(1500);
					} catch (Exception e) {
					}
					startInstallApp(application, appInfos, this);
				};
			}.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 判断预安装的本地标识,存在标识表示预安装已经通过
	 * @param context
	 * @return
	 */
	private boolean isLocalInitInstallFlag(Context context) {
		try {
			if (!new File(flagFileDir).exists()) {
				new File(flagFileDir).mkdir();
				return false;
			}
			if (new File(flagFilePath).exists())
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 标识预安装已经通过的本地标识
	 */
	private void initInstallAppSuccess(Thread thread) {
		try {
			if(thread != null)
				try {
					thread.sleep(3000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			LauncherApplication.isFactoryInstallModel = false;
			if (!new File(flagFileDir).exists())
				new File(flagFileDir).mkdir();
			if (!new File(flagFilePath).exists())
				new File(flagFilePath).createNewFile();
			if(LauncherAppState.getInstance().mLauncher != null)
				LauncherAppState.getInstance().mLauncher.factoryInstallEnd();
		} catch (Exception e) {
		}
	}
	
	/**
	 * 判断本地预安装APP,是否已经全部安装
	 * @param context
	 * @param appInfos
	 * @return
	 */
	private boolean isAllAppsInstalled(Context context,List<ApkInfo> appInfos){
		if (appInfos != null && appInfos.size() > 0)
			return false;
		List<ApplicationInfo> apps = context.getPackageManager().getInstalledApplications(0);
		if(apps == null || apps.size() <= 0)
			return false;
		for (ApkInfo appInfo : appInfos) {
			if (appInfo != null && !isAppInstalled(appInfo.pkgname, apps))
				return false;
		}
		return true;
	}
	
	private boolean isAppInstalled(String pkgname, List<ApplicationInfo> apps){
		if(pkgname == null || pkgname.trim().length() <= 0)
			return false;
		for(ApplicationInfo appInfo : apps){
			try {
				if(pkgname.equals(appInfo.packageName))
					return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private List<ApkInfo> getLocalApkInfo(){
		File file = new File(infoFilePath);
		if(!file.exists() || !file.isAbsolute())
			return null;
		FileInputStream fos = null;
		InputStreamReader isr = null;
		BufferedReader dr = null;
		List<String> apkInfos = new ArrayList<String>();
		try {
			fos = new FileInputStream(file);
			isr = new InputStreamReader(fos);
			dr = new BufferedReader(isr);
			String line = dr.readLine();
			while (line != null) {
				apkInfos.add(line);
				line = dr.readLine();
			}
			return buildApkInfo(apkInfos);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (dr != null)
					dr.close();
				if (isr != null)
					isr.close();
				if (fos != null)
					fos.close();
				dr = null;
				isr = null;
				fos = null;
			} catch (Exception e2) {
			}
		}
		return null;
	}
	
	private List<ApkInfo> buildApkInfo(List<String> infos) {
		if (infos == null || infos.size() <= 0)
			return null;
		List<ApkInfo> apkInfos = new ArrayList<ApkInfo>();
		for (int line = 0; line < infos.size() / 2; line++) {
			try {
				String apkPath = infos.get(line * 2);
				String pkgName = infos.get(line * 2 + 1);
				if (apkPath != null && apkPath.trim().length() > 0
						&& pkgName != null && pkgName.trim().length() > 0)
					apkInfos.add(new ApkInfo(pkgName, apkPath));
			} catch (Exception e) {
			}
		}
		if(apkInfos != null && apkInfos.size() > 0)
			return apkInfos;
		return null;
	}

	private void startInstallApp(Context context, List<ApkInfo> appInfos, Thread thread){
		if (initTimes++ < 3) {
			List<ApkInfo> uninstalledApps = getUninstalledAppInfo(context, appInfos);
			if(uninstalledApps == null || uninstalledApps.size() <= 0){
				initInstallAppSuccess(thread);
				return;
			}
			for(ApkInfo appInfo : uninstalledApps){
				if(appInfo != null)
					installLocalApk(context, appInfo.localPath);
			}
			if (isAllAppsInstalled(context, uninstalledApps)) {
				initInstallAppSuccess(thread);
			} else {
				startInstallApp(context, appInfos, thread);
			}
		} else {
			// TODO 初始化3次仍未成功,跳过预安装
			initInstallAppSuccess(thread);
		}
	}
	
	private List<ApkInfo> getUninstalledAppInfo(Context context, List<ApkInfo> appInfos){
		List<ApkInfo> uninstalledApps = new ArrayList<ApkInfo>();
		List<ApplicationInfo> apps = context.getPackageManager().getInstalledApplications(0);
		try {
			for(ApkInfo appInfo : appInfos){
				if(appInfo == null || appInfo.pkgname == null)
					continue;
				if(!isAppInstalled(appInfo.pkgname, apps))
					uninstalledApps.add(appInfo);
			}
			return uninstalledApps;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private int installLocalApk(Context context, String path){
		try {
			if(path == null || path.trim().length() <= 0)
				return -1;
			return Runtime.getRuntime().exec("pm install -r " + path).waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
}

class ApkInfo {
	public String pkgname;
	public String localPath;
	
	public ApkInfo(String pkgname, String localPath) {
		Log.e("super_hw", "installtest pkgname:" + pkgname + " localPath:" + localPath);
		this.pkgname = pkgname;
		this.localPath = localPath;
	}
}
