package com.android.upgrade;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class RomUpgradeData implements Serializable{
	private static final long serialVersionUID = 1860053523167622121L;
	
	public boolean upgrade;
	public boolean forced;
	public String version;
	public String desc;
	public String downloadUrl;
	
	public static RomUpgradeData fromJson(JSONObject json) throws JSONException{
		RomUpgradeData data = new RomUpgradeData();
		if (json.has("upgrade")) {
			data.upgrade = json.getBoolean("upgrade");
		}
		if (json.has("forced")) {
			data.forced = json.getBoolean("forced");
		}
		if (json.has("version")) {
			data.version = json.getString("version");
		}
		if (json.has("desc")) {
			data.desc = json.getString("desc");
		}
		if (json.has("link")) {
			data.downloadUrl = json.getString("link");
		}
		return data;
	}
	
}
