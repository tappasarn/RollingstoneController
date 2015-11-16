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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import xyz.rollingstone.packet.CommandPacketBuilder;
import xyz.rollingstone.JoyStick;
import xyz.rollingstone.MainActivity;
import xyz.rollingstone.R;
import xyz.rollingstone.tele.TelepathyToRobot;
import xyz.rollingstone.liveview.LiveViewUpdaterSocket;
import xyz.rollingstone.tele.TelepathyToRobotThread;

public class ManualTab extends Fragment {

    // Define TAG for logcat filtering
    final public static String DEBUG = "me.hibikiledo.DEBUG";
    // Define TAG for sending message betweeb UI Thread and LiveViewUpdater Thread
    final public static String MSG = "me.hibikiledo.MESSAGE";
    // Define type of MSG. In this case, LIVEVIEW message which contains Bitmap
    final public static int LIVEVIEW_MSG = 0;

    // For showing live view
    private ImageView imageView;
    // Hold bitmap data for displaying in imageView
    private Bitmap imageData;

    // Reference to virtual joystick
    private JoyStick joystick;

    // Thread for performing image fetching from the robot
    private LiveViewUpdaterSocket updater = null;

    // Use to save application configurations
    private SharedPreferences sharedPreferences;

    // Root layout for ManualTab
    private FrameLayout frameLayout;
    // Layout for holding virtual joystick
    private RelativeLayout joystickLayout;

    // Define size of stick and layout
    private int layoutSize = 500, stickSize = 150;
    // True if joystick is displaying, otherwise false
    private boolean isJoystickShown = false;

    // To send a command to robot
    private TelepathyToRobotThread telepathyToRobotThread;
    private TelepathyToRobot telepathyToRobot;
    // To count the commandId
    private Integer commandId = 0;
    // Use to get value from the app and and build it into proper way
    private CommandPacketBuilder commandPacketBuilder;

