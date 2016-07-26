package com.bais.cordova.video;


import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

//import cn.com.ebais.kyytvhismart.R;
import cn.com.ebais.kyytvali.R;

public class VideoController extends Activity{
	
	 private Bundle extras;
	 private int number;
	 private int totle = 0;
	 private ArrayList<String> mediaurls;
	 private VideoView videoww;
	 private MediaController mMediaController;
	 private ImageView loading;

	@SuppressLint("InlinedApi") public void onCreate(Bundle icicle) {

		super.onCreate(icicle);
		
		
		if(isNetworkConnected(this) == false){
			this.finish();
			Toast.makeText(getBaseContext(), "网路中断，请检查网路连线。", Toast.LENGTH_LONG).show();		
			return;
		}
		
		
		int currentOrientation = getResources().getConfiguration().orientation;	
		switch(currentOrientation) {
	      case Configuration.ORIENTATION_PORTRAIT:
	    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	       break;
	     }		
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        extras = getIntent().getExtras();
        number = extras.getInt("medianumber");
        mediaurls = extras.getStringArrayList("mediaUrl");        

    	DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

		FrameLayout frelLayout = new FrameLayout(this);
				
		videoww = new VideoView(this);
		RelativeLayout.LayoutParams videowwLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		videowwLayoutParam.addRule(RelativeLayout.CENTER_IN_PARENT);
		videoww.setLayoutParams(videowwLayoutParam);		
		frelLayout.addView(videoww);	
		
		loading = new ImageView(this);
		loading.setImageDrawable(this.getResources().getDrawable((R.drawable.loading)));
		RelativeLayout.LayoutParams loadingLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		loadingLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		loading.setLayoutParams(loadingLayoutParam);
		frelLayout.addView(loading);	
		
	       mMediaController = new MediaController(this);
	       videoww.setMediaController(mMediaController);
	       videoww.setVideoURI(Uri.parse(mediaurls.get(totle)));
	       		if(number == 2) totle++;
	       mMediaController.setMediaPlayer(videoww);
	       videoww.requestFocus();
	       videoww.setDrawingCacheEnabled(true); 
	       RelativeLayout.LayoutParams frelLayoutLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
	       this.addContentView(frelLayout, frelLayoutLayoutParam);	
	       
       
	    videoww.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
       {
	       	@Override
	            public void onCompletion(MediaPlayer mp){
	       		videoww.setVisibility(View.INVISIBLE);
	       		loading.setVisibility(View.VISIBLE);
	       			
						if(number==2)
						{						
						videoww.setVisibility(View.VISIBLE); 
			       		videoww.setVideoURI(Uri.parse(mediaurls.get(totle)));
			       			if(totle+1 == mediaurls.size()){
			       				totle = 0;
			       			}else{
			       				totle++;
			       			}
			       			return;
						}
						finish();	            
	            }
	   }); 	
       
       videoww.setOnPreparedListener(new OnPreparedListener() {
           @Override
           public void onPrepared(MediaPlayer mp) {
         	   loading.setVisibility(View.INVISIBLE);  
         	   videoww.start();
            }
           
       });
       
       videoww.setOnErrorListener(new OnErrorListener(){
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			finish();	
			Toast.makeText(getBaseContext(), "连线中断，请重新点选课程。", Toast.LENGTH_LONG).show();
			return false;
		}    	   
       });

      /* videoww.setOnKeyListener(new OnKeyListener(){
    	   @Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
		        return true;
			}
       });*/
      
	}
	
	 public boolean isNetworkConnected(Context context) {   
		if (context != null) {   
		 ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);   
		 NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();   
			 if (mNetworkInfo != null) {   
				 return mNetworkInfo.isAvailable();   
			 }   
		}   
		 return false;   
	} 
	
	
	public boolean dispatchKeyEvent (KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
		|| event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP 
		|| event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT 
		|| event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN)
		{ 
			//Toast.makeText(getBaseContext(), "dispatchKeyEvent", Toast.LENGTH_SHORT).show();
			return true; 
		} 
		return super.dispatchKeyEvent(event);
	}
}
