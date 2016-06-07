package com.bais.cordova.video;

import cn.com.ebais.kyytvali.R;
import java.io.IOException;
import java.util.ArrayList;
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

public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControl, OnInfoListener, OnErrorListener, OnCompletionListener, OnBufferingUpdateListener {
    
     private Bundle extras;
	 private int number;
	 private int totle = 0;
	 private ArrayList<String> mediaurls;
	 private SurfaceView videoSurface;
	 private SurfaceHolder videoHolder;
	 private MediaPlayer player;
	 private VideoControllerView controller;
	 private ImageView loading;
	 //private FrameLayout waitinging;
	 //private ImageView waiting;
	 private boolean yes;
	 private int iscreate = 0;
	 private int gposition = 0;
	 private boolean err = true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
		if(isNetworkConnected(this) == false){
			this.finish();
			Toast.makeText(getBaseContext(), "网路中断，请检查网路连线。", Toast.LENGTH_SHORT).show();		
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

        loading = (ImageView) findViewById(R.id.loading);
        /*waitinging = (FrameLayout) findViewById(R.id.waitinging);
        waiting = (ImageView) findViewById(R.id.imageView_w);
        Animation am_w = AnimationUtils.loadAnimation(this, R.drawable.wait_anima);
        waiting.startAnimation(am_w);
        waitinging.setVisibility(View.GONE);*/
        
        videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);
        controller = new VideoControllerView(this);
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    	player.setOnCompletionListener(this);
    	player.setOnPreparedListener(this);
    	player.setOnBufferingUpdateListener(this);
    	player.setOnErrorListener(this);
    	player.setOnInfoListener(this);
   		playering(totle);
   		iscreate = 1;
   		totle++;
    }    

    public void playering(int mm){
    	try {
    		player.setDataSource(mediaurls.get(mm));
    		player.prepare();
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
       
       if(err == false){
			player.seekTo(gposition);
	    	loading.setVisibility(View.GONE);
    	   return ;
       }   
       
       //**初始/連播**//
       if(iscreate == 1){
			new Handler().postDelayed(new Runnable(){    
			    public void run() {
			        player.start();
			        iscreate = 0;
			        loading.setVisibility(View.GONE);
			    }    
			}, 30000);
       }else if(iscreate == 0){
	        player.start();	 
	        loading.setVisibility(View.GONE);
       }

    }
    
	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		if(player.getCurrentPosition()>0){
	    	gposition = player.getCurrentPosition();
	    	controller.updatePausePlay();
		}
		
		if(player.getCurrentPosition()==0){
			player.reset();
			playering(totle-1);
			player.start();	
			//Toast.makeText(getApplication(), "onInfo onInfo >>", Toast.LENGTH_SHORT).show();
		}
		
		
		
		/*switch (what) {  
        case MediaPlayer.MEDIA_INFO_BUFFERING_START:          	
        	player.pause();
        	//waitinging.setVisibility(View.VISIBLE);  
            break;  
        case MediaPlayer.MEDIA_INFO_BUFFERING_END:       	
        	new Handler().postDelayed(new Runnable(){    
        		public void run() {    
        			player.start();
        			//waitinging.setVisibility(View.GONE);
        		}    
        	 }, 3000);     	
            break;  
        }  */
        return false;  
	}    
    
    @Override
	public void onCompletion(MediaPlayer mp) {	
   		loading.setVisibility(View.VISIBLE);   	
        if(err == false){
     	   return ;
        }         
        player.reset();
		if(number==2)
		{
			//Log.i("................onCompletion", totle+">>>>");	
			playering(totle);
			totle++;
				if(totle == mediaurls.size()){
	   				totle = 0;
	   			}
			
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
        finish();
        //Log.i("..............", "onDestory()............");
        return ;
    }
	
	 @Override
	 protected void onStop() {
	    super.onStop();
	    yes = true;
	    finish();
	   // Log.i("................", "onStop()............");
	    return ; 
	 }

	

	@Override
	public boolean onError(final MediaPlayer mp, final int what, final int extra){
		//LOG.e(".............................", "("+what+","+extra+")");
		//LOG.i(".............................", "("+what+","+extra+")");
		//Toast.makeText(getBaseContext(), "onError "+"("+what+","+extra+")", Toast.LENGTH_SHORT).show(); 
		err = false;
		if(yes!=true){
			player.reset();
			playering(totle-1);
			player.start();	
			
				new Handler().postDelayed(new Runnable(){    
				    public void run() {
				    	if(player.getCurrentPosition()>0){
				    		err = true;
				    		return ;
				    	}				    	
						Toast.makeText(getBaseContext(), "连线中断，请重新点选课程。", Toast.LENGTH_SHORT).show();
						finish();
				    }    
				}, 20000);	
		}				
		return false;		
	}
	
    public boolean dispatchKeyEvent (KeyEvent event) {
    	
    	if( event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE || event.getKeyCode() == 4){
        	finish();
        	return false;
        }
		return controller.dispatchKeyEvent(event);
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
