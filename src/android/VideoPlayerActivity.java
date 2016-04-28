package com.bais.cordova.video;

import java.io.IOException;
import java.util.ArrayList;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;


public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControl, OnCompletionListener, OnVideoSizeChangedListener, OnBufferingUpdateListener {
    
     private Bundle extras;
	 private int number;
	 private int totle = 0;
	 private ArrayList<String> mediaurls;
	 private int vWidth,vHeight; 
	 private SurfaceView videoSurface;
	 private SurfaceHolder videoHolder;
	 private MediaPlayer player;
	 private VideoControllerView controller;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        extras = getIntent().getExtras();
        number = extras.getInt("medianumber");
        mediaurls = extras.getStringArrayList("mediaUrl");  
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        
        videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        videoSurface.setScrollBarSize(100);
        
        videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);
        controller = new VideoControllerView(this);
               
        player = new MediaPlayer();
    	player.setOnCompletionListener(this);
    	player.setOnPreparedListener(this);
    	player.setOnVideoSizeChangedListener(this);
    	player.setOnBufferingUpdateListener(this);    	
    	playering();
    }
    
    
    public void playering(){

    	try {
    		player.reset();
    		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    		player.setDataSource(mediaurls.get(totle));
    		player.prepare();
    		//Toast.makeText(getApplication(), getResources().getDisplayMetrics().widthPixels, Toast.LENGTH_LONG).show();
        	if(number == 2) totle++; 
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
    	//player.prepareAsync();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	player.stop();
    	//player.release();
    	//finish();
    	//android.os.Process.killProcess(android.os.Process.myPid());
    	
    }
    // End SurfaceHolder.Callback

    // Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {

       controller.setMediaPlayer(this);
       controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
       player.start();
    }
    // End MediaPlayer.OnPreparedListener

    // Implement VideoMediaController.MediaPlayerControl
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
    public void toggleFullScreen() {
        
    }
    // End VideoMediaController.MediaPlayerControl

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
		//Toast.makeText(getApplication(), "onCompletion", Toast.LENGTH_LONG).show();
		if(number==2)
		{
			playering();
   			return;
		}
		finish();	 
	}


	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		// TODO Auto-generated method stub
		//Toast.makeText(getApplication(), player.getVideoWidth(), Toast.LENGTH_LONG).show();    
	}


	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		//controller.onBufferingUpdate(percent);
		
	}

}
