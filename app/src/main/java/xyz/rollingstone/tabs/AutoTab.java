package xyz.rollingstone.tabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import xyz.rollingstone.Action;
import xyz.rollingstone.ActionSQLHelper;
import xyz.rollingstone.ResumeIndicator;
import xyz.rollingstone.packet.Banana;
import xyz.rollingstone.packet.CommandPacketBuilder;
import xyz.rollingstone.HeartBeat;
import xyz.rollingstone.MainActivity;
import xyz.rollingstone.R;
import xyz.rollingstone.tele.TelepathyToServer;

public class AutoTab extends Fragment {

    public static String tableName;
    private static List<String> displayList;
    private static List<String> anotherDisplayList;
    private static List<int[]> packetList;
    private static List<String> selectedScripts;
    public static final String DEBUG = "AutoTab.DEBUG";
    private Banana banana;
    private static TelepathyToServer telepathyToServer;
    private SharedPreferences sharedPreferences;

    private Button startButton;
    private Button resumeButton;
    private Button stopButton;

    private TextView pastpastTextView;
    private TextView pastTextView;
    private TextView currentTextView;
    private TextView nextTextView;
    private TextView nextnextTextView;
    private Handler handler;

    public ResumeIndicator resumeIndicator;

    final Integer REQ_A_TYPE = 2;

    String robotIP;
    int controlPORT;
    int heartbeatPORT;
    HeartBeat HB;
    Integer currentIndex;
    private boolean executeOnResume = false;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.auto_tab, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        this.sharedPreferences = getActivity().getSharedPreferences(
                MainActivity.PREFERENCES, Context.MODE_PRIVATE);
        this.startButton = (Button) getView().findViewById(R.id.startButton);
        this.resumeButton = (Button) getView().findViewById(R.id.resumeButton);
        this.stopButton = (Button) getView().findViewById(R.id.stopButton);

        MainActivity.startButtonState = true;
        MainActivity.stopButtonState = false;
        MainActivity.resumeButtonState = false;

        startButton.setEnabled(MainActivity.startButtonState);
        stopButton.setEnabled(MainActivity.stopButtonState);
        resumeButton.setEnabled(MainActivity.resumeButtonState);

        robotIP = this.sharedPreferences.getString(MainActivity.ROBOT_IP, null);
        controlPORT = this.sharedPreferences.getInt(MainActivity.CONTROL_PORT, -1);
        heartbeatPORT = this.sharedPreferences.getInt(MainActivity.HEARTBEAT_PORT, -1);

        currentIndex = 0;
        /*
            To Adjust the color of TextView
         */
        pastpastTextView = (TextView) getView().findViewById(R.id.pastpastAction);
        pastpastTextView.setTextColor(Color.argb(38, 0, 0, 0));

        pastTextView = ((TextView) getView().findViewById(R.id.pastAction));
        pastTextView.setTextColor(Color.argb(38, 0, 0, 0));

        currentTextView = (TextView) getView().findViewById(R.id.currentAction);
        currentTextView.setTextColor(Color.argb(87, 0, 0, 0));

        nextTextView = (TextView) getView().findViewById(R.id.nextAction);
        nextTextView.setTextColor(Color.argb(54, 0, 0, 0));

        nextnextTextView = (TextView) getView().findViewById(R.id.nextNextAction);
        nextnextTextView.setTextColor(Color.argb(54, 0, 0, 0));

        if (MainActivity.selectedScripts != null) {

            /* if there is at least 1 selected script, get the table which has the same name as them */
            selectedScripts = MainActivity.selectedScripts;
            ActionSQLHelper db = new ActionSQLHelper(getActivity());
            displayList = new ArrayList<String>();
            anotherDisplayList = new ArrayList<String>();

            packetList = new ArrayList<int[]>();
            CommandPacketBuilder commandPacketBuilder;

            /* loop through every script, we are using 2 Lists here, 1 for keeping display data to be displayed on UI,
              * another one is to keep the commandPacketList to be sent to the robot
               * */
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

            /**
             * Set the initial script command
             */
            pastpastTextView.setText("");
            pastTextView.setText("");
            currentTextView.setText("");
            nextTextView.setText("");
            nextnextTextView.setText("");
            startButton.setEnabled(true);
            currentIndex = 0;
            if (displayList.size() > 2) {
                currentTextView.setText(displayList.get(currentIndex));
                nextTextView.setText(displayList.get(currentIndex + 1));
                nextnextTextView.setText(displayList.get(currentIndex + 2));
            } else if (displayList.size() == 2) {
                currentTextView.setText(displayList.get(currentIndex));
                nextTextView.setText(displayList.get(currentIndex + 1));
            } else if (displayList.size() == 1) {
                currentTextView.setText(displayList.get(currentIndex));
            } else {
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

                        pastpastTextView.setText("");
                        pastTextView.setText("");
                        currentTextView.setText("");
                        nextTextView.setText("");
                        nextnextTextView.setText("");
                        startButton.setEnabled(true);
                        currentIndex = 0;

                        if (displayList.size() > 2) {
                            currentTextView.setText(displayList.get(currentIndex));
                            nextTextView.setText(displayList.get(currentIndex + 1));
                            nextnextTextView.setText(displayList.get(currentIndex + 2));
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
                    HB = new HeartBeat(robotIP, controlPORT, heartbeatPORT, packetList, handler, resumeIndicator);
                    //HB.cancel(false);
                    HB.execute();
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
                pastpastTextView.setText("");
                pastTextView.setText("");
                currentTextView.setText("");
                nextTextView.setText("");
                nextnextTextView.setText("");
                startButton.setEnabled(true);
                currentIndex = 0;

                if (displayList != null) {
                    if (displayList.size() > 2) {
                        currentTextView.setText(displayList.get(currentIndex));
                        nextTextView.setText(displayList.get(currentIndex + 1));
                        nextnextTextView.setText(displayList.get(currentIndex + 2));
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
    }

    /**
     * use to slide the action to show what is executing
     */
    public void actionSlider() {
        if (currentIndex - 1 < 0) {
            pastpastTextView.setText("");
        } else {
            pastpastTextView.setText(displayList.get(currentIndex - 1));
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
            nextnextTextView.setText("");
        } else {
            nextnextTextView.setText(displayList.get(currentIndex + 3));
        }

        currentIndex++;
    }

}