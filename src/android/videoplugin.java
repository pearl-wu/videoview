package org.ihopkc.videoplayer;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.content.Intent;

/**
 * This class echoes a string called from JavaScript.
 */
public class videoplugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("play")) {
            final String url= args.getString(0);  
            final boolean showAds= args.getBoolean(1); 
            final boolean isLive= args.getBoolean(2);
            final String adServer = args.getString(3);
            
            cordova.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Context context = cordova.getActivity().getApplicationContext();
                        Intent intent = new Intent(context,play.class);
                        intent.putExtra("url", url);
                        intent.putExtra("showAds", showAds);
                        intent.putExtra("isLive", isLive);
                        intent.putExtra("adServer", adServer);
                        cordova.getActivity().startActivity(intent);
                    }
                });
                
                return true;
        }
        return false;
    }
 
}