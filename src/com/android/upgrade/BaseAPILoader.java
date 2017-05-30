package com.android.upgrade;

import android.content.AsyncTaskLoader;
import android.content.Context;

public abstract class BaseAPILoader<T> extends AsyncTaskLoader<T> {

	public BaseAPILoader(Context context) {
		super(context);
	}

	public abstract T loadInBackground();

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	@Override
	public void onCanceled(T data) {
		super.onCanceled(data);
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	@Override
	protected void onReset() {
		stopLoading();

	}
}
