package com.android.upgrade;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.util.Log;

public class XCAPI {
	private static final String PARAM_NAME = "$$FACES$$";
	public static final String BASE_URL_XC = "http://pt1905.gitv.tv/tvstore/";
	public static final String HOST_URL = BASE_URL_XC + "faces.do";
	public static String mServer = "881";//servsrID(暂时不变)//805:881     812:882
	public static int mUser = 0;

	public static UpgradeRom upgrdeRom(String rom, String version,String SerialNumber)
			throws Exception {
		List<BasicNameValuePair> pair = new ArrayList<BasicNameValuePair>();
		pair.add(new BasicNameValuePair("rom", rom));
		pair.add(new BasicNameValuePair("version", version));
		pair.add(new BasicNameValuePair("serialNumber", SerialNumber));
		String ret = postData(buildRequestContent("romUpgrade", pair),false);
		Log.d("upgrdeRom"," 返回信息 : "+ret);
		if (ret != null) {
			return UpgradeRom.fromJson(new JSONObject(ret));
		} else {
			return null;
		}
	}
	
	private static String buildRequestContent(String method,
			List<BasicNameValuePair> pair) throws Exception {
		JSONObject jHead = buildHead(method);
		JSONObject jBody = new JSONObject();
		if (pair != null && pair.size() != 0) {
			for (int i = 0; i < pair.size(); i++) {
				jBody.put(pair.get(i).getName(), pair.get(i).getValue());
			}
		}
		JSONObject jRequest = new JSONObject();
		jRequest.put("head", jHead);
		jRequest.put("body", jBody);
		Log.d("TAG", jRequest.toString() + "");
		if (method.equals("romUpgrade")) {
		    Log.d("upgrdeRom"," 请求接口 : "+jRequest);
        }
		return jRequest.toString();
	}
	
	private static JSONObject buildHead(String method) throws Exception {
		JSONObject jHead = new JSONObject();
		jHead.put("method", method);
		jHead.put("server", mServer);
		jHead.put("version", "0.2");
		if (mUser != 0) {
			jHead.put("user",  String.valueOf(mUser));
		}
		
		return jHead;
	}
	
	private static String postData(String param,boolean saveLog) {
		List<BasicNameValuePair> pair = new ArrayList<BasicNameValuePair>();
		pair.add(new BasicNameValuePair(PARAM_NAME, param));
		Log.d("UpgradeRom", "postData = "+pair + "");
		return HttpRequest.post(HOST_URL, pair, HTTP.UTF_8,0);
	}
}
