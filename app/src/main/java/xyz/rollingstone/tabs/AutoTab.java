package xyz.rollingstone.tabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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
import java.util.List;

import xyz.rollingstone.Action;
import xyz.rollingstone.ActionSQLHelper;
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
        final int robotPORT = this.sharedPreferences.getInt(MainActivity.LIVEVIEW_PORT, -1);

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
            Log.d(DEBUG, selectedScripts.toString());
            Toast.makeText(getActivity(), bundle.getStringArrayList("SELECTED").toString(), Toast.LENGTH_SHORT).show();
<<<<<<< HEAD
=======

>>>>>>> master
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
            HeartBeat HB = new HeartBeat(robotIP, robotPORT, robotPORT+1, packetList);
            HB.execute();
        } else {
            Log.d(DEBUG, "No Automated Script set yet");
        }

    }

}