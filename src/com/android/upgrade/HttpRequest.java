package com.android.upgrade;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

public class HttpRequest {
	
	private static String executeForResult(HttpRequestBase request,
			String encoding,int timeOut) {
		DefaultHttpClient client = null;
		try {
			client = new DefaultHttpClient(buildHttpParams(timeOut));
			HttpResponse response = client.execute(request);
			String result = "";
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				result = EntityUtils.toString(response.getEntity(), encoding);
			}
			if(client != null){
				client.clearResponseInterceptors();
				client = null;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			if(client != null){
				client.clearResponseInterceptors();
				client = null;
			}
			return "";
		}
	}
	
	public static String post(String host, List<BasicNameValuePair> params,String encoding,int timeOut) {
		HttpPost httpPost = new HttpPost(host);
		try {
			UrlEncodedFormEntity p_entity = new UrlEncodedFormEntity(params,encoding);
			httpPost.setEntity(p_entity);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return executeForResult(httpPost, encoding,timeOut);
	}

	private static BasicHttpParams buildHttpParams(int timeOut) {
		BasicHttpParams httpParams = new BasicHttpParams();
		if(timeOut > 0){
			HttpConnectionParams.setConnectionTimeout(httpParams,timeOut);
			HttpConnectionParams.setSoTimeout(httpParams,timeOut);
		}else{
			HttpConnectionParams.setConnectionTimeout(httpParams,30*1000);
			HttpConnectionParams.setSoTimeout(httpParams,30*1000);
		}
		return httpParams;
	}
}
