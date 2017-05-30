package com.android.push;

import org.json.JSONException;
import org.json.JSONObject;

public class PushJson {

    public String title;
    public String msg;
    public int code;
    public String sn;
    public String account;
    public String pwd;

    public static PushJson fromJson(JSONObject json) throws JSONException {
        PushJson data = new PushJson();
        if (json.has("title")) {
            data.title = json.getString("title");
        }

        if (json.has("code")) {
            data.code = json.getInt("code");
        }
        
        if (data.code==5) {
            if (json.has("msg")) {
                JSONObject object = json.getJSONObject("msg");
                if (object.has("account")) {
                    data.account = object.getString("account");
                }
                if (object.has("pwd")) {
                    data.pwd = object.getString("pwd");
                }
                if (object.has("sn")) {
                    data.sn = object.getString("sn");
                }
            }
        }else {
            if (json.has("msg")) {
                data.msg = json.getString("msg");
            }
        }

       
       
        return data;
    }

}
