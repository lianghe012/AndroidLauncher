package com.android.upgrade;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;


public class UpgradeRom implements Serializable{

	private static final long serialVersionUID = -6738988683454157725L;
	
	public String message;
	public int status;
	public RomUpgradeData data;
	
	public static UpgradeRom fromJson(JSONObject json) throws JSONException{
		UpgradeRom upgrade = new UpgradeRom();
		if (json.has("message")) {
			upgrade.message = json.getString("message");
		}
		if (json.has("status")) {
			upgrade.status = json.getInt("status");
		}
		if (json.has("data")) {
			upgrade.data = RomUpgradeData.fromJson(json.getJSONObject("data"));
		}
		return upgrade;
	}
}
