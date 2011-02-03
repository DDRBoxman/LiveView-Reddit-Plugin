/*
 * Copyright (c) 2010 Sony Ericsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.recursivepenguin.android.liveview.reddit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sonyericsson.extras.liveview.plugins.AbstractPluginService;
import com.sonyericsson.extras.liveview.plugins.PluginConstants;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class RedditService extends AbstractPluginService {

	Handler mHandler;
	boolean mWorkerRunning = false;
	long mUpdateInterval = 900000;
	String UPDATE_INTERVAL = "updateInterval";
	int mCounter = 0;
	
	String mUsername;
	String mPassword;
	
	HttpClient mClient = new DefaultHttpClient();
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		// Create handler.
		if(mHandler == null) {
			mHandler = new Handler();
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// ... 
		// Do plugin specifics.
		// ...
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// ... 
		// Do plugin specifics.
		// ...
	}
	
	/**
	 * Plugin is just sending notifications.
	 */
	protected boolean isSandboxPlugin() {
	    return false;
	}
	
	/**
	 * Must be implemented. Starts plugin work, if any.
	 */
	protected void startWork() {
		
		// Check if plugin is enabled.
		if(!mWorkerRunning && mSharedPreferences.getBoolean(PluginConstants.PREFERENCES_PLUGIN_ENABLED, false)) {
			mWorkerRunning = true;
			scheduleTimer();
		}
		
	}
	
	/**
	 * Must be implemented. Stops plugin work, if any.
	 */
	protected void stopWork() {
		
	}
	
	/**
	 * Must be implemented.
	 * 
	 * PluginService has done connection and registering to the LiveView Service. 
	 * 
	 * If needed, do additional actions here, e.g. 
	 * starting any worker that is needed.
	 */
	protected void onServiceConnectedExtended(ComponentName className, IBinder service) {
		
	}
	
	/**
	 * Must be implemented.
	 * 
	 * PluginService has done disconnection from LiveView and service has been stopped. 
	 * 
	 * Do any additional actions here.
	 */
	protected void onServiceDisconnectedExtended(ComponentName className) {
		
	}

	/**
	 * Must be implemented.
	 * 
	 * PluginService has checked if plugin has been enabled/disabled.
	 * 
	 * The shared preferences has been changed. Take actions needed. 
	 */	
	protected void onSharedPreferenceChangedExtended(SharedPreferences prefs, String key) {
		if(key.equals(UPDATE_INTERVAL)) {
			long value = Long.parseLong(prefs.getString("updateInterval", "15"));
			mUpdateInterval = value * 1000 * 60;
		} else if (key.equals("username")) {
			mUsername = prefs.getString("username", "");
		} else if (key.equals("password")) {
			mPassword = prefs.getString("password", "");
		}
	}

	/**
	 * This method is called by the LiveView application to start the plugin.
	 * For sandbox plugins, this means when the user has pressed the action button to start the plugin.
	 */
	protected void startPlugin() {
		Log.d(PluginConstants.LOG_TAG, "startPlugin");
		
		// Check if plugin is enabled.
		if(mSharedPreferences.getBoolean(PluginConstants.PREFERENCES_PLUGIN_ENABLED, false)) {
			startWork();
		}
		
	}

	/**
     * This method is called by the LiveView application to stop the plugin.
     * For sandbox plugins, this means when the user has long-pressed the action button to stop the plugin.
     */
	protected void stopPlugin() {
		Log.d(PluginConstants.LOG_TAG, "stopPlugin");
		stopWork();
	}

	/**
     * Sandbox mode only. When a user presses any buttons on the LiveView device, this method will be called.
     */
	protected void button(String buttonType, boolean doublepress, boolean longpress) {
		Log.d(PluginConstants.LOG_TAG, "button - type " + buttonType + ", doublepress " + doublepress + ", longpress " + longpress);
	}

	/**
     * Called by the LiveView application to indicate the capabilites of the LiveView device.
     */
	protected void displayCaps(int displayWidthPx, int displayHeigthPx) {
	    Log.d(PluginConstants.LOG_TAG, "displayCaps - width " + displayWidthPx + ", height " + displayHeigthPx);
	}

	/**
     * Called by the LiveView application when the plugin has been kicked out by the framework.
     */
	protected void onUnregistered() {
		Log.d(PluginConstants.LOG_TAG, "onUnregistered");
		stopWork();
	}

	/**
     * When a user presses the "open in phone" button on the LiveView device, this method is called.
     * You could e.g. open a browser and go to a specific URL, or open the music player.
     */
	protected void openInPhone(String openInPhoneAction) {
		Log.d(PluginConstants.LOG_TAG, "openInPhone: " + openInPhoneAction);
		
		// Open in browser.
		final Uri uri = Uri.parse(openInPhoneAction);
		final Intent browserIntent = new Intent();
		browserIntent.setData(uri);
		browserIntent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
		browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(browserIntent);
	}
	
	/**
     * Sandbox mode only. Called by the LiveView application when the screen mode has changed.
     * 0 = screen is off, 1 = screen is on
     */
    protected void screenMode(int mode) {
        Log.d(PluginConstants.LOG_TAG, "screenMode: screen is now " + ((mode == 0) ? "OFF" : "ON"));
    }
			
    private void sendAnnounce(String header, String body, String messageIntent) {
		try {
			if(mWorkerRunning && (mLiveViewAdapter != null) && mSharedPreferences.getBoolean(PluginConstants.PREFERENCES_PLUGIN_ENABLED, false)) {
			    mLiveViewAdapter.sendAnnounce(mPluginId, mMenuIcon, header, body, System.currentTimeMillis(), messageIntent);
				Log.d(PluginConstants.LOG_TAG, "Announce sent to LiveView");
			} else {
				Log.d(PluginConstants.LOG_TAG, "LiveView not reachable");
			}
		} catch(Exception e) {
			Log.e(PluginConstants.LOG_TAG, "Failed to send announce", e);
		}
	}
	
    /**
     * Schedules a timer. 
     */
    private void scheduleTimer() {
        if(mWorkerRunning) {
            mHandler.postDelayed(mAnnouncer, mUpdateInterval);
        }
    }
	
    /**
     * The runnable used for posting to handler
     */
    private Runnable mAnnouncer = new Runnable() {
        
        @Override
        public void run() {
            try
            {          
            	String modhash = null;
            	if (mUsername.length() > 0 && mPassword.length() > 0) {
            		modhash = loginUser();
            	}
            	if (modhash != null) {
	            	//get messages! http://www.reddit.com/message/unread.json
            		HttpPost req = new HttpPost("http://www.reddit.com/message/unread.json");
            		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            		nameValuePairs.add(new BasicNameValuePair("uh", modhash));
            		req.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            		HttpResponse res = mClient.execute(req);
	            	int status = res.getStatusLine().getStatusCode();
	            	if (status == 200) {
	            		String data = Util.convertStreamToString(res.getEntity().getContent());
	            		JSONObject inbox = new JSONObject(data);
	            		inbox = inbox.getJSONObject("data");
	            		JSONArray messages = inbox.getJSONArray("children");
	            		for (int i=0; i<messages.length(); i++) {
	            			JSONObject message = messages.getJSONObject(i);
	            			message = message.getJSONObject("data");
	            			String id = message.getString("id");
	            			//check if id is already in database
	            			if (true) {
	            				String subject = message.getString("subject");
	            				String content = message.getString("body");
	            				sendAnnounce(subject, content, "http://www.reddit.com/message/messages/" + id);
	            			}
	            		}
	            	} else {
	            		Log.d(PluginConstants.LOG_TAG, ":(" + res.getStatusLine().getReasonPhrase());
	            	}
            	}
            } catch(Exception re) {
                Log.e(PluginConstants.LOG_TAG, "Failed to load reddit messages.", re);
            }
            
            scheduleTimer();
        }
        
    };
    
    public String loginUser() {
		HttpPost loginRequest = new HttpPost("http://www.reddit.com/api/login/" + mUsername);
		try {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("user", mUsername));
	        nameValuePairs.add(new BasicNameValuePair("passwd", mPassword));
	        nameValuePairs.add(new BasicNameValuePair("api_type", "json"));
	        loginRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = mClient.execute(loginRequest);
	        String data = Util.convertStreamToString(response.getEntity().getContent());

	        JSONObject reponseData =new JSONObject(data);
	        reponseData = reponseData.getJSONObject("json");

	        if (reponseData.has("errors")) {
		        JSONArray errors = reponseData.getJSONArray("errors");
		        if (errors.length() > 0) {
		        	return null;
		        }
	        }

	        //reponseData = reponseData.getJSONObject("data");
	        String modhash = reponseData.getString("modhash");
	        //cookie = reponseData.getString("cookie");

	        //prefsChanged = true;

	        return modhash;
	    } catch (ClientProtocolException e) {
	    	e.printStackTrace();
	        return null;
	    } catch (IOException e) {
	    	e.printStackTrace();
	    	return null;
	    } catch (JSONException e) {
	    	e.printStackTrace();
			return null;
		}
	}
}