    // to determine whether the id is occupied or not
    private int[] availableId = {0, 0, 0, 0};

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
                // Get message type
                int msgType = msg.getData().getInt(MSG);
                // If message type is LIVEVIEW_MSG, this implies that, new bitmap data has been set.
                // We are free to update Bitmap data in imageView.
                if (msgType == LIVEVIEW_MSG) {
                    imageView.setImageBitmap(imageData);
                }
                return false;
            }
        });

        // Get sharedPreferences object for accessing configurations
        this.sharedPreferences = getActivity().getSharedPreferences(
                MainActivity.PREFERENCES, Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.manual_tab, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Locate layouts and views
        imageView = (ImageView) getView().findViewById(R.id.liveview);
        frameLayout = (FrameLayout) getView().findViewById(R.id.frame_layout);
        if (joystickLayout == null) {
            joystickLayout = (RelativeLayout) getView().findViewById(R.id.joystick);
        }

        // Get IP and PORT from sharedPreference use in LiveViewUpdaterSocket
        final String SERVER_IP = sharedPreferences.getString(MainActivity.SERVER_IP, null);
        final int LIVEVIEW_PORT = sharedPreferences.getInt(MainActivity.LIVEVIEW_PORT, 0);

        //Get Port from sharedPreference use for control
        final int CONTROL_PORT = sharedPreferences.getInt(MainActivity.CONTROL_PORT, 0);

        // Create our joystick
        joystick = new JoyStick(getActivity(), joystickLayout, R.drawable.joystick_button);
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
                Log.d(DEBUG, "Oh! I'm touched!!");

                // Get IP and PORT from sharedPreference use in LiveViewUpdaterSocket
                String IP = sharedPreferences.getString(MainActivity.ROBOT_IP, null);
                int LIVEVIEW_PORT = sharedPreferences.getInt(MainActivity.LIVEVIEW_PORT, 0);

                //Get Port from sharedPreference use for control
                int CONTROL_PORT = sharedPreferences.getInt(MainActivity.CONTROL_PORT, 0);


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

                    // This handles another TouchEvent while virtual joystick is being shown.
                    // Without this dispatchTouchEvent will raise RuntimeException.
                    if (!isJoystickShown) {
                        // Obtain MotionEvent object
                        MotionEvent motionEvent = MotionEvent.obtain(
                                SystemClock.uptimeMillis(),
                                SystemClock.uptimeMillis() + 100,
                                event.getAction(),
                                (layoutSize / 2) + (event.getX() - xRef),
                                (layoutSize / 2) + (event.getY() - yRef),
                                0
                        );
                        joystickLayout.dispatchTouchEvent(motionEvent);
                        isJoystickShown = true;
                    }

                }

                // When user lift up finger from screen, remove joystickLayout
                if (event.getAction() == MotionEvent.ACTION_UP) {

                    isJoystickShown = false;

                    // Remove joystick from root view
                    joystickLayout.dispatchTouchEvent(event);
                    frameLayout.removeView(joystickLayout);

                    // need to check whether the id is usable
                    if (availableId[commandId] == 0) {
                        commandPacketBuilder = new CommandPacketBuilder();
                        commandPacketBuilder.setType(0); // set type = REQ_M_TYPE

                        commandPacketBuilder.setId(commandId);
                        availableId[commandId] = 1;
                        commandId = (commandId + 1) % 4;

                        commandPacketBuilder.setCommand(0);
                        commandPacketBuilder.setValue(0);

                        //execute the correct value
                        int[] command = commandPacketBuilder.Create();
                        // send direction and distance to the robot and Log it for debugging

                        /*
                        telepathyToRobotThread = new TelepathyToRobotThread(getActivity(), IP, CONTROL_PORT, availableId, command);
                        telepathyToRobotThread.start();
                        */
                        telepathyToRobot = new TelepathyToRobot(getActivity(), IP, CONTROL_PORT, availableId);
                        telepathyToRobot.execute(command[0],command[1]);
                    }
                }


                // Only ACTION_DOWN and ACTION_MOVE needed to be dispatch
                // to joystickLayout as well
                if (event.getAction() == MotionEvent.ACTION_MOVE) {

                    Log.d(DEBUG, "ACTION_MOVE");
                    // Obtain MotionEvent object
                    MotionEvent motionEvent = MotionEvent.obtain(
                            SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis() + 100,
                            event.getAction(),
                            (layoutSize / 2) + (event.getX() - xRef),
                            (layoutSize / 2) + (event.getY() - yRef),
                            0
                    );

                    joystickLayout.dispatchTouchEvent(motionEvent);

                    int direction = joystick.get8Direction();
                    int distance = Math.min(map((int) joystick.getDistance(), 0, 215, 0, 100), 100);

                    Log.d(DEBUG, "commandId " + commandId);
                    Log.d(DEBUG, "AV[CID] " + availableId[0]);
                    Log.d(DEBUG, "AV[CID] " + availableId[1]);
                    Log.d(DEBUG, "AV[CID] " + availableId[2]);
                    Log.d(DEBUG, "AV[CID] " + availableId[3]);
                    // need to check whether the id is usable
                    if (availableId[commandId] == 0) {

                        commandPacketBuilder = new CommandPacketBuilder();
                        commandPacketBuilder.setType(0); // set type = REQ

                        commandPacketBuilder.setId(commandId);
                        availableId[commandId] = 1;
                        commandId = (commandId + 1) % 4;

                        commandPacketBuilder.setCommand(direction);
                        commandPacketBuilder.setValue(distance);

                        //execute the correct value
                        int[] command = commandPacketBuilder.Create();

                        // send direction and distance to the robot and Log it for debugging
                        Log.d(DEBUG, "Im at before Tele2Robot");

                        /*
                        telepathyToRobotThread = new TelepathyToRobotThread(getActivity(), IP, CONTROL_PORT, availableId, command);
                        telepathyToRobotThread.start();
                        */
                        telepathyToRobot = new TelepathyToRobot(getActivity(), IP, CONTROL_PORT, availableId);
                        telepathyToRobot.execute(command[0], command[1]);
                    }

                }

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
        updater = new LiveViewUpdaterSocket(this, SERVER_IP, LIVEVIEW_PORT);
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
        if (!updater.isAlive()) {

            // Get IP and PORT from sharedPreferences in LiveViewUpdaterSocket
            String SERVER_IP = sharedPreferences.getString(MainActivity.SERVER_IP, null);
            int PORT = sharedPreferences.getInt(MainActivity.LIVEVIEW_PORT, 0);

            updater = new LiveViewUpdaterSocket(this, SERVER_IP, PORT);
            updater.start();
        }
    }

    // Set image data passed by updater thread as local data
    public void setLiveViewData(Bitmap imageData) {
        this.imageData = imageData;
    }

    // Allow updater thread to access the handler
    public Handler getHandler() {
        return this.handler;
    }

    // Function to map value from one range to another range
    private int map(int x, int in_min, int in_max, int out_min, int out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

}