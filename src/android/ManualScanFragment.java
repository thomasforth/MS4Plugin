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

import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.imactivate.example.R;
import com.moodstocks.android.ManualScannerSession;
import com.moodstocks.android.MoodstocksError;
import com.moodstocks.android.Result;
import com.moodstocks.android.Scanner;

public class ManualScanFragment extends Fragment implements
		ManualScannerSession.Listener {

	private static final int TYPES = Result.Type.IMAGE | Result.Type.QRCODE
			| Result.Type.EAN13;

	private ManualScannerSession session = null;
	private ProgressDialog progressDialog = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_scan, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		SurfaceView preview = (SurfaceView) getView().findViewById(R.id.preview);

		try {
			session = new ManualScannerSession(getActivity(), Scanner.get(),this, preview);
			session.setResultTypes(TYPES);
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
		super.onResume();
		session.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		session.stop();
	}

	@Override
	public void onStop() {
		session.stop();
		super.onStop();
	}

	public void snap() {
		session.snap();
	}

	/*
	 * This will never occur -- the webview always covers the scanning Fragments */
	/* 
	public void onPreviewTapped(View v) {
		if (v.getId() == R.id.preview) {
			session.snap();
		}
	}
	*/

	@Override
	public void onCameraOpenFailed(Exception e) {
		e.printStackTrace();
	}

	@Override
	public void onWarning(String debugMessage) {
		Log.d("ManualScanFragmentDebug", debugMessage);
	}

	@Override
	public void onError(MoodstocksError error) {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		Toast.makeText(getActivity(), "Error: " + error.getMessage(),
				Toast.LENGTH_SHORT).show();
		session.resume();
	}
	
	@Override
	public void onResult(Result result, Bitmap bitmap) {
		if (result != null) {		
			JSONObject obj = new JSONObject();
			try {
				obj.put("format", result.getType() == Result.Type.IMAGE ? "Image" : "Barcode");
				obj.put("value", result.getValue());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			MS4Plugin.scanSuccess(obj);
		} else {
			MS4Plugin.scanNoResult();
		}
		// by default the ManualScanner pauses after it gets a result. This seems odd so I restart it immediately.
		resumeScan();
	}

	@Override
	public void onServerSearchStart() {

	}
}