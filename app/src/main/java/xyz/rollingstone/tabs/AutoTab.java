package xyz.rollingstone.tabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import xyz.rollingstone.Action;
import xyz.rollingstone.ActionSQLHelper;
import xyz.rollingstone.HeartBeatThread;
import xyz.rollingstone.ResumeIndicator;
import xyz.rollingstone.liveview.LiveViewCallback;
import xyz.rollingstone.liveview.LiveViewUpdaterSocket;
import xyz.rollingstone.packet.CommandPacketBuilder;
import xyz.rollingstone.MainActivity;
import xyz.rollingstone.R;

public class AutoTab extends Fragment implements LiveViewCallback {

    private static List<String> displayList;
    private static List<String> anotherDisplayList;
    private static List<int[]> packetList;
    private static List<String> selectedScripts;

    private SharedPreferences sharedPreferences;

    private Button startButton;
    private Button resumeButton;
    private Button stopButton;

    private TextView pastPastTextView;
    private TextView pastTextView;
    private TextView currentTextView;
    private TextView nextTextView;
    private TextView nextNextTextView;

    private Handler handler;

    public ResumeIndicator resumeIndicator;

    final Integer REQ_A_TYPE = 2;

    String robotIP;
    int controlPORT;
    int heartbeatPORT;
    HeartBeatThread HB;
    Integer currentIndex;

    private boolean executeOnResume = false;

    // ----------------------------- LIVE VIEW STACK ----------------------------

    // Define TAG for logcat filtering
    final public static String DEBUG = "me.hibikiledo.DEBUG";
    // Define TAG for sending message betweeb UI Thread and LiveViewUpdater Thread
    final public static String MSG = "me.hibikiledo.MESSAGE";
    // Define type of MSG. In this case, LIVEVIEW message which contains Bitmap
    final public static int LIVEVIEW_MSG = 0;

    // Thread for performing image fetching from the robot
    private LiveViewUpdaterSocket updater = null;
    // For showing live view
    private ImageView imageView;
    // Hold bitmap data for displaying in imageView
    private Bitmap imageData;

    /*
        Handler for UI thread
            This allows LiveViewUpdater to set new imageData and trigger update
     */
    public Handler liveViewHandler;

