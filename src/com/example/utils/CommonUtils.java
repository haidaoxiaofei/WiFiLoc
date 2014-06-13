package com.example.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Helper class handling the responses from the server
 * */
public class CommonUtils {

	// Shared Preferences
	public static final String SHARED_PREFS = "GEOCROWD_PREFERENCES";

	// Used for activity result from intent
	public static final int DASHBOARD = 1;
	public static final int WIRELESS_CONNECTIVITY = 2;
	public static final int CREATE_ACCOUNT = 3;

	/****************************************************************************************************************/

	/**
	 * Checks for Internet connectivity
	 * 
	 * @param context
	 *            the context that called this method
	 * @return true if wifi are enabled and in connected, otherwise false
	 * 
	 * */
	public static boolean isOnline(final Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnected();
	}

	/****************************************************************************************************************/

	/**
	 * Prompts user to connect to Internet
	 * 
	 * @param context
	 *            the context that called this method
	 * 
	 * */
	public static void AlertDialogInternetConnection(final Context context) {
		// Display a dialog to enable wifi
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("No internet access");
		builder.setMessage("Show wireless settings?");
		builder.setCancelable(true);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Start new intent for wifi settings
				Intent wireless_settings_intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
				((Activity) context).startActivityForResult(wireless_settings_intent, WIRELESS_CONNECTIVITY);
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Cancel the dialog
				dialog.cancel();
			}
		});

		// Here is the actual display code
		AlertDialog alert = builder.create();
		alert.show();
	}

	/****************************************************************************************************************/

	/**
	 * Prompts user to enable location
	 * 
	 * @param context
	 *            the context that called this method
	 * 
	 * */
	public static void AlertDialogLocationServices(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Location services are disabled");
		builder.setMessage("Show location settings?");
		builder.setCancelable(true);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Start new intent for location services settings
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				((Activity) context).startActivity(intent);
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Cancel the dialog
				dialog.cancel();
			}
		});

		// Here is the actual display code
		AlertDialog alert = builder.create();
		alert.show();
	}

	/****************************************************************************************************************/

	/**
	 * Method used to display toast message to user
	 * 
	 * @param context
	 *            the context that called this method
	 * @param textMSG
	 *            the message string
	 * @param duration
	 *            the duration of display
	 * */
	public static void toastPrint(Context context, String textMSG, int duration) {
		Toast.makeText(context, textMSG, duration).show();
	}

	/****************************************************************************************************************/

	/**
	 * Returns the absolute constructed by the parameters
	 * 
	 * @param IP
	 *            the IP of the server
	 * @param PORT
	 *            the PORT of the server
	 * @param WebService
	 *            the webservice to call
	 * 
	 * @return the absolute url
	 * 
	 * */
	public static String getAbsoluteURL(final String IP, final String PORT, final String WebService) {
		String url = IP + ":" + PORT + WebService;
		if (!url.startsWith("http://"))
			url = "http://" + url;
		return url;
	}

	/****************************************************************************************************************/

	/**
	 * Method used to pop up message to user
	 * 
	 * @param context
	 *            the context that called this method
	 * @param msg
	 *            the message string
	 * @param title
	 *            the title string
	 * @param imageID
	 *            the id of an image in resources
	 * */
	public static void popup_msg(Context context, String msg, String title, int imageID) {

		AlertDialog.Builder alert_box = new AlertDialog.Builder(context);

		alert_box.setTitle(title);
		alert_box.setMessage(msg);
		alert_box.setIcon(imageID);

		alert_box.setNeutralButton("Hide", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		AlertDialog alert = alert_box.create();
		alert.show();
	}

	/****************************************************************************************************************/
}
