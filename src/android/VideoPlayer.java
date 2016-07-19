package com.bais.cordova.video;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class VideoPlayer extends CordovaPlugin{

    private static final String TAG = "BACKGROUND_VIDEO";
    private static final String ACTION_MANY_RECORDING = "many_play";
    private static final String ACTION_CUSTOM_RECORDING = "custom_play";
    private static final int ACTIVITY_CODE_PLAY_MEDIA = 7;
    private int NUMBER = 1;
    

    @SuppressLint("RtlHardcoded") 
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
    	
    	try {
        	
            Log.d(TAG, "ACTION: " + action);
           
            
            if(ACTION_MANY_RECORDING.equals(action)){

            	final JSONArray video_names = args.getJSONArray(0);
            	NUMBER = args.getInt(1); 
            	
              	//Toast.makeText(cordova.getActivity(), video_names.toString()+"<<ACTION_MANY_RECORDING", Toast.LENGTH_SHORT).show();

            	cordova.getActivity().runOnUiThread(new Runnable() {
            		@Override
            		public void run(){
            			play(VideoController.class, NUMBER, video_names);
            		}
            	});
              return true;
            }
            
            if(ACTION_CUSTOM_RECORDING.equals(action)){
            	final JSONArray video_names = args.getJSONArray(0);
            	NUMBER = args.getInt(1); 
            	
            	cordova.getActivity().runOnUiThread(new Runnable() {
            		@Override
            		public void run(){

            			play(VideoPlayerActivity.class, NUMBER, video_names);
            		}
            	});
              return true;	
            }
                       

            callbackContext.error(TAG + ": INVALID ACTION");
            return false;
        } catch(Exception e) {
            Log.e(TAG, "ERROR: " + e.getMessage(), e);
            callbackContext.error(TAG + ": " + e.getMessage());
            return false;
        }
    }

    
    private void play(final Class activityClass, final int nUMBER2, final JSONArray options){
    	final CordovaInterface cordovaObj = cordova;
    	final CordovaPlugin plugin = this;

    	cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				
				//Toast.makeText(cordova.getActivity(), "play play", Toast.LENGTH_SHORT).show();
				final Intent streamIntent = new Intent(cordovaObj.getActivity().getApplicationContext(), activityClass);
				
				ArrayList<String> listdata = new ArrayList<String>();     
				JSONArray jArray = (JSONArray)options; 
				if (jArray != null) { 
				   for (int i=0;i<jArray.length();i++){ 
				    try {
						listdata.add(jArray.get(i).toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				   } 
				}			
				
				Bundle extras = new Bundle();
				extras.putInt("medianumber", nUMBER2);
				
				extras.putStringArrayList("mediaUrl", listdata);
				
				streamIntent.putExtras(extras);
				
				cordovaObj.startActivityForResult(plugin, streamIntent, ACTIVITY_CODE_PLAY_MEDIA);
			}
		});				
	}


	  //Plugin Method Overrides

	    @Override
	    public void onPause(boolean multitasking) {
	    	super.onPause(false);
	    }
	    
	    public void onRestart(){}
	
	    @Override
	    public void onResume(boolean multitasking) {
	        super.onResume(true);
	    }
	
	    @Override
	    public void onDestroy() {
	    	super.onDestroy();
		    	android.os.Process.killProcess(android.os.Process.myPid());
	    }
}
