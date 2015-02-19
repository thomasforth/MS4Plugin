package com.imactivate.ms4tom;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import com.imactivate.ms4tom.R;
import com.moodstocks.android.Result;
import com.moodstocks.android.Scanner;
import com.moodstocks.android.MoodstocksError;
import com.moodstocks.android.Scanner.SearchOption;
import com.moodstocks.android.advanced.Image;
import com.moodstocks.phonegap.plugin.CordovaFragment;
import com.moodstocks.phonegap.plugin.AutoScanFragment;
import com.moodstocks.phonegap.plugin.MS4Plugin;
import com.moodstocks.phonegap.plugin.ManualScanFragment;

public class MainActivity extends Activity implements Scanner.SyncListener{

    // Moodstocks API key/secret pair
    private static final String API_KEY = "ks495yee1mkuuhzipzms";
    private static final String API_SECRET = "zGcHPj3xMdrpDqQN";
    
    private boolean compatible = false;
    private Scanner scanner;
    
    private Fragment cordovaFragment;
    private Fragment scanFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // Load the CordovaFragment into the main view.
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        cordovaFragment = new CordovaFragment();
        fragmentTransaction.add(R.id.cordovaFragmentHolder, cordovaFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();   
    
    }
    

	public void openScanner(String bundleName) {
		// Create the scanner object and start syncing
		compatible = Scanner.isCompatible();
		if (compatible) {
			try {
				scanner = Scanner.get();
				String path = Scanner.pathFromFilesDir(this, "scanner.db");
				scanner.open(path, API_KEY, API_SECRET);
				if (bundleName != null) {
					tryLoadBundle(bundleName);
				}
				else {
					// scanner opened, no bundle loaded.
					MS4Plugin.openFinished(false);
				}
				scanner.setSyncListener(this);
			} catch (MoodstocksError e) {
				Log.d("MainActivity", "Moodstocks Error on scanner open");
				e.printStackTrace();
			}
		}
	}     
	
	public void syncScanner() {
		scanner.sync();
	}
    
