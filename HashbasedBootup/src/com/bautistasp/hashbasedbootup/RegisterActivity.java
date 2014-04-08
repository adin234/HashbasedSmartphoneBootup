package com.bautistasp.hashbasedbootup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import com.bautistasp.hashbasedbootup.utilities.Constants;
import com.bautistasp.hashbasedbootup.utilities.ExpandedListView;
import com.bautistasp.hashbasedbootup.utilities.Utility;
import com.bautistasp.hashbasedbootup.R;
import com.stericson.RootTools.RootTools;

import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	@SuppressWarnings("unused")
	private static final String TAG = "RegisterActivity";
	public static RegisterActivity registerActivity;
	Button registerButton;
	ExpandedListView registeredCards;
	TextView numRegd;
	List<String> hashList;
	CheckBox enable;
	Boolean cancelled;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		TelephonyManager tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE); 
		String number = tm.getDeviceId();
		
		Utility.makeToast(number, this);
		registerActivity = this;
		cancelled = false;
		askForRoot();
		if (Utility.isSdPresent()) {
			registerButton = (Button) findViewById(R.id.regButton);
			registeredCards = (ExpandedListView) findViewById(R.id.nameRegd);
			numRegd = (TextView) findViewById(R.id.numRegd);
			enable = (CheckBox) findViewById(R.id.enable);

			enable.setChecked(Utility.appEnabled(this));

			enable.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					cancelled = false;
					if (enable.isChecked()) {
						AlertDialog dialog = Utility
								.passwordCorrect(RegisterActivity.this);
						if (dialog != null) {
							dialog.setOnDismissListener(new OnDismissListener() {

								@Override
								public void onDismiss(DialogInterface dialog) {
									if (cancelled) {
										enable.setChecked(false);
										Utility.makeToast("invalid password",
												RegisterActivity.this);
									} else {
										SharedPreferences pref = getSharedPreferences(
												"hbbuPref", 0);
										SharedPreferences.Editor editor = pref
												.edit();
										editor.putBoolean("enabled", true);
										editor.commit();
									}
								}
							});
							dialog.setOnCancelListener(new OnCancelListener() {

								@Override
								public void onCancel(DialogInterface dialog) {
									cancelled = true;
								}
							});
							dialog.show();
						} else {
							SharedPreferences pref = getSharedPreferences(
									"hbbuPref", 0);
							SharedPreferences.Editor editor = pref.edit();
							enable.setChecked(false);
							editor.putBoolean("enabled", true);
							editor.commit();
						}
					} else {
						AlertDialog dialog = Utility
								.passwordCorrect(RegisterActivity.this);
						if (dialog != null) {
							dialog.setOnDismissListener(new OnDismissListener() {

								@Override
								public void onDismiss(DialogInterface dialog) {
									if (cancelled) {
										enable.setChecked(true);
										Utility.makeToast("invalid password",
												RegisterActivity.this);
									} else {
										SharedPreferences pref = getSharedPreferences(
												"hbbuPref", 0);
										SharedPreferences.Editor editor = pref
												.edit();
										editor.putBoolean("enabled", false);
										editor.commit();
									}
								}
							});
							dialog.setOnCancelListener(new OnCancelListener() {

								@Override
								public void onCancel(DialogInterface dialog) {
									cancelled = true;
								}
							});
							dialog.show();
						} else {
							SharedPreferences pref = getSharedPreferences(
									"hbbuPref", 0);
							SharedPreferences.Editor editor = pref.edit();
							enable.setChecked(true);
							editor.putBoolean("enabled", false);
							editor.commit();
						}
					}
				}
			});

			registerButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
					//	Utility.makeToast(Utility.getSDCID(Utility.searchExternal().getMmc_host()), getApplicationContext());
						register();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

			try {
				hashList = Utility.selectRegisteredCards();
				registeredCards.setAdapter(new ArrayAdapter<String>(this,
						R.layout.hash_list_layout, hashList));
				numRegd.setText("Currently there are " + hashList.size()
						+ " cards registered");

			} catch (IOException e) {
				Utility.makeToast("error reading registered cards", this);
			}
		} else {
			Utility.makeToast("SD Card not present, app will now close", this);
			this.finish();
		}
	}

	@SuppressWarnings("deprecation")
	private void askForRoot() {
		SharedPreferences pref = getSharedPreferences("hbbuPref", 0);
		//Log.d(TAG, "systemAPP" + pref.getBoolean("systemApp", false));
		//TODO make false
		if (!pref.getBoolean("systemApp", true)
				&& !pref.getBoolean("rooted", false)) {
			RootTools.useRoot = true;
			if (RootTools.isAccessGiven())
				pref.edit().putBoolean("rooted", true);
			String fullAppName = Constants.getAppName(this) + "-"
					+ Constants.getVersion(this) + ".apk";
			String localDir = "/data/app/" + fullAppName;
			if ((new File(localDir).exists())) {
				String destDir = Utility.searchExternal() + "/" + fullAppName;
				String systemDir = "/system/app/" + fullAppName;
				try {
					RootTools.sendShell("cat " + localDir + " > " + destDir,
							10000);
					//Log.d(TAG, "localdir:" + localDir + " "+ (new File(localDir).exists()) + " | destdir:"+ destDir + " " + (new File(destDir)).exists());
					RootTools.remount("/system/app/", "rw");
					RootTools.sendShell("cat " + destDir + " > " + systemDir,
							10000);
					//Log.d(TAG,"localdir:" + destDir + " "+ (new File(destDir).exists())+ " | destdir:" + systemDir + " "+ (new File(systemDir)).exists());
					RootTools.remount("/system/app", "ro");
					RootTools.sendShell("rm " + localDir, 10000);
					SharedPreferences.Editor editor = pref.edit();
					editor.putBoolean("systemApp", true);
					editor.commit();
					//Log.d(TAG, "" + pref.getBoolean("systemApp", false));
					PowerManager pm = (PowerManager) this
							.getSystemService(Context.POWER_SERVICE);
					Thread.sleep(5000);
					pm.reboot("Because I want you to reboot!");
				} catch (SecurityException e) {
					Utility.makeToast("please turn your phone on", this);
					try {
						Thread.sleep(5000);
						RootTools.sendShell("reboot -p", 10000);
					} catch (Exception ea) {
					}
				} catch (Exception e) {

					e.printStackTrace();
					Utility.makeToast("unable to copy", this);
				}
			}
		}

	}

	File hashFile;
	String absolutePath;

	private void register() throws IOException {
		absolutePath = Utility.searchExternal().getMountPoint();
		// save hash to sdcard
		File hashFileDir = new File(absolutePath + "/.securehash");
		hashFileDir.mkdir();
		hashFile = new File(absolutePath + "/.securehash/.hash");
		if (hashFile.exists()) {
			
			final String line = Utility.randomString(Utility.getSDCID(Utility.searchExternal().getMmc_host()));

			if (!hashList.contains(line.trim())) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						this);
				alertDialogBuilder.setTitle("Warning!");
				alertDialogBuilder
						.setMessage(
								"It seems that the card is already registered to another device, do you want to register it here too?")
						.setCancelable(false)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										try {
											doRegister(true, null);
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								})
						.setNeutralButton("No",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
			} else {
				Utility.makeToast("Card already registered", this);
			}
		} else {
			doRegister(true, null);
		}
	}

	@SuppressWarnings("rawtypes")
	ArrayAdapter adapter;
	
	@SuppressWarnings("deprecation")
	private void doRegister(Boolean override, String string) throws IOException {
		String randomString;
		if (override) {
			randomString = Utility.randomString(Utility.getSDCID(Utility.searchExternal().getMmc_host()));
			try {
				FileOutputStream fOut = new FileOutputStream(hashFile);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
				myOutWriter.append(randomString);
				myOutWriter.close();
				fOut.close();
				Toast.makeText(getBaseContext(), "done writing hash",
						Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else
			randomString = string;
		// save hash to /system
		if(!Utility.selectRegisteredCards().contains(Utility.randomString(Utility.getSDCID(Utility.searchExternal().getMmc_host())))){
			File hashListDir = new File("/system/");
			RootTools.remount(hashListDir.getAbsolutePath(), "rw");
			try {
				RootTools.sendShell("echo \"" + absolutePath + "," + randomString
						+ "\" >> /system/.hashList", 10000);
				// //Log.d(TAG, "" + Utility.getMounts());
			} catch (Exception e) {
	
			}
			RootTools.remount(hashListDir.getAbsolutePath(), "ro");
		}
		try {
			adapter = new ArrayAdapter<String>(this,
					R.layout.hash_list_layout, hashList);
			hashList = Utility.selectRegisteredCards();
			registeredCards.setAdapter(adapter);
			registeredCards.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					Log.d("TOUCHY", ""+arg2);
					hashList.remove(arg2);
					adapter.notifyDataSetChanged();
				}
			});
			numRegd.setText("Currently there are " + hashList.size()
					+ " cards registered");
		} catch (IOException e) {
			Utility.makeToast("error reading registered cards", this);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

}