    // --------------------------- END LIVE VIEW STACK ---------------------------

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create liveViewHandler
        liveViewHandler = new Handler(new Handler.Callback() {
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


    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout as view
        View view = inflater.inflate(R.layout.auto_tab, container, false);

        // Locate UI elements
        imageView = (ImageView) view.findViewById(R.id.imageView);
        sharedPreferences = getActivity().getSharedPreferences(MainActivity.PREFERENCES, Context.MODE_PRIVATE);
        startButton = (Button) view.findViewById(R.id.startButton);
        resumeButton = (Button) view.findViewById(R.id.resumeButton);
        stopButton = (Button) view.findViewById(R.id.stopButton);

        // Locate UI components related to script commands and customize them
        pastPastTextView = (TextView) view.findViewById(R.id.pastpastAction);
        pastPastTextView.setTextColor(Color.argb(38, 0, 0, 0));

        pastTextView = ((TextView) view.findViewById(R.id.pastAction));
        pastTextView.setTextColor(Color.argb(38, 0, 0, 0));

        currentTextView = (TextView) view.findViewById(R.id.currentAction);
        currentTextView.setTextColor(Color.argb(87, 0, 0, 0));

        nextTextView = (TextView) view.findViewById(R.id.nextAction);
        nextTextView.setTextColor(Color.argb(54, 0, 0, 0));

        nextNextTextView = (TextView) view.findViewById(R.id.nextNextAction);
        nextNextTextView.setTextColor(Color.argb(54, 0, 0, 0));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity.startButtonState = true;
        MainActivity.stopButtonState = false;
        MainActivity.resumeButtonState = false;

        startButton.setEnabled(MainActivity.startButtonState);
        stopButton.setEnabled(MainActivity.stopButtonState);
        resumeButton.setEnabled(MainActivity.resumeButtonState);

        robotIP = sharedPreferences.getString(MainActivity.ROBOT_IP, null);
        controlPORT = sharedPreferences.getInt(MainActivity.CONTROL_PORT, -1);
        heartbeatPORT = sharedPreferences.getInt(MainActivity.HEARTBEAT_PORT, -1);

        currentIndex = 0;

        if (MainActivity.selectedScripts != null) {

            /* if there is at least 1 selected script, get the table which has the same name as them */
            selectedScripts = MainActivity.selectedScripts;
            ActionSQLHelper db = new ActionSQLHelper(getActivity());
            displayList = new ArrayList<String>();
            anotherDisplayList = new ArrayList<String>();

            packetList = new ArrayList<int[]>();
            CommandPacketBuilder commandPacketBuilder;

            /* loop through every script, we are using 2 Lists here, 1 for keeping display data to be displayed on UI,
             *  another one is to keep the commandPacketList to be sent to the robot
             */
            for (String script : selectedScripts) {
                List<Action> actionList = db.getAllActionsFromTable(script);

                for (Action act : actionList) {
                    displayList.add(act.humanize());
                    commandPacketBuilder = new CommandPacketBuilder(act);
                    commandPacketBuilder.setType(REQ_A_TYPE);
                    commandPacketBuilder.setId(0); // there is no such thing as an ID in automated system

                    int[] dummy = new int[2];
                    dummy = commandPacketBuilder.Create();
                    Log.d(DEBUG, "Dummy1" + String.format("%8s", Integer.toBinaryString(dummy[0])).replace(' ', '0'));
                    Log.d(DEBUG, "Dummy2" + String.format("%8s", Integer.toBinaryString(dummy[1])).replace(' ', '0'));

                    packetList.add(commandPacketBuilder.Create());
                }
            }

            // Initialize view and list values when first load the autoTab

            pastPastTextView.setText("");
            pastTextView.setText("");
            currentTextView.setText("");
            nextTextView.setText("");
            nextNextTextView.setText("");

            startButton.setEnabled(true);
            currentIndex = 0;

            if (displayList.size() > 2) {
                currentTextView.setText(displayList.get(currentIndex));
                nextTextView.setText(displayList.get(currentIndex + 1));
                nextNextTextView.setText(displayList.get(currentIndex + 2));
            }
            else if (displayList.size() == 2) {
                currentTextView.setText(displayList.get(currentIndex));
                nextTextView.setText(displayList.get(currentIndex + 1));
            }
            else if (displayList.size() == 1) {
                currentTextView.setText(displayList.get(currentIndex));
            }
            else {
                currentTextView.setText("Script has no action/ no script is selected");
                startButton.setEnabled(false);
            }

            /**
             * To handle the message passed from HeartBeat to here
             */
            handler = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    String messages = (String) msg.getData().getSerializable("status");
                    if (messages.equals("OK")) {
                        actionSlider();
                    } else if (messages.equals("CNNERR")) {
                        Toast.makeText(getActivity(), "The operation is aborted, can't connect to server", Toast.LENGTH_SHORT).show();
                        startButton.setEnabled(true);
                        stopButton.setEnabled(false);
                        resumeButton.setEnabled(false);
                    } else if (messages.equals("DONE")) {
                        currentTextView.setText("Script Execution is done");
                        currentTextView.setTextColor(getResources().getColor(R.color.editButton));
                        stopButton.setEnabled(false);
                        Toast.makeText(getActivity(), "Script Execution is done", Toast.LENGTH_SHORT).show();
                    } else if (messages.equals("ERR")) {
                        resumeButton.setEnabled(true);
                        Toast.makeText(getActivity(), "Obstacle found", Toast.LENGTH_SHORT).show();
                    } else if (messages.equals("NOHB")) {
                        startButton.setEnabled(true);
                        Toast.makeText(getActivity(), "no HeartBeat", Toast.LENGTH_SHORT).show();
                    } else if (messages.equals("CANCL")) {

                        pastPastTextView.setText("");
                        pastTextView.setText("");
                        currentTextView.setText("");
                        nextTextView.setText("");
                        nextNextTextView.setText("");
                        startButton.setEnabled(true);
                        currentIndex = 0;

                        if (displayList.size() > 2) {
                            currentTextView.setText(displayList.get(currentIndex));
                            nextTextView.setText(displayList.get(currentIndex + 1));
                            nextNextTextView.setText(displayList.get(currentIndex + 2));
                        } else if (displayList.size() == 2) {
                            currentTextView.setText(displayList.get(currentIndex));
                            nextTextView.setText(displayList.get(currentIndex + 1));
                        } else if (displayList.size() == 1) {
                            currentTextView.setText(displayList.get(currentIndex));
                        } else {
                            currentTextView.setText("Script has no action/ no script is selected");
                            startButton.setEnabled(false);
                        }

                        Toast.makeText(getActivity(), "Stop Executing Successfully", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resumeIndicator = new ResumeIndicator();
                    HB = new HeartBeatThread(robotIP, controlPORT, heartbeatPORT, packetList, handler, resumeIndicator);
                    //HB.cancel(false);
                    HB.start();
                    Log.d(DEBUG, "HeartBeat is executing");
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    Toast.makeText(getActivity(), "Start Executing", Toast.LENGTH_SHORT).show();
                }
            });

            resumeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resumeIndicator.setInt(5);
                    resumeButton.setEnabled(false);
                    Toast.makeText(getActivity(), "Resume Executing", Toast.LENGTH_SHORT).show();
                }
            });

            stopButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HB.cancel(true);

                    startButton.setEnabled(true);
                    resumeButton.setEnabled(false);
                    stopButton.setEnabled(false);
                    Toast.makeText(getActivity(), "Send Stop Command", Toast.LENGTH_SHORT).show();
                    currentIndex = 0;
                }
            });

        } else {
            Log.d(DEBUG, "No Automated Script set yet");
        }

        // ----------------------------- LIVE VIEW STACK ----------------------------

        // Get IP and PORT from sharedPreference use in LiveViewUpdaterSocket
        final String SERVER_IP = sharedPreferences.getString(MainActivity.SERVER_IP, null);
        final int LIVEVIEW_PORT = sharedPreferences.getInt(MainActivity.LIVEVIEW_PORT, 0);

        // Creating new thread for refreshing ImageView
        updater = new LiveViewUpdaterSocket(this, SERVER_IP, LIVEVIEW_PORT);
        updater.start();

    }

    @Override
    public void onResume() {
        super.onResume();

        if(executeOnResume) {

            Log.d(DEBUG, "AUTOTAB ONRESUME");

            robotIP = this.sharedPreferences.getString(MainActivity.ROBOT_IP, null);
            controlPORT = this.sharedPreferences.getInt(MainActivity.CONTROL_PORT, -1);
            heartbeatPORT = this.sharedPreferences.getInt(MainActivity.HEARTBEAT_PORT, -1);

            startButton.setEnabled(MainActivity.startButtonState);
            stopButton.setEnabled(MainActivity.stopButtonState);
            resumeButton.setEnabled(MainActivity.resumeButtonState);

            if (MainActivity.autoTabcurrentIndex != 0) {
                currentIndex = MainActivity.autoTabcurrentIndex - 1;
                actionSlider();
            } else {
                /**
                 * Set the initial script command
                 */
                pastPastTextView.setText("");
                pastTextView.setText("");
                currentTextView.setText("");
                nextTextView.setText("");
                nextNextTextView.setText("");
                startButton.setEnabled(true);
                currentIndex = 0;

                if (displayList != null) {
                    if (displayList.size() > 2) {
                        currentTextView.setText(displayList.get(currentIndex));
                        nextTextView.setText(displayList.get(currentIndex + 1));
                        nextNextTextView.setText(displayList.get(currentIndex + 2));
                    } else if (displayList.size() == 2) {
                        currentTextView.setText(displayList.get(currentIndex));
                        nextTextView.setText(displayList.get(currentIndex + 1));
                    } else if (displayList.size() == 1) {
                        currentTextView.setText(displayList.get(currentIndex));
                    } else {
                        currentTextView.setText("Script has no action/ no script is selected");
                        startButton.setEnabled(false);
                    }
                } else {
                    currentTextView.setText("Script has no action/ no script is selected");
                    startButton.setEnabled(false);
                    stopButton.setEnabled(false);
                    resumeButton.setEnabled(false);
                }
            }
        } else {
            executeOnResume = true;
        }

        // ----------------------------- LIVE VIEW STACK ----------------------------
        Log.d(DEBUG, "onResume called. Starting updater thread if none exist ..");
        if (!updater.isAlive()) {

            // Get IP and PORT from sharedPreferences in LiveViewUpdaterSocket
            String SERVER_IP = sharedPreferences.getString(MainActivity.SERVER_IP, null);
            int PORT = sharedPreferences.getInt(MainActivity.LIVEVIEW_PORT, 0);

            updater = new LiveViewUpdaterSocket(this, SERVER_IP, PORT);
            updater.start();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(DEBUG, "AUTOTAB ONPAUSE");
        MainActivity.autoTabcurrentIndex = currentIndex;

        MainActivity.startButtonState = startButton.isEnabled();
        MainActivity.stopButtonState = stopButton.isEnabled();
        MainActivity.resumeButtonState = resumeButton.isEnabled();

        Log.d(DEBUG, "startState = " + MainActivity.startButtonState);
        Log.d(DEBUG, "stopState = " + MainActivity.stopButtonState);
        Log.d(DEBUG, "resumeState = " + MainActivity.resumeButtonState);

        // ----------------------------- LIVE VIEW STACK ----------------------------

        // When on pause is called, stop the updater thread
        Log.d(DEBUG, "onPause called. Stopping updater thread ..");
        updater.kill();
    }


    /**
     * use to slide the action to show what is executing
     */
    public void actionSlider() {
        if (currentIndex - 1 < 0) {
            pastPastTextView.setText("");
        } else {
            pastPastTextView.setText(displayList.get(currentIndex - 1));
        }

        if (currentIndex + 1 > displayList.size() - 1) {
            currentTextView.setText("");
            pastTextView.setText(displayList.get(currentIndex));
        } else {
            pastTextView.setText(displayList.get(currentIndex));
            currentTextView.setText(displayList.get(currentIndex + 1));
        }

        if (currentIndex + 2 > displayList.size() - 1) {
            nextTextView.setText("");
        } else {
            nextTextView.setText(displayList.get(currentIndex + 2));
        }

        if (currentIndex + 3 > displayList.size() - 1) {
            nextNextTextView.setText("");
        } else {
            nextNextTextView.setText(displayList.get(currentIndex + 3));
        }

        currentIndex++;
    }

    // Allow updater thread to access the liveViewHandler
    @Override
    public Handler getLiveViewHandler() {
        return liveViewHandler;
    }

    // Set image data passed by updater thread as local data
    @Override
    public void setLiveViewData(Bitmap imageData) {
        this.imageData = imageData;
    }

}