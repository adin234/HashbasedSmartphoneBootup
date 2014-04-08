package com.bautistasp.hashbasedbootup.utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.stericson.RootTools.RootTools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import java.security.*;

public class Utility {
	public final static String TAG = "Utility";

	public static boolean isSdPresent() {
		StringBuilder sb = null;
		try {
			FileInputStream fs = new FileInputStream("/proc/mounts");

			DataInputStream in = new DataInputStream(fs);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;
			sb = new StringBuilder();

			while ((strLine = br.readLine()) != null) {
				sb.append(strLine);
			}

			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (sb != null && sb.toString().contains(Utility.searchExternal().getMountPoint())) {
			return true;
		}
		return false;
	}

	public static String randomString(String s) {
		try {
			MessageDigest m=MessageDigest.getInstance("MD5");
			m.update(s.getBytes(),0,s.length());
			return new BigInteger(1,m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("finally")
	public static MMCModel searchExternal() {
		File file = new File("/system/etc/vold.fstab");
		FileReader fr = null;
		BufferedReader br = null;
		String path = null;
		String mmcpoint = null;
		
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			if (fr != null) {
				br = new BufferedReader(fr);
				String s = br.readLine();
				while (s != null) {
					if (s.startsWith("dev_mount")) {
						String[] tokens = s.split("\\s");
						String[] mmcpointokens = tokens[tokens.length-1].split("/"); //get mmcpoint
						path = tokens[2]; // mount_point
						mmcpoint = mmcpointokens[mmcpointokens.length-1];
						Log.d("PATHS",Arrays.toString(tokens)+"\nsd = "+path +" in "+mmcpointokens[mmcpointokens.length-1]);
						if (!Environment.getExternalStorageDirectory()
								.getAbsolutePath().equals(path)) {
							break;
						}
					}
					s = br.readLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fr != null) {
					fr.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				return new MMCModel(path, mmcpoint);
			}
		}
	}

	public static void makeToast(String s, Context c) {
		Toast.makeText(c, s, Toast.LENGTH_LONG).show();
	}

	public static String getMounts() {
		try {
			return "" + RootTools.getMounts();
		} catch (Exception e2) {
		}
		return null;
	}

	public static boolean validHash() {
		List<String> hashList;
		try {
			hashList = selectRegisteredCards();
		} catch (IOException e) {
			return true;
		}
		String registeredHash;
		
		if(!new File(searchExternal().getMountPoint()+"/.securehash/.hash").exists())
			//Log.d("DOESEXIST", ".hash doesnt exist, i must die");
			return false;
		
		registeredHash = randomString(Utility.getSDCID(Utility.searchExternal().getMmc_host()));
		if (hashList.size() == 0 || hashList.contains(registeredHash))
			return true;
		
		return false;

	}

	public static List<String> selectRegisteredCards() throws IOException {
		List<String> hashList = new ArrayList<String>();
		try {
			// open the file for reading
			File stream = new File("/system/.hashList");
			// if file the available for reading
			if (stream.exists()) {
				// prepare the file for reading
				InputStream iStream = new FileInputStream(stream);
				InputStreamReader fIn = new InputStreamReader(iStream);
				BufferedReader reader = new BufferedReader(fIn);

				String line;

				// read every line of the file into the line-variable, on line
				// at the time
				line = reader.readLine();
				while (line != null) {
					hashList.add(line.split(",")[1]);
					line = reader.readLine();
				}

				// Utility.setListViewHeightBasedOnChildren(registeredCards);
				// close the file again
				iStream.close();
			}

		} catch (java.io.FileNotFoundException e) {
			// do something if the myfilename.txt does not exits
		}
		// Log.dTAG,"hashlist == " + ((hashList == null) ? "null" :
		// hashList.size()));
		return hashList;
	}

	public static boolean appEnabled(Context c) {
		SharedPreferences pref = c.getSharedPreferences("hbbuPref", 0);
		try {
			if(selectRegisteredCards().size()<1)
				return false;
		} catch (IOException e) {
			return false;
		}
		return pref.getBoolean("enabled", true);
	}

	public static AlertDialog passwordCorrect(final Context c) {
		// Log.dTAG, ""+c.getSharedPreferences("hbbuPref",
		// 0).getBoolean("hasPassword",false));
		if (!c.getSharedPreferences("hbbuPref", 0).getBoolean("hasPassword",
				false)) {
			askForNewPassword(c);
			return null;
		} else {
			return getInputPassword(c);
		}
	}

	static String value;

	private static AlertDialog getInputPassword(final Context c) {
		final EditText input = new EditText(c);

		AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(c)
				.setTitle("Input Current Password")
				.setMessage("Input password to continue").setView(input)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						value = input.getText().toString();
						// Log.dTAG,
						// value+"|"+c.getSharedPreferences("hbbuPref",
						// 0).getString("passwordValue",
						// "-1")+"  | "+value.equals(c.getSharedPreferences("hbbuPref",
						// 0).getString("passwordValue", "-1")));
						if (value.equals(c.getSharedPreferences("hbbuPref", 0)
								.getString("passwordValue", "-1"))) {
							dialog.dismiss();
						}
						dialog.cancel();
					}
				});

		return dialogbuilder.create();
	}

	private static void askForNewPassword(final Context c) {
		final EditText input = new EditText(c);
		new AlertDialog.Builder(c).setTitle("Input Password")
				.setMessage("Please input a password for future use")
				.setView(input)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString();
						c.getSharedPreferences("hbbuPref", 0).edit()
								.putBoolean("hasPassword", true).commit();
						c.getSharedPreferences("hbbuPref", 0).edit()
								.putString("passwordValue", value).commit();
					}
				}).setOnCancelListener(new OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {
						makeToast("You cant back off now", c);
						askForNewPassword(c);
					}
				}).show();
	}

	public static String getSDCID(String mmcHost) {
		if (Utility.isSdPresent()) {
            try {
                File input = new File("/sys/class/mmc_host/"+mmcHost);
                String cid_directory = null;
                int i = 0;
                File[] sid = input.listFiles();

                for (i = 0; i < sid.length; i++) {
                    if (sid[i].toString().contains(mmcHost+":")) {
                        cid_directory = sid[i].toString();
                        String SID = (String) sid[i].toString().subSequence(
                                cid_directory.length() - 4,
                                cid_directory.length());
                        Log.d(TAG, " SID of MMC = " + SID);
                        break;
                    }
                }
                BufferedReader CID = new BufferedReader(new FileReader(
                        cid_directory + "/cid"));
                String sd_cid = CID.readLine();
                Log.d(TAG, "CID of the MMC = " + sd_cid);
                CID.close();
                return sd_cid;
            } catch (Exception e) {
                Log.e("CID_APP", "Can not read SD-card cid");
                return null;
            }

        } else {
            return null;
        }
	}

}
