package com.android.push;

import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.android.upgrade.GlobalConsts;

public class WifiUtils {
	private WifiManager wifiManager;
	private WifiInfo wifiInfo;

	// private Context context;

	public WifiUtils(Context context) {
		// this.context = context;
		wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		wifiInfo = wifiManager.getConnectionInfo(); // Wifi信息
		GlobalConsts.wifiState = (WifiState) context;
		
	}
	
	public int getWifiState(){
		return wifiManager.getWifiState();
	}

	/**
	 * 断开当前Wifi连接的网络
	 */
	public void disconnectWifi() {
		int netId = wifiInfo.getNetworkId();
		wifiManager.disableNetwork(netId);
		wifiManager.disconnect();
		wifiInfo = null;
	}

	/**
	 * 打开Wifi
	 */
	public void openNetCard() {
		Log.i("toggle", " openNetCard() ");
		if (!isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
	}

	/**
	 * 关闭Wifi
	 */
	public void closeNetCard() {
		Log.i("toggle", " closeNetCard() ");
		if (isWifiEnabled()) {
			wifiManager.setWifiEnabled(false);
		}
	}

	// 判断Wifi开启状态
	public boolean isWifiEnabled() {
		return wifiManager.isWifiEnabled();
	}

	//
	private WifiConfiguration CreateWifiInfo(String SSID, String Password,
			int Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";

		WifiConfiguration tempConfig = IsExsits(SSID);
		if (tempConfig != null) {
			Log.i("wifi连接", "      删除列表中重复的");
			wifiManager.removeNetwork(tempConfig.networkId);
		}

		if (Type == 1 || Type == 0) // WIFICIPHER_NOPASS
		{
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		}

		if (Type == 2) // WIFICIPHER_WEP : WEP
		{
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + Password + "\"";
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == 3) // WIFICIPHER_WPA
		{
			Log.i("wifi连接", "     WPA加密  ");
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}

	public WifiConfiguration IsExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = wifiManager
				.getConfiguredNetworks();
		if (existingConfigs != null && existingConfigs.size() > 0) {
			for (WifiConfiguration existingConfig : existingConfigs) {
				if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
					return existingConfig;
				}
			}
		}
		return null;
	}

	public boolean connectWifi(String SSID, String Password, int Type) {
		WifiConfiguration config = CreateWifiInfo(SSID, Password, Type);
		int netID = wifiManager.addNetwork(config);
		boolean bRet = wifiManager.enableNetwork(netID, true);
		wifiManager.saveConfiguration();
		wifiInfo = wifiManager.getConnectionInfo(); // 重新获取Wifi信息
		return bRet;
	}

	// 是否已存有密码
	public String passExsits(String SSID, int type) {
		WifiConfiguration config = IsExsits(SSID);
		if (config != null && config.preSharedKey != null) {

			Log.d("WifiUtil", " config.preSharedKey " + config.preSharedKey);
			String pass = config.preSharedKey.replace("\"", "");
			if (pass != null && pass.length() > 0) {
				return pass;
			}
		}
		return null;
	}

	// 连接
	public void reConn(WifiConfiguration config) {
		int netID = wifiManager.addNetwork(config);
		wifiManager.enableNetwork(netID, true);
		wifiManager.saveConfiguration();
	}

	// 清除密码：即取消保存
	public void clearPass(String SSID) {
		/*
		 * WifiConfiguration config = IsExsits(SSID); if(config != null){
		 * config.preSharedKey = ""; int netID = wifiManager.addNetwork(config);
		 * wifiManager.saveConfiguration(); return true; } return false;
		 */
		WifiConfiguration config = CreateWifiInfo(SSID, "", 3);
		wifiManager.addNetwork(config);
		wifiManager.saveConfiguration();
	}

	public interface WifiState {
		void changeWifiText(int i);
	}
}
