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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.imactivate.ms4tom.R;
import com.moodstocks.android.AutoScannerSession;
import com.moodstocks.android.MoodstocksError;
import com.moodstocks.android.Result;
import com.moodstocks.android.Scanner;
import com.moodstocks.android.Scanner.SearchOption;

public class AutoScanFragment extends Fragment implements AutoScannerSession.Listener {
	
    private AutoScannerSession session = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        SurfaceView preview = (SurfaceView)getView().findViewById(R.id.preview);

        try {
            session = new AutoScannerSession(getActivity(), Scanner.get(), this, preview);
            
            // Turn on or off Partial recognition -- SearchOption.SMALLTARGET works too.
            if (MS4Plugin.noPartials == true){
            	session.setSearchOptions(SearchOption.NOPARTIAL);            
            }
            if (MS4Plugin.smallTarget == true) {
            	session.setSearchOptions(SearchOption.SMALLTARGET);
            }
            if (MS4Plugin.useDeviceRotation == true) {
            	//
            }
            session.setResultTypes(MS4Plugin.scanFormats);
            session.start();

        } catch (MoodstocksError e) {
            e.printStackTrace();
        }
    }

    public void resumeScan() {
    	this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
		        session.resume();		
			}
    	});
    }
    
    public void pauseScan() {
    	this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
		        session.pause();		
			}
    	});
    }
     
    @Override
    public void onResume() {
		session.resume();
        super.onResume();

    }

	@Override
	public void onPause() {
		session.pause();
		super.onPause();
	}
	
	// A session.stop() needs adding or the scanFragment does not release the camera object properly.
	@Override
	public void onStop() {
		session.stop();
		super.onStop();
	}

    @Override
    public void onCameraOpenFailed(Exception e) {
        // You should inform the user if this occurs!
  	  	e.printStackTrace();
    }

    @Override
    public void onWarning(String debugMessage) {
        Log.d("AutoScanFragmentDebug", debugMessage);
    }

    @Override
    public void onResult(Result result) {
    	MS4Plugin.scanSuccess(result);
    }

}