    // Load bundle. On first run only
	private void tryLoadBundle(String bundleName) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (!prefs.getBoolean("firstTime", false)) {
			try {
				Log.d("mainActivity", "Loading Bundle. This should happen on first run only");
				scanner.importBundle(this, API_KEY, bundleName);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean("bundleLoaded", true);
				editor.commit();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (MoodstocksError e) {
				e.printStackTrace();
			}
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("firstTime", true);
			editor.commit();
		}

		// test if bundle has ever been loaded
		if (!prefs.getBoolean("bundleLoaded", false)) {
			// scanner opened, bundle loaded.
			MS4Plugin.openFinished(true);
		} else {
			// scanner opened, no bundle loaded.
			MS4Plugin.openFinished(false);
		}
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (compatible) {
            try {
                scanner.close();
                scanner.destroy();
                scanner = null;
            } catch (MoodstocksError e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSyncStart() {
        Log.d("Moodstocks SDK", "Sync will start.");
    }

    @Override
    public void onSyncComplete() {
        try {
            Log.d("Moodstocks SDK", "Sync succeeded (" + scanner.count() + " images)");
            MS4Plugin.syncFinished(scanner.count());
        } catch (MoodstocksError e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSyncFailed(MoodstocksError e) {
        Log.d("Moodstocks SDK", "Sync error #" + e.getErrorCode() + ": " + e.getMessage());
        MS4Plugin.syncFailed();
    }

    @Override
    public void onSyncProgress(int total, int current) {
        int percent = (int) ((float) current / (float) total * 100);
        Log.d("Moodstocks SDK", "Sync progressing: " + percent + "%");
        
        // In most cases, Sync happens too quickly to be passed through to the cordova webview
        //MS4Plugin.syncProgress(percent);
    }

	/* 
	 * methods called from MS4Plugin.java 
	 * */

    private FragmentManager fragmentManager = getFragmentManager();
    
    public void startAutoScan() {	        
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        scanFragment = new AutoScanFragment();
        fragmentTransaction.add(R.id.scanFragmentHolder, scanFragment);
        //fragmentTransaction.addToBackStack(scanFragment.toString());
        fragmentTransaction.commit(); 
	}
    
    public void startManScan() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        scanFragment = new ManualScanFragment();     
        fragmentTransaction.add(R.id.scanFragmentHolder, scanFragment);
        //fragmentTransaction.addToBackStack(scanFragment.toString());
        fragmentTransaction.commit(); 
    }
    
    public void stopScan() {
        //fragmentManager.popBackStack();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(scanFragment);
        fragmentTransaction.commit();
    }
    
    /*
     * Backstack is managed in Javascript -- Phonegap's backbutton bindings are very hard to override from Java 
     */
    /*
    @Override
    public void onBackPressed() {
    	Log.d("backstackcount", "" + fragmentManager.getBackStackEntryCount());
    	if(fragmentManager.getBackStackEntryCount() != 0) {
    		fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
    */

    // Doesn't work yet
	public void pauseScan() {
		if(scanFragment.getClass().toString().contains("ManualScanFragment")){
    		Log.d(this.toString(), "Manual Scans are paused until tapped -- pause request ignored");	
    	}
    	else if (scanFragment.getClass().toString().contains("AutoScanFragment")) {    		
    		((AutoScanFragment) scanFragment).pauseScan();
    	}
    	else {
    		// newScanFragment doesn't exist. Not scanning. Do nothing.
    	}
	}

	public void resumeScan() {    	
		if(scanFragment.getClass().toString().contains("ManualScanFragment")){
			Log.d(this.toString(), "Manual Scans are paused until tapped -- use tapToScan() to resume scanning on a Manual Scanner session");	
    	}
    	else if (scanFragment.getClass().toString().contains("AutoScanFragment")) {
    		((AutoScanFragment) scanFragment).resumeScan();
    	}
    	else {
    		// newScanFragment doesn't exist. Not scanning. Do nothing.
    	}
	}
    
	// Android permissions changes in v4.4+ mean that passing a file URI no longer works. This is a known bug.
	// This method of passing a Base64 String image works everywhere and the extra overhead is acceptable.
	// Scaling and rotation are handled within the phonegap camera plugin and so don't need handling here.
	public void processCustomImageString(String Base64image) {
		byte[] imageAsBytes = Base64.decode(Base64image.getBytes(), Base64.DEFAULT);
		Bitmap bmp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

		// Build the Moodstocks 'Image' object:
		Image img = null;
		try {
			img = new Image(bmp);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (MoodstocksError e) {
			e.printStackTrace();
		}

		// Perform a local search:
		try {
			//SearchOption.NOPARTIAL, or SearchOption.SMALLTARGET can be passed instead of SearchOption.DEFAULT
			Result result = scanner.search(img, SearchOption.DEFAULT, Result.Extra.NONE);
			if (result != null) {
				MS4Plugin.recognisePhotoSuccess(result);
				Log.d("MainActivity", "[Local search] Result found: " + result.getValue());
			} else {
				MS4Plugin.recognisePhotoFail();
				Log.d("MainActivity", "[Local search] No result found");
			}
		} catch (MoodstocksError e) {
			e.printStackTrace();
		}

		// Release the Moodstocks 'Image' object. This is a native object and the garbage-collector won't destroy it.
		img.release();
	}
  
	// For manual scans / tap-to-scan
	public void tapToScan() {
    	if(scanFragment.getClass().toString().contains("ManualScanFragment")){
    		((ManualScanFragment) scanFragment).snap();	
    	}
    	else {
    		Log.d("MainActivity", "TapToScan only works with a manual scanning session");
    		// do nothing for now
    		// but maybe implement tap to refocus in future
    	}		
	}

}