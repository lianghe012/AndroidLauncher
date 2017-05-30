package com.android.upgrade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class GetMac {
	public static void getMacAddress() {// 如果获取硬件Id失败则返回默认的
		new Thread() {
			public void run() {
				String mac = null;
				if(mac== null || mac.length() <6){
					final String path = "/sys/class/net/eth0/address";
					File file = new File(path);
					if (file.exists()) {
						try {
							mac  = new BufferedReader(new FileReader(file)).readLine();
							if (mac != null && mac.length() > 0
									&& !mac.equals("")) {
								GlobalConsts.mac = mac;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				
				if(mac== null ||mac.length() <6){
					final String path2 = "/sys/class/net/wlan0/address";
					File file2 = new File(path2);
					if (file2.exists()) {
						try {
							mac = new BufferedReader(new FileReader(file2)).readLine();
							if (mac != null && mac.length() > 0
									&& !mac.equals("")) {
								GlobalConsts.mac = "w" + mac;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};
		}.start();
	}

}