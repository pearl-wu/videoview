package com.bais.cordova.video;

import java.io.IOException;
import java.util.ArrayList;

import cn.com.ebais.kyytvhismart.R;
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
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, VideoControllerView.MediaPlayerControl, OnPreparedListener, OnInfoListener, OnErrorListener, OnCompletionListener, OnBufferingUpdateListener {
    
     private Bundle extras;
	 private int number;
	 private int totle = 0;
	 private ArrayList<String> mediaurls;
	 private SurfaceView videoSurface;
	 private SurfaceHolder videoHolder;
	 private MediaPlayer player;
	 private VideoPlayerController controller;
	 private ImageView loading;
	 //private FrameLayout waitinging;
	 //private ImageView waiting;
	 private boolean yes;
	 private int gposition = 0;
	 private int ii = 1;
	 private int bb = 1;
	 private boolean err = true;
	 private Runnable r;
	 private boolean onll = false;
	 private Handler handler = new Handler();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		/*if(isNetworkConnected(this) == false){
			this.finish();
			Toast.makeText(getBaseContext(), "网路中断，请检查网路连线。", Toast.LENGTH_SHORT).show();		
			return;
		}*/
    	//onbtnPingClicked();  

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

        loading = (ImageView) findViewById(R.id.loading);
        /*waitinging = (FrameLayout) findViewById(R.id.waitinging);
        waiting = (ImageView) findViewById(R.id.imageView_w);
        Animation am_w = AnimationUtils.loadAnimation(this, R.drawable.wait_anima);
        waiting.startAnimation(am_w);
        waitinging.setVisibility(View.GONE);*/
        
        videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);
        controller = new VideoPlayerController(this);
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    	player.setOnCompletionListener(this);
    	player.setOnPreparedListener(this);
    	player.setOnBufferingUpdateListener(this);
    	player.setOnErrorListener(this);
    	player.setOnInfoListener(this);
    	onPlayers();
    	totle++;
    	onll = true;
    }  
    
    public void onPlayers(){
    	//Toast.makeText(getBaseContext(), "onPlayers onPlayers"+bb, Toast.LENGTH_SHORT).show();	
    	r = new Runnable(){    
		    public void run() {
		    	playering(0);
		    	onPlayers();
		    	bb++;
		    }    
		};
		if(bb==10){
			android.os.Process.killProcess(android.os.Process.myPid());
			Toast.makeText(getBaseContext(), "播放失败,请确认网路连线。", Toast.LENGTH_SHORT).show();	
			return ;
		}
		handler.postDelayed(r, 3000);   	    	
    }
    
    public void onbtnPingClicked(){
    	r = new Runnable(){    
		    public void run() {
		    	isConnected();
		    	onbtnPingClicked();
		    }    
		};
    	new Handler().postDelayed(r, 1000);             
    }
    private boolean isConnected(){
        if (!isNetworkConnected(this)){
        	 ii++;
        }
		return false;
    }    

    public void playering(int mm){
    	try {
    		player.setDataSource(mediaurls.get(mm));
    		player.prepare();
    		start();
        	if(err == false){
    	    	seekTo(gposition);
    	     	err = true;
    	     }
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
    }

	public void timerTask(Runnable m){
		handler.removeCallbacks(m);
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		if(mp.getCurrentPosition()>0){
			gposition = mp.getCurrentPosition();
			loading.setVisibility(View.GONE);
	    	controller.updatePausePlay();
	     	ii=1;
		     	if(onll==true){
		     		timerTask(r);
		     		onll=false;
		     	}	     	
		}
        return false;  
	}    
    
    @Override
	public void onCompletion(MediaPlayer mp) {	
    	loading.setVisibility(View.VISIBLE); 
    	if(err == false){
    		return;
    	}  
    	Toast.makeText(getApplication(), totle+">>onCompletion", Toast.LENGTH_SHORT).show();
		if(number==2){
		    player.reset();
			playering(totle);
			totle++;
				if(totle == mediaurls.size()){
	   				totle = 0;
	   			}
   			return;
		}
		android.os.Process.killProcess(android.os.Process.myPid());	 
	}
    
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		controller.onBufferingUpdate(percent);
		//Toast.makeText(getApplication(), getBufferPercentage()+">>", Toast.LENGTH_SHORT).show();
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();        
        //Log.i("..............", "onDestory()............");
        return ;
    }
	
	 @Override
	 protected void onStop() {
	    super.onStop();
	   // Log.i("................", "onStop()............");
	    return ; 
	 }	

	@Override
	public boolean onError(final MediaPlayer mp, final int what, final int extra){
		ii++;
		err = false;
		if(yes!=true){		
			errstart();
		}				
		return false;		
	}
	
    public void errstart(){   		
    	if(ii>20){
    		Toast.makeText(getBaseContext(), "连线中断，请重新点选课程。", Toast.LENGTH_SHORT).show();
    	}
		if(ii==60){
			android.os.Process.killProcess(android.os.Process.myPid());	
			return ;
		}
			player.reset();
			playering(totle-1);
    } 
    public boolean dispatchKeyEvent (KeyEvent event) {
    	//onUserInteraction();
    	controller.show();	
        if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN){
        	controller.dispatchright();
        	return true;
        }else if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN){
             controller.dispatchleft();
             return true;
        }else if(event.getKeyCode() == 62 && event.getAction() == KeyEvent.ACTION_DOWN){
        	if(player.isPlaying()){
            	player.pause();  
            	return true;
        	}else{
        		player.start();
        		return true;
        	}
        }else if( event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE || event.getKeyCode() == 4){
    		yes = true;
    		android.os.Process.killProcess(android.os.Process.myPid());
    		return false;
        }
       return super.dispatchKeyEvent(event);
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
