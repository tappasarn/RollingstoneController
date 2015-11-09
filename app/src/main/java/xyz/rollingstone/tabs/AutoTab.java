package xyz.rollingstone.tabs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import xyz.rollingstone.Action;
import xyz.rollingstone.ActionSQLHelper;
import xyz.rollingstone.Big;
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

    private TextView pastpastTextView;
    private TextView pastTextView;
    private TextView currentTextView;
    private TextView nextTextView;
    private TextView nextnextTextView;
    private Integer currentIndex = 0;
    private Handler handler;

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

        final String robotIP = this.sharedPreferences.getString(MainActivity.LIVEVIEW_IP, null);
        final int controlPORT = this.sharedPreferences.getInt(MainActivity.CONTROL_PORT, -1);
        final int heartbeatPORT = this.sharedPreferences.getInt(MainActivity.HEARTBEAT_PORT, -1);

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

        Bundle bundle = this.getArguments();

        if (bundle != null) {

            /* if there is at least 1 selected script, get the table which has the same name as them */
            selectedScripts = bundle.getStringArrayList("SELECTED");
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
                    commandPacketBuilder.setType(0);
                    commandPacketBuilder.setId(0);

                    packetList.add(commandPacketBuilder.Create());
                }
            }

            /**
             * Set the initial script command
             */
            pastpastTextView.setText("");
            pastTextView.setText("");
            currentTextView.setText(displayList.get(currentIndex));
            nextTextView.setText(displayList.get(currentIndex + 1));
            nextnextTextView.setText(displayList.get(currentIndex + 2));

            handler = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    String messages = (String) msg.getData().getSerializable("status");
                    if (messages == "OK") {
                        actionSlider();
                    } else if (messages == "CNNERR") {
                        Toast.makeText(getActivity(), "The operation is aborted, can't connect to server", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HeartBeat HB = new HeartBeat(robotIP, controlPORT, heartbeatPORT, packetList, handler);
                    HB.execute();
                    Log.d(DEBUG, "Hello, Im executing");
                }
            });
        } else {
            Log.d(DEBUG, "No Automated Script set yet");
        }

    }

    public int actionSlider() {
        if (currentIndex - 1 < 0) {
            pastpastTextView.setText("");
        } else {
            pastpastTextView.setText(displayList.get(currentIndex - 1));
        }

        pastTextView.setText(displayList.get(currentIndex));

        if (currentIndex + 1 > displayList.size()-1) {
            currentTextView.setText(displayList.get(currentIndex));
        } else {
            currentTextView.setText(displayList.get(currentIndex + 1));
        }

        if (currentIndex + 2 > displayList.size()-1) {
            nextTextView.setText("");
        } else {
            nextTextView.setText(displayList.get(currentIndex + 2));
        }

        if (currentIndex + 3 > displayList.size()-1) {
            nextnextTextView.setText("");
        } else {
            nextnextTextView.setText(displayList.get(currentIndex + 3));
        }

        currentIndex++;

        return 0;
    }

}