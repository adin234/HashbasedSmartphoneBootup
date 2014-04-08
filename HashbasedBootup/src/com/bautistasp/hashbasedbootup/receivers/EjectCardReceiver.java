package com.bautistasp.hashbasedbootup.receivers;

import com.bautistasp.hashbasedbootup.RegisterActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EjectCardReceiver extends BroadcastReceiver {
	public static final String TAG = "EjectCardReceiver";
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		if(RegisterActivity.registerActivity != null)
			 RegisterActivity.registerActivity.finish();
	}

}
