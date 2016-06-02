package com.bais.cordova.video;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.cordova.LOG;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import tw.com.bais.demoview.R;

public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControl, OnInfoListener, OnErrorListener, OnCompletionListener, OnBufferingUpdateListener {
    
     private Bundle extras;
	 private int number;
	 private int totle = 0;
	 private ArrayList<String> mediaurls;
	 private SurfaceView videoSurface;
	 private SurfaceHolder videoHolder;
	 private MediaPlayer player;
	 private VideoControllerView controller;
	 private FrameLayout loading;
	 private FrameLayout waitinging;
	 private ImageView loadstr;
	 private ImageView waiting;
	 private Animation am_w;
	 private boolean yes;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
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
        setContentView(R.layout.activity_video_player);
        videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        videoSurface.setScrollBarSize(100);

        loading = (FrameLayout) findViewById(R.id.loadinging);
        loadstr = (ImageView) findViewById(R.id.imageView_b);        
        Animation am = AnimationUtils.loadAnimation(this, R.drawable.animationset);
        loadstr.startAnimation(am);
        
        waitinging = (FrameLayout) findViewById(R.id.waitinging);
        waiting = (ImageView) findViewById(R.id.imageView_w);
        am_w = AnimationUtils.loadAnimation(this, R.drawable.wait_anima);
        waiting.startAnimation(am_w);
        waitinging.setVisibility(View.GONE);

		//RelativeLayout.LayoutParams loadingLayoutParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		//loadingLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		//loading.setLayoutParams(loadingLayoutParam);
		//this.addContentView(loading, loadingLayoutParam);
        
        videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);
        controller = new VideoControllerView(this);
        player = new MediaPlayer();
    	player.setOnCompletionListener(this);
    	player.setOnPreparedListener(this);
    	player.setOnBufferingUpdateListener(this);
    	player.setOnErrorListener(this);
    	player.setOnInfoListener(this);
   		playering();
    }    

    public void playering(){
    	try {
    		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    		player.setDataSource(mediaurls.get(totle));
    		player.prepareAsync();
    			//Toast.makeText(getApplication(), getResources().getDisplayMetrics().widthPixels, Toast.LENGTH_LONG).show();
        	if(number == 2) totle++;
        		//Toast.makeText(getApplication(), totle+">>"+mediaurls.size(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	controller.show();	        
        return false;
    }

    // Implement SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	player.setDisplay(holder); 
    	player.setScreenOnWhilePlaying(true);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}
    // End SurfaceHolder.Callback

    // Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {
       controller.setMediaPlayer(this);
       controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
       loading.setVisibility(View.GONE);
       player.start();
       //controller.updatePausePlay();
    }
    
	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {

		switch (what) {  
        case MediaPlayer.MEDIA_INFO_BUFFERING_START:          	
        	player.pause();
        	waitinging.setVisibility(View.VISIBLE);  
            break;  
        case MediaPlayer.MEDIA_INFO_BUFFERING_END:       	
        	new Handler().postDelayed(new Runnable(){    
        		public void run() {    
        			player.start();
        			waitinging.setVisibility(View.GONE);
        		}    
        	 }, 3000);     	
            break;  
        }  
        return false;  
	}    
    
   @Override
	public void onCompletion(MediaPlayer mp) {	
   		player.reset();
   		loading.setVisibility(View.VISIBLE);		
		if(number==2)
		{
			playering();
				if(totle == mediaurls.size()){
	   				totle = 0;
	   			}
				//Log.i("................onCompletion", totle+">>>>");				
   			return;
		}
		finish();	 
	}
    
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		controller.onBufferingUpdate(percent);
		//Toast.makeText(getApplication(), getBufferPercentage()+">>", Toast.LENGTH_SHORT).show();
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();        
        yes = true;
        //Log.i("..............", "onDestory()............");
        return ;
    }
	
	 @Override
	 protected void onStop() {
	    super.onStop();
	    yes = true;
	   // Log.i("................", "onStop()............");
	    return ; 
	 }
	

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		
		LOG.e(".............................", "("+what+","+extra+")");
		LOG.i(".............................", "("+what+","+extra+")");
		if(yes!=true){
			Toast.makeText(getBaseContext(), "连线中断，请重新点选课程。", Toast.LENGTH_LONG).show();
		}
		finish();
		return false;
		
	}
	
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
    	int percentage = (player.getCurrentPosition() * 100) / player.getDuration();
        return percentage;
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int i) {
        player.seekTo(i);
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {}

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

}
