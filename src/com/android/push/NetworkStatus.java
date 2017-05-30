package com.android.push;

public interface NetworkStatus {
	void ondisconnect();

	void onconnect();

	void reSetStatusIcon(int i);
}