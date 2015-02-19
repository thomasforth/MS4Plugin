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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import com.imactivate.ms4tom.R;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

// Code as per https://github.com/Adobe-Marketing-Cloud/app-sample-android-phonegap/wiki/Embed-Webview-in-Android-Fragment


public class CordovaFragment extends Fragment implements CordovaInterface {
    CordovaWebView myWebView;

//    public static CordovaFragment newInstance() {
//    	CordovaFragment fragment = new CordovaFragment();
//        return fragment;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LayoutInflater localInflater = inflater.cloneInContext(new CordovaContext(getActivity(), this));
        View rootView = localInflater.inflate(R.layout.fragment_cordova, container, false);        
        myWebView = (CordovaWebView) rootView.findViewById(R.id.myWebView);
        myWebView.setBackgroundColor(Color.argb(1, 0, 0, 0));
        
		// fixes a bug in android 3.0 - 4.0.3 that causes an issue with transparent webviews.
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB
				&& android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			myWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
		}
        
        
        Config.init(getActivity());
        myWebView.loadUrl(Config.getStartUrl());
        return rootView;
    } 
    
    // Plugin to call when activity result is received
    protected CordovaPlugin activityResultCallback = null;
    protected boolean activityResultKeepRunning;

    // Keep app running when pause is received. (default = true)
    // If true, then the JavaScript and native code continue to run in the background
    // when another application (activity) is started.
    protected boolean keepRunning = true;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public Object onMessage(String id, Object data) {
        return null;
    }

    public void onDestroy() {
        super.onDestroy();
        if (myWebView.pluginManager != null) {
            myWebView.pluginManager.onDestroy();
        }
    }

    @Override
    public ExecutorService getThreadPool() {
        return threadPool;
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {
        this.activityResultCallback = plugin;
    }

    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        this.activityResultCallback = command;
        this.activityResultKeepRunning = this.keepRunning;

        // If multitasking turned on, then disable it for activities that return results
        if (command != null) {
            this.keepRunning = false;
        }

        // Start activity
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        CordovaPlugin callback = this.activityResultCallback;
        if (callback != null) {
            callback.onActivityResult(requestCode, resultCode, intent);
        }
    }

	private class CordovaContext extends ContextWrapper implements CordovaInterface
	{
	    CordovaInterface cordova;
	
	    public CordovaContext(Context base, CordovaInterface cordova) {
	        super(base);
	        this.cordova = cordova;
	    }
	    public void startActivityForResult(CordovaPlugin command,
	                                       Intent intent, int requestCode) {
	        cordova.startActivityForResult(command, intent, requestCode);
	    }
	    public void setActivityResultCallback(CordovaPlugin plugin) {
	        cordova.setActivityResultCallback(plugin);
	    }
	    public Activity getActivity() {
	        return cordova.getActivity();
	    }
	    public Object onMessage(String id, Object data) {
	        return cordova.onMessage(id, data);
	    }
	    public ExecutorService getThreadPool() {
	        return cordova.getThreadPool();
	    }
	}

}