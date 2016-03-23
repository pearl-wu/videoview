package com.bais.cordova.video;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.MediaController;

import android.widget.RelativeLayout;
import android.widget.VideoView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import tw.com.bais.video.R;



public class VideoPlayer extends CordovaPlugin implements OnCompletionListener, OnPreparedListener, OnErrorListener, OnDismissListener, OnBufferingUpdateListener, OnTouchListener {

    protected static final String LOG_TAG = "VideoPlayer";
    protected static final String ASSETS = "/android_asset/";

    private CallbackContext callbackContext = null;
    private Dialog dialog;
    private VideoView videoView;
    private MediaPlayer player;
    private int videoXx = 0;
    private int videoYy = 0;
    private int videowidth = WindowManager.LayoutParams.MATCH_PARENT;
    private int videoheight = WindowManager.LayoutParams.MATCH_PARENT;
    private ProgressDialog pro;
    private MediaController mediactrl = null;
    private Boolean isStreaming = true;
    
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("play")) {
            this.callbackContext = callbackContext;

            CordovaResourceApi resourceApi = webView.getResourceApi();
            String target = args.getString(0);
            final JSONObject options = args.getJSONObject(1);

            
            try {
            	videoXx= options.getInt("videoXx");
            	videoYy= options.getInt("videoYy");
            	videowidth= options.getInt("videoWidth");
            	videoheight= options.getInt("videoHeight");
            	isStreaming= options.getBoolean("isStreaming");
            } catch (Exception e) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getLocalizedMessage());
                result.setKeepCallback(false); // release status callback in JS side
                callbackContext.sendPluginResult(result);
                callbackContext = null;
            }
            
            pro = new ProgressDialog(cordova.getActivity());
            pro.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pro.setMessage("Loading....");
            pro.setCancelable(false);
            WindowManager.LayoutParams propar = pro.getWindow().getAttributes();
            propar.x = videoXx;
            propar.y = videoYy;
            pro.getWindow().setAttributes(propar); 
            pro.show();
            

            String fileUriStr;
            try {
                Uri targetUri = resourceApi.remapUri(Uri.parse(target));
                fileUriStr = targetUri.toString();
            } catch (IllegalArgumentException e) {
                fileUriStr = target;
            }

            Log.v(LOG_TAG, fileUriStr);

            final String path = stripFileProtocol(fileUriStr);

            // Create dialog in new thread
            cordova.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    openVideoDialog(path, options);
                }
            });

            // Don't return any result now
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
            callbackContext = null;

            return true;
        }
        else if (action.equals("close")) {
            if (dialog != null) {
                if(player.isPlaying()) {
                    player.stop();
                }
                player.release();
                dialog.dismiss();
            }

            if (callbackContext != null) {
                PluginResult result = new PluginResult(PluginResult.Status.OK);
                result.setKeepCallback(false); // release status callback in JS side
                callbackContext.sendPluginResult(result);
                callbackContext = null;
            }

            return true;
        }
        return false;
    }

    public static String stripFileProtocol(String uriString) {
        if (uriString.startsWith("file://")) {
            return Uri.parse(uriString).getPath();
        }
        return uriString;
    }


	@TargetApi(Build.VERSION_CODES.JELLY_BEAN) 
    protected void openVideoDialog(String path, JSONObject options) {
        // Let's create the main dialog
        dialog = new Dialog(cordova.getActivity(), android.R.style.Theme_NoTitleBar);
        dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.setCancelable(true);
        //dialog.setOnDismissListener(this);

        // Main container layout
        RelativeLayout main = new RelativeLayout(cordova.getActivity());
        main.setBackgroundColor(Color.argb(0, 255, 0, 0));
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        main.setLayoutParams(p);
	       // main.setOrientation(LinearLayout.VERTICAL);
	       // main.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
	       // main.setVerticalGravity(Gravity.CENTER_VERTICAL);
        
        ImageButton closebn = new ImageButton(cordova.getActivity());
        closebn.setImageResource(R.drawable.close_icon_black);
        closebn.setPadding(0, 0, 0, 0);
        closebn.setAdjustViewBounds(true);
       // closebn.setMaxWidth(80);
        //closebn.setMaxHeight(80);
        RelativeLayout.LayoutParams buttonpar = new RelativeLayout.LayoutParams(80, 80);
        buttonpar.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buttonpar.setMargins(0, -6, -6, 0);
        closebn.setLayoutParams(buttonpar);

        
        videoView = new VideoView(cordova.getActivity());
        RelativeLayout.LayoutParams videopar = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        videoView.setLayoutParams(videopar);
	    	// videoView.setVideoURI(uri);
	        // videoView.setVideoPath(path);

        main.addView(videoView);
        main.addView(closebn);

        videoView.requestFocus();
        videoView.setOnTouchListener(this);

        player = new MediaPlayer();        
        Log.d(LOG_TAG+"---holder", isStreaming+"<<<<<<<<<<");
        
    	mediactrl = new MediaController(cordova.getActivity(), !isStreaming);
        mediactrl.setMediaPlayer(videoView);
        //videoView.setMediaController(mediactrl);
        mediactrl.setEnabled(true); 
        mediactrl.show(5000);
    	
       /* if (mediactrl.isShowing()) { 
        	Log.i(LOG_TAG, "mediactrl.hide()");
        	mediactrl.hide(); 
        } else { 
        	Log.i(LOG_TAG, "mediactrl.show()");
        	mediactrl.show(); 
        }   */

        
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnBufferingUpdateListener(this);

        if (path.startsWith(ASSETS)) {
            String f = path.substring(15);
            AssetFileDescriptor fd = null;
            try {
                fd = cordova.getActivity().getAssets().openFd(f);
                player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                
            } catch (Exception e) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getLocalizedMessage());
                result.setKeepCallback(false); // release status callback in JS side
                callbackContext.sendPluginResult(result);
                callbackContext = null;
                return;
            }
        }
        else {
            try {
               player.setDataSource(path);
            } catch (Exception e) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getLocalizedMessage());
                result.setKeepCallback(false); // release status callback in JS side
                callbackContext.sendPluginResult(result);
                callbackContext = null;
                return;
            }
        }

        try {
            float volume = Float.valueOf(options.getString("volume"));
            Log.d(LOG_TAG, "setVolume: " + volume);
            player.setVolume(volume, volume);
        } catch (Exception e) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getLocalizedMessage());
            result.setKeepCallback(false); // release status callback in JS side
            callbackContext.sendPluginResult(result);
            callbackContext = null;
            return;
        }

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            try {
                int scalingMode = options.getInt("scalingMode");
                switch (scalingMode) {
                    case MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING:
                        Log.d(LOG_TAG, "setVideoScalingMode VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING");
                        player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                        break;
                    default:
                        Log.d(LOG_TAG, "setVideoScalingMode VIDEO_SCALING_MODE_SCALE_TO_FIT");
                        player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                }
            } catch (Exception e) {
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getLocalizedMessage());
                result.setKeepCallback(false); // release status callback in JS side
                callbackContext.sendPluginResult(result);
                callbackContext = null;
                return;
            }
        }
                
        final SurfaceHolder mHolder = videoView.getHolder();
        mHolder.setKeepScreenOn(true);
        
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                player.setDisplay(holder);
                try {
                    player.prepare();
                } catch (Exception e) {
                    PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getLocalizedMessage());
                    result.setKeepCallback(false); // release status callback in JS side
                    callbackContext.sendPluginResult(result);
                    callbackContext = null;
                }
            }
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                player.release();
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
        });
        
        
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = videowidth;
        lp.height = videoheight;
        lp.x = videoXx;
        lp.y = videoYy;

        dialog.setContentView(main);
        dialog.show();
        dialog.getWindow().setAttributes(lp);
        
        
        closebn.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			player.stop();
    			dialog.dismiss();
    			pro.dismiss();
    		}
    	});
        
        
    }

	@Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(LOG_TAG, "MediaPlayer.onError(" + what + ", " + extra + ")");
        if(mp.isPlaying()) {
            mp.stop();
        }
        mp.release();
        dialog.dismiss();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
    	mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "MediaPlayer completed");
        mp.release();
        dialog.dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.d(LOG_TAG, "Dialog dismissed");
        if (callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK);
            result.setKeepCallback(false); // release status callback in JS side
            callbackContext.sendPluginResult(result);
            callbackContext = null;
        }
    }

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		if(percent >1 ){
			pro.dismiss();	
		}
	}

	@SuppressLint("NewApi") @Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		 if (mediactrl.isShowing()) { 
			 	Log.i(LOG_TAG+"----onTouch", "mediactrl.show()");
	        	//mediactrl.hide(); 
	        } else { 
	        	Log.i(LOG_TAG+"----onTouch", "mediactrl.hide()");
	        	//mediactrl.show(); 
	        }   
		return false;
	}
}
