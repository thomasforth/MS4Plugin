/*
 * Copyright (c) 2015 Moodstocks SAS and imactivate
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.moodstocks.phonegap.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.imactivate.example.MainActivity;
import com.moodstocks.android.Result;

import android.util.Log;

public class MS4Plugin extends CordovaPlugin {
	// scanTypes and scanFlags are public so they can be used in ManualScanFragment and AutoScanFramgent classes
	// they are initially set to default values and changed later
	public static int scanFormats = Result.Type.IMAGE | Result.Type.QRCODE | Result.Type.EAN13 | Result.Type.EAN8 | Result.Type.DATAMATRIX;
	public static boolean noPartials = true;
	public static boolean smallTarget = true;
	public static boolean useDeviceRotation = true;
	
	// Cordova Plugin Methods
	public static final String TEST_MESSAGE = "testMessage";	
	public static final String RECOGNISE_PHOTO = "recognisePhoto";
	public static final String RECOGNISE_PHOTO_STRING = "recognisePhotoString";
	public static final String START_SCAN = "startScan";
	public static final String STOP_SCAN = "stopScan";
	public static final String PAUSE_SCAN = "pauseScan";
	public static final String RESUME_SCAN = "resumeScan";
	public static final String TAP_TO_SCAN = "tapToScan";
	public static final String OPEN_SCAN = "openScanner";
	public static final String SYNC_SCAN = "syncScanner";	
	
	/* CallbackContexts are stored in a public variable so they can be used later in this class. */
	/* This is a different pattern from previous versions of the plugin which used startActivityForResult. */
	/* The startActivityForResult method does not work with Fragments. This pattern does. */
	private static CallbackContext scanCallbackContext = null;
	private static CallbackContext syncCallbackContext = null;
	private static CallbackContext recognisePhotoCallbackContext = null;
	private static CallbackContext openCallbackContext = null;
	
	private static CallbackContext messageCallbackContext = null;
	
	public static void scanSuccess(JSONObject resultObject){
		PluginResult scanResult = new PluginResult(PluginResult.Status.OK, resultObject);
		scanResult.setKeepCallback(true);
		scanCallbackContext.sendPluginResult(scanResult);
		//scanResult.setKeepCallback(true);
		//scanCallbackContext.sendPluginResult(scanFrame);
	}
	/*public static void recognisePhotoSuccess(JSONObject obj){
		PluginResult scanResult = new PluginResult(PluginResult.Status.OK, obj.getValue());
		scanResult.setKeepCallback(false);
		recognisePhotoCallbackContext.sendPluginResult(scanResult);
	}*/
	
	public static void recognisePhotoFail(){
		PluginResult scanResult = new PluginResult(PluginResult.Status.ERROR, "No Image Found");
		scanResult.setKeepCallback(false);
		recognisePhotoCallbackContext.sendPluginResult(scanResult);
	}
	
	public static void scanNoResult() {
		PluginResult scanResult = new PluginResult(PluginResult.Status.ERROR, "No Image Found");
		scanResult.setKeepCallback(true);
		scanCallbackContext.sendPluginResult(scanResult);
	}
	
	public static void syncFinished(int count) {
		PluginResult syncResult = new PluginResult(PluginResult.Status.OK, count);
		syncResult.setKeepCallback(false); // final sync callback -- doesn't need keeping
		syncCallbackContext.sendPluginResult(syncResult);		
	}
	
	public static void syncFailed() {
		syncCallbackContext.error(0);		
	}
	
	public static void openFinished(Boolean bundleFlag) {
		PluginResult openResult = new PluginResult(PluginResult.Status.OK, bundleFlag);
		openResult.setKeepCallback(false); // final open callback -- doesn't need keeping
		openCallbackContext.sendPluginResult(openResult);		
	}
	
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException{	
		
		try {
			if (action.equals(TEST_MESSAGE)) {
				messageCallbackContext = callbackContext;
				// A test to check that the plugin is passing messages properly
				Log.d("MS4Plugin", args.getJSONObject(0).opt("message").toString());	
			}
			if (action.equals(RECOGNISE_PHOTO_STRING)){
				recognisePhotoCallbackContext = callbackContext;
				// The phonegap camera plugin passes me a correctly rotated and scaled image as a Base64 string
				((MainActivity) this.cordova.getActivity()).processCustomImageString(args.getJSONObject(0).optString("photoString").toString());
		    }	
			if (action.equals(START_SCAN)){
				scanCallbackContext = callbackContext;
				scanFormats = args.getJSONObject(0).optInt("scanFormats");
				noPartials = args.getJSONObject(0).optBoolean("noPartials");
				smallTarget = args.getJSONObject(0).optBoolean("smallTarget");
				useDeviceRotation = args.getJSONObject(0).optBoolean("useDeviceRotation");
				
				String scanType = args.getJSONObject(0).optString("scanType");
				Log.d("MoodstocksPlugin4", "Starting a " + scanType + " scan.");
				if (scanType.equals("manual")) {					
					((MainActivity) this.cordova.getActivity()).startManScan();
				}
				else if (scanType.equals("auto")) {
					((MainActivity) this.cordova.getActivity()).startAutoScan();
				}
				else {		
					callbackContext.error(0);
					return false;
				}
			}
			if (action.equals(OPEN_SCAN)){				
				openCallbackContext = callbackContext;
				if(args.getJSONObject(0).length()<1) {
					Log.d("openScan", "no Bundle");
					((MainActivity) this.cordova.getActivity()).openScanner(null);
				}
				else {
					((MainActivity) this.cordova.getActivity()).openScanner(args.getJSONObject(0).opt("bundleName").toString());
				}
			}		
			if (action.equals(STOP_SCAN)){
				((MainActivity) this.cordova.getActivity()).stopScan();
				callbackContext.success();
			}
			if (action.equals(PAUSE_SCAN)){
				((MainActivity) this.cordova.getActivity()).pauseScan();
			}
			if (action.equals(RESUME_SCAN)){
				((MainActivity) this.cordova.getActivity()).resumeScan();
			}
			if (action.equals(TAP_TO_SCAN)){
				((MainActivity) this.cordova.getActivity()).tapToScan();				
			}
			if (action.equals(SYNC_SCAN)) {
				syncCallbackContext = callbackContext;
				((MainActivity) this.cordova.getActivity()).syncScanner();
			}
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
			callbackContext.error(0);
			return false;
		}

	}


	
}