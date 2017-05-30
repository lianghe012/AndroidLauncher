package com.android.launcher3.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class XCSharedPreferences {

	public static String getPreferences(Context context, String key) {
		SharedPreferences preferences = null;
		String str = null;
		try {
			preferences = context.getSharedPreferences("xcPreferences", 0);
			if (preferences != null) {
				str = preferences.getString(key, null);
			}
		} catch (Exception e) {
		}
		return str;
	}

	public static void savePreferences(Context context, String key, String value) {
		SharedPreferences preferences = null;
		preferences = context.getSharedPreferences("xcPreferences", 0);
		Editor editor = null;
		if (preferences != null) {
			editor = preferences.edit();
			if (editor != null) {
				editor.putString(key, value);
				editor.commit();
			}
		}
	}
}
