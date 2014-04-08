package com.bautistasp.hashbasedbootup.receivers;

import com.bautistasp.hashbasedbootup.utilities.Utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	public static final String TAG = "BootReceiver";

	@Override
	public void onReceive(Context context, Intent arg1) {
		Log.d(TAG, "received " + arg1.getAction());
		SharedPreferences pref = context.getSharedPreferences(
				"hbbuPref", 0);
		if(arg1.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			SharedPreferences.Editor editor = pref.edit();
			editor.putBoolean("fromBoot", true);
			editor.commit();
		}
			
		if (arg1.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
			if (pref.getBoolean("fromBoot", false) && Utility.appEnabled(context)
					&& (!Utility.isSdPresent() || (Utility.isSdPresent() && !Utility
							.validHash()))) {

				/*
				 * Log.d(TAG, "appenabled:" + Utility.appEnabled(context) +
				 * "|sdpresent:" + Utility.isSdPresent() + "|validhash:" +
				 * Utility.validHash());
				 */
				//try {
					pref.edit().remove("fromBoot").commit();
					//if (!RootTools.isAccessGiven()) {
						PowerManager pm = (PowerManager) context
								.getSystemService(Context.POWER_SERVICE);
						pm.reboot("Because I want you to reboot!");
					//}
					//RootTools.sendShell("reboot -p", 10000);
					
				/*} catch (IOException e) {
					e.printStackTrace();
				} catch (RootToolsException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}*/

			}
			else if(pref.getBoolean("fromBoot", false)){
				Log.d(TAG, "was i able to remove it? "+pref.edit().remove("fromBoot").commit());
			}
		}
		/*
		 * else{ Log.d("BootReceiver","balayb"); }
		 */
	}
}
