package org.ihopkc.videoplayer;

//change this (com.phonegap.helloworld) to your package name, keep the .R
//example: your.package.name.R;
import com.phonegap.helloworld.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;



public class play extends Activity {
  //global vars/objects
  
  VideoView videoView;
  ImageView imageView;
  ImageButton imageButton;
  String defaultBannerLink = "https://www.ihopkc.org/give";
  //url of main video
  String mainVideoUrl = "";
  //url to get json info for an ad
  String adServer = "";
  //an ad's video url
  String AdVideoUrl = "";
  //current position in main video
  int position = 0;
  //if content playing is an ad or not
  boolean isAd = false;
  //time spacing between ads in ms  
  int nextAdTime = 0;
  //time ammout until usre can skip ad
  int adSkipTime = -1;

  MediaController mediaController;
  //bool to show ads or not
  boolean showAds = true;
  //bool to show preroll and then only banners
  boolean isLive = true;
  int playCount = 0;
   public void onCreate(Bundle savedInstanceState) {
    
          super.onCreate(savedInstanceState);
          //get setup and get vars from the javascript call
          setContentView(R.layout.activity_player);
          Bundle bundle = getIntent().getExtras();
          String url = bundle.getString("url");
          showAds = bundle.getBoolean("showAds");
      isLive =  bundle.getBoolean("isLive");
      adServer =  bundle.getString("adServer");   
      //if the adserver is not set dont show ads
      if(adServer.indexOf("http") == -1){
        showAds = false;
      }

      //set up the player and play a video  
      makePlayer(url);
      }
  private void makePlayer(String URL){
    
    //set the current mainvideo url
    mainVideoUrl = URL;
    //set up the video view and contoller
      videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVideoURI(Uri.parse(mainVideoUrl));  
        mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        videoView.requestFocus();
        
       //make a banner close button
       final Button closeBanner = (Button) findViewById(R.id.closeBanner);
       closeBanner.setText("[close]"); 
       closeBanner.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            imageView = (ImageView)findViewById(R.id.imageButton1);
             final View vcloseBanner = findViewById(R.id.closeBanner);
              imageView.setVisibility(View.GONE);
              vcloseBanner.setVisibility(View.GONE);
          }
       });
        //play a prerool is show ads else play the normal video
        if(showAds){
          updateAd();
          
        }
        else{
          ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
          progressBar.setVisibility(View.VISIBLE);
          imageView = (ImageView)findViewById(R.id.imageButton1);
          imageView.setVisibility(View.GONE);
          videoView.start();
        }
    
        //when video ends run function videoCometion()
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
              videoCompletion();
            } 
        });
      
     //when video starts playing  
     videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

         @Override
         public void onPrepared(MediaPlayer mp) {
           //hide the spinner
           ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
           progressBar.setVisibility(View.GONE);
           //if video playing is an ad
           if(isAd){
             //get the skip ad button
             final Button bButton = (Button) findViewById(R.id.skipButton);
             final View vButton = findViewById(R.id.skipButton);  
            
             
           if(adSkipTime > -1 && (int)videoView.getDuration() > adSkipTime){
            //show the close skip ad button
             vButton.setVisibility(View.VISIBLE);
             //remove any skip function
             bButton.setOnClickListener(null);
             //count down till user can skip
             new CountDownTimer(adSkipTime, 1000) {
               //display countdown timer
                   public void onTick(long millisUntilFinished) {
                     String message = "Skip Ad In: ";
                    bButton.setText(message +(int)( millisUntilFinished / 1000 ));
                   }
                   //setup click listner to skip ad
                   public void onFinish() {
                     bButton.setText("Skip Ad");
                     bButton.setOnClickListener(new OnClickListener() {
                      public void onClick(View v) {
                        videoCompletion();
                      }
                     }); 
                   }
                }.start();
           }
           }
         }
     });  
  }


    
    public void updateAd(){ 

       //dose a rest call to get a ad
       //then runs doData on success
       if(playCount == 0 || videoView.isPlaying() && !isAd){
         getdata();  
       }
       else{
         final Handler handler = new Handler();
         handler.postDelayed(new Runnable() {
               @Override
               public void run() {
               updateAd();
               }
             }, 1000);
       }
    }
    //runs when any video is done
    public void videoCompletion(){
      //we dont care if the main video is done
      if(!isAd){
          return;
        }
      
       
    if(showAds){
      //set is ad to false  
      isAd = false;
      //show the progress bar
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
      progressBar.setVisibility(View.VISIBLE);
      //set the video controls
      videoView.setMediaController(mediaController);
      //set the video url
      videoView.setVideoURI(Uri.parse(mainVideoUrl));  
      //seek to where the main video stopped to play an ad
      videoView.seekTo(position);
      //play the video
      videoView.start();
      //show the banner
        imageView = (ImageView)findViewById(R.id.imageButton1);
        imageView.setVisibility(View.VISIBLE);
        
        //show the close banner button
      final View vcloseBanner = findViewById(R.id.closeBanner); 
      vcloseBanner.setVisibility(View.VISIBLE);   
        //Hide the skip button
      final View vButton = findViewById(R.id.skipButton); 
        vButton.setVisibility(View.GONE); 
        //if nextAd Time is set add run a function to play another ad in a given time by nextAdTime 
      if(nextAdTime > 0){
         final Handler handler = new Handler();
         handler.postDelayed(new Runnable() {
             @Override
             public void run() {
             updateAd();
             }
           }, nextAdTime); 
      }
        
       }
   }//end of videoCompletion

    //sets some global vars pass by a jsonSting from the adServer
    public  void doData(String jsonStr){
      
       
       String overlayImage = "";
         //store the current position of the main video
       position = videoView.getCurrentPosition();
       try {
         //set json object
      JSONObject jObject = new JSONObject(jsonStr);
      //get the settings object
      JSONObject settings = jObject.getJSONObject("settings");
      //get next adtime and adSkiptime
      nextAdTime = settings.getInt("nextAdTime");
      adSkipTime = settings.getInt("adSkipTime");
      //get the ad object
      JSONObject ad = jObject.getJSONObject("ad");
      JSONObject overlay = ad.getJSONObject("overlay");
      //set the vars for overlay ,Ad Video, and click url
      overlayImage = overlay.getString("image");
      AdVideoUrl = ad.getString("video");
      defaultBannerLink  = overlay.getString("link");
      
      
      //playing the ad
      //if main video is live play only a preroll
      if( (playCount == 0) || !isLive){
        //set global var isAd to true to tell if current content playing is an ad
        isAd = true;
        //show the spinner
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);
          progressBar.setVisibility(View.VISIBLE);
          //hide the close banner button
          final View vcloseBanner = findViewById(R.id.closeBanner); 
          vcloseBanner.setVisibility(View.GONE);  
          
          //hide the banner
          imageView = (ImageView)findViewById(R.id.imageButton1);
          imageView.setVisibility(View.GONE);
          //remove the video controlls
          videoView.setMediaController(null);
          //set the ad video url
          videoView.setVideoURI(Uri.parse(AdVideoUrl)); 
        //play the ad 
          videoView.start();
        
      }
      playCount++;
      
      
            
    } catch (JSONException e) {
    
    }
       
       
       //set the banner image
       
       // Create an object for subclass of AsyncTask
       GetXMLTask task = new GetXMLTask();
       // Execute the task
       task.execute(new String[] { overlayImage });
       
      //overlay click listner
       imageView = (ImageView)findViewById(R.id.imageButton1);
       
       imageView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
              Uri clickUrl = Uri.parse(defaultBannerLink);
              Intent launchBrowser = new Intent(Intent.ACTION_VIEW, clickUrl);
              startActivity(launchBrowser);
            }
      });

    
       
       
      //for switching banners on live video
    //if video is live add a delay to run updateAd() again 
       if(isLive && playCount > 1 && nextAdTime > 0){
           final Handler handler = new Handler();
           handler.postDelayed(new Runnable() {
               @Override
               public void run() {
               updateAd();
               }
             }, nextAdTime); 
      }
    }

    
    
    
    //A task to get a bitmap and load the info into the banner 
    private class GetXMLTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap map = null;
            for (String url : urls) {
                map = downloadImage(url);
            }
            return map;
        }
 
        // Sets the Bitmap returned by doInBackground
        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
 
        // Creates Bitmap from InputStream and returns it
        private Bitmap downloadImage(String url) {
            Bitmap bitmap = null;
            InputStream stream = null;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;
 
            try {
                stream = getHttpConnection(url);
                bitmap = BitmapFactory.
                        decodeStream(stream, null, bmOptions);
                stream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return bitmap;
        }
 
        
        // Makes HttpURLConnection and returns InputStream
        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
 
            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();
 
                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return stream;
        }
    }
    
    //gets the ad server's jason file
    public void getdata() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.
              ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //fix any caheing by adding a random number to the url
            Random r=new Random();
            int rand=(r.nextInt(99999)+1);  
            String chcheFix;
            
            if(adServer.indexOf("?") > 0){
              chcheFix = "&rand="+rand;
            }
            else{
              chcheFix = "?rand="+rand;
            }
            //set the url
            URL url = new URL(adServer+chcheFix);
            
            HttpURLConnection con = (HttpURLConnection) url
              .openConnection();
            //Read the json file function below 
            readStream(con.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }     
    //simple file io then call doData function to set some global vars
    private void readStream(InputStream in) {
      BufferedReader reader = null;
      String line = "";
      try {
        reader = new BufferedReader(new InputStreamReader(in));
        String json = "";
        
        while ((line = reader.readLine()) != null) {
          
          json += line;
        }
        doData(json);

      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
      
     
      
    }

   
    
}