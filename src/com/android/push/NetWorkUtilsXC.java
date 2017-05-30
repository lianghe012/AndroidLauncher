package com.android.push;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.android.upgrade.GlobalConsts;

public class NetWorkUtilsXC {
	private Context ct;

	public NetWorkUtilsXC(Context context) {
		ct = context;
	}

	// 单独判断网络
	private BroadcastReceiver receiverNetState = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int i = isNetworkAvailable(ct);
			Log.e("网络测试", "GlobalConsts.networkFlag----------"+GlobalConsts.networkFlag);
			Log.e("网络测试", "i>>>>"+i);
			GlobalConsts.connectType = i;
			if (i == -1) {
				lastFlag = flag;
				GlobalConsts.connectFirstShow = false;
				GlobalConsts.networkFlag = false;
				exeOperation(1, i);				
			} else if (i != -1) {
				if (lastFlag != flag || lastFlag == 0) { //首次lastFlag == 0
					GlobalConsts.networkFlag = true;
					//*********
					if(lastConnFlag != flag && lastFlag != 0){							//去除首次进入launcher
						GlobalConsts.networkChangeTime = System.currentTimeMillis();
					}
					lastConnFlag = flag;
					//*********
					exeOperation(2, i);				
					if (!GlobalConsts.connectFirstShow && lastFlag != 2) { //无线到有线网络变化,不弹框，仅图标变化
						exeOperation(3, i);				
					} else {
						GlobalConsts.connectFirstShow = false;
					}
					lastFlag = flag;
				}
			}
			if (GlobalConsts.wifiState != null) {
				GlobalConsts.wifiState.changeWifiText(i);
			}
		}
	};

	public void regNetReceiver() {
		IntentFilter filterNet = new IntentFilter();
		filterNet.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		ct.registerReceiver(receiverNetState, filterNet);
	}

	public void unregNetReceiver() {   
		ct.unregisterReceiver(receiverNetState);
	}

	private static int flag; // 当前网络 1有限网 2无限网 3无网络 4手机
	public static int lastFlag; // 上一网络----包含无网的情况
	private static int lastConnFlag;		//上一连接网络----为记录不同网络切换成功的时间

	public static int isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connectivity
				.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		NetworkInfo netInfo2 = connectivity
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo netInfo3 = connectivity
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		State stateWlan = null, stateWifi = null, traffic = null;

		if (netInfo != null) {
			stateWlan = netInfo.getState();
		}
		if (netInfo2 != null && stateWlan != State.CONNECTED) {
			stateWifi = netInfo2.getState();
			if (stateWifi == State.CONNECTED) {
				GlobalConsts.currentFlag = 1;
				GlobalConsts.wifiFlag = 1;
			} else if (stateWifi == State.CONNECTING) {
				GlobalConsts.currentFlag = 2;
				GlobalConsts.wifiFlag = 2;
			} else if (stateWifi == State.DISCONNECTED
					|| stateWifi == State.DISCONNECTING) {
				// 断开
				if (GlobalConsts.currentFlag == 1) {
					GlobalConsts.wifiFlag = 3;
				} else {
					GlobalConsts.wifiFlag = 4;
				}
			}
		}
		if (netInfo3 != null) {
			traffic = netInfo3.getState();
			Log.e("traffic", "traffic－－－　" + traffic);
		}
		if (stateWlan == State.CONNECTED) {
			flag = 1;
			return 0;
		} else if (stateWlan == State.UNKNOWN
				|| stateWlan == State.DISCONNECTED) {
			if (stateWifi == State.CONNECTED) {
				WifiManager wifi = (WifiManager) context 
						.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifi.getConnectionInfo();
				Log.i("MyWifi", "wifiInfo = " + wifiInfo.getRssi());
				flag = 2;
				if (Math.abs(wifiInfo.getRssi()) < 50) {
					return 2;
				} else if (Math.abs(wifiInfo.getRssi()) < 70
						&& Math.abs(wifiInfo.getRssi()) > 50) {
					return 3;
				} else if (Math.abs(wifiInfo.getRssi()) < 100
						&& Math.abs(wifiInfo.getRssi()) > 70) {
					return 4;
				}
				return 1;
			} else if (stateWifi == State.UNKNOWN
					|| stateWifi == State.DISCONNECTED) {
				if (traffic == State.CONNECTED) {
					Log.e("traffic", "traffic－－－执行！");
					flag = 4;
					return 5;
				}
				flag = 3;
				return -1;
			}
			flag = 3;
			return -1;
		} else {
			if (stateWifi == State.CONNECTED) {
				WifiManager wifi = (WifiManager) context
						.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifi.getConnectionInfo();
				Log.i("MyWifi", "wifiInfo = " + wifiInfo.getRssi());
				flag = 2;
				if (Math.abs(wifiInfo.getRssi()) < 50) {
					return 2;
				} else if (Math.abs(wifiInfo.getRssi()) < 70
						&& Math.abs(wifiInfo.getRssi()) > 50) {
					return 3;
				} else if (Math.abs(wifiInfo.getRssi()) < 100
						&& Math.abs(wifiInfo.getRssi()) > 70) {
					return 4;
				}
				return 1;
			} else if (stateWifi == State.UNKNOWN
					|| stateWifi == State.DISCONNECTED) {
				if (traffic == State.CONNECTED) {
					Log.e("traffic", "traffic－－－执行！");
					flag = 4;
					return 5;
				}
				flag = 3;
				return -1;
			}
			flag = 3;
			return -1;
		}

	}

	// 判断网络是否连接
	public static boolean isConnectedNetwork(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] netInfos = connectivity.getAllNetworkInfo();
			for (NetworkInfo networkInfo : netInfos) {
				if (networkInfo.getState() == State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 判断指定Activity是否是当前运行Activity
	 * @param context
	 * @param className  Activity全称(包名+类名) 例如: com.android.launcher1905.XCMainactivity
	 * @return
	 */
	public static boolean isTopActivity(Context context, String className){
		ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		if(am != null && am.getRunningTasks(1) != null){
			RunningTaskInfo runTaskInfo = am.getRunningTasks(1).get(0);
			String runningName = runTaskInfo.topActivity.getClassName();
			if(className != null && className.equals(runningName)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @修改方法
	 *  TODO 处理　公共网络监听的处理方法，这里添加的就是TopActivity 
	 *  这里用list去处理是因为有些类 不论是不是最顶层的Activity 都要处理一些操作
	 */
	public static void addDetectionNetworkActivity(NetworkStatus networkStatus){
		GlobalConsts.netWorkList.add(networkStatus);
	}
	
	/**
	 * @修改方法
	 * TODO 用于移出 对activity 的网络检测监听
	 */
	public static void removeNetworkDetection(NetworkStatus networkStatus){
		try {
			Log.d("codeDownload", "remove networkStatus＝ " + networkStatus);
			GlobalConsts.netWorkList.remove(networkStatus);
			Log.d("codeDownload", "GlobalConsts.netWorkList size>>" + GlobalConsts.netWorkList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param flag  操作类型   1断网  2改变图标  3有网
	 * @param i  网络标志
	 */
	private void exeOperation(int flag, int i){
		if (GlobalConsts.netWorkList != null && GlobalConsts.netWorkList.size() > 0){
			int netWorkSize = GlobalConsts.netWorkList.size();
			switch (flag) {
			case 1:
				for (int j = 0; j < netWorkSize; j++) {
					NetworkStatus detection = GlobalConsts.netWorkList.get(j);
					if (detection!=null) {
						detection.ondisconnect();
						detection.reSetStatusIcon(i);	  	
					}
				}
				break;
			case 2:
				for (int j = 0; j < netWorkSize; j++) {
					NetworkStatus detection = GlobalConsts.netWorkList.get(j);
					if (detection!=null) {
						detection.reSetStatusIcon(i);	
					}
				}
				break;
			case 3:
				for (int j = 0; j < netWorkSize; j++) {
					NetworkStatus detection = GlobalConsts.netWorkList.get(j);
					if (detection!=null) {
						detection.onconnect();
					} 
				}
				break;
			default:
				break;
			}
		}
	}	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
