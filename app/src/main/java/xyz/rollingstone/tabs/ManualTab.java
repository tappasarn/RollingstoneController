package xyz.rollingstone.tabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import xyz.rollingstone.JoyStick;
import xyz.rollingstone.MainActivity;
import xyz.rollingstone.R;
import xyz.rollingstone.liveview.LiveViewUpdaterSocket;

public class ManualTab extends Fragment {

    final public static String DEBUG = "me.hibikiledo.DEBUG";
    final public static String MSG = "me.hibikiledo.MESSAGE";

    final public static int LIVEVIEW_MSG = 0;

    private ImageView imageView;
    private Bitmap imageData;

    private JoyStick joystick;

    private LiveViewUpdaterSocket updater = null;
    private SharedPreferences sharedPreferences;

    private FrameLayout frameLayout;
    private RelativeLayout joystickLayout;

    private int layoutSize = 500, stickSize = 150;

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

        this.sharedPreferences = getActivity().getSharedPreferences(
                MainActivity.PREFERENCES, Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.manual_tab, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Locate layouts and views
        imageView = (ImageView) getView().findViewById(R.id.liveview);
        frameLayout = (FrameLayout) getView().findViewById(R.id.frame_layout);
        joystickLayout = (RelativeLayout) getView().findViewById(R.id.joystick);

        // Get IP and PORT from sharedPreference use in LiveViewUpdaterSocket
        String IP = sharedPreferences.getString(MainActivity.LIVEVIEW_IP, null);
        int PORT = sharedPreferences.getInt(MainActivity.LIVEVIEW_PORT, 0);

        // Create our joystick
        joystick = new JoyStick(getContext(), joystickLayout, R.drawable.joystick_button);
        joystick.setStickSize(stickSize, stickSize);
        joystick.setLayoutSize(layoutSize, layoutSize);
        joystick.setLayoutAlpha(150);
        joystick.setStickAlpha(100);
        joystick.setOffset(90);
        joystick.setMinimumDistance(50);

        // Listener for frameLayout which is our root view
        // This listener will update location of the joystick to where the touch event occurs
        frameLayout.setOnTouchListener(new View.OnTouchListener() {

            private float xRef, yRef;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    float xPos = event.getX();
                    float yPos = event.getY();

                    xRef = xPos;
                    yRef = yPos;

                    // Remove joystick from root view
                    frameLayout.removeView(joystickLayout);

                    // Create layout parameters for joystickLayout
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.height = layoutSize;
                    params.width = layoutSize;
                    params.gravity = Gravity.TOP;
                    params.topMargin = (int) yPos - (params.height / 2);
                    params.leftMargin = (int) xPos - (params.width / 2);

                    // Add joystick back with created layout parameters
                    frameLayout.addView(joystickLayout, params);

                }

                // Obtain MotionEvent object
                MotionEvent motionEvent = MotionEvent.obtain(
                        SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis() + 100,
                        event.getAction(),
                        (layoutSize / 2) + (event.getX() - xRef),
                        (layoutSize / 2) + (event.getY() - yRef),
                        0
                );
                // Dispatch this event to joystickLayout as well
                // The reason is to make sure that joystickLayout get event
                joystickLayout.dispatchTouchEvent(motionEvent);

                return true;
            }
        });

        // Make sure to draw stick when joystickLayout is touched
        joystickLayout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent event) {
                joystick.drawStick(event);
                return true;
            }
        });

        // Creating new thread for refreshing ImageView
        updater = new LiveViewUpdaterSocket(this, IP, PORT);
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

            // Get IP and PORT from sharedPreferenceuse in LiveViewUpdaterSocket
            String IP = sharedPreferences.getString(MainActivity.LIVEVIEW_IP, null);
            int PORT = sharedPreferences.getInt(MainActivity.LIVEVIEW_PORT, 0);

            updater = new LiveViewUpdaterSocket(this, IP, PORT);
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