package xyz.rollingstone.tabs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import xyz.rollingstone.R;
import xyz.rollingstone.liveview.LiveViewUpdaterSocket;

public class ManualTab extends Fragment {

    final public static String DEBUG = "me.hibikiledo.DEBUG";
    final public static String MSG = "me.hibikiledo.MESSAGE";

    final public static int LIVEVIEW_MSG = 0;

    private ImageView imageView;
    private Bitmap imageData;

    private LiveViewUpdaterSocket updater;

    /*
        Handler for UI thread
            This allows LiveViewUpdater to set new imageData and trigger update
     */
    public Handler handler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create handler
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                int msgType = msg.getData().getInt(MSG);
                if( msgType == LIVEVIEW_MSG ) {
                    imageView.setImageBitmap( imageData );
                }
                return false;
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.manual_tab, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Locate our image view
        imageView = (ImageView) getView().findViewById(R.id.liveview);

        // Creating new thread for refreshing ImageView
        updater = new LiveViewUpdaterSocket(this);
        updater.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(DEBUG, "onPause called. Stopping updater thread ..");
        updater.kill();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(DEBUG, "onResume called. Starting updater thread if none exist ..");
        if( ! updater.isAlive() ) {
            updater = new LiveViewUpdaterSocket(this);
            updater.start();
        }
    }

    // Implementation for handling live view updates
    public void setLiveViewData(Bitmap imageData) {
        this.imageData = imageData;
    }

    public Handler getHandler() {
        return this.handler;
    }

}