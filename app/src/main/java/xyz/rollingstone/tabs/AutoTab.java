package xyz.rollingstone.tabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import xyz.rollingstone.Action;
import xyz.rollingstone.ActionSQLHelper;
import xyz.rollingstone.Banana;
import xyz.rollingstone.MainActivity;
import xyz.rollingstone.R;
import xyz.rollingstone.TelepathyToServer;

public class AutoTab extends Fragment {

    public static String tableName;
    private static List<String> displayList;
    private static List<String> selectedScripts;
    public static final String DEBUG = "AutoTab.DEBUG";
    private Banana banana;
    private static TelepathyToServer telepathyToServer;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_run_script, container, false);
    }



    @Override
    public void onStart() {
        super.onStart();
        this.sharedPreferences = getActivity().getSharedPreferences(
                MainActivity.PREFERENCES, Context.MODE_PRIVATE);

        //Toast.makeText(getContext(), String.format("page 2 born"), Toast.LENGTH_SHORT).show();
        TextView pastpastTextView = (TextView) getView().findViewById(R.id.pastpastAction);
        pastpastTextView.setTextColor(Color.argb(38, 0, 0, 0));

        TextView pastTextView = ((TextView) getView().findViewById(R.id.pastAction));
        pastTextView.setTextColor(Color.argb(38, 0, 0, 0));

        TextView currentTextView = (TextView) getView().findViewById(R.id.currentAction);
        currentTextView.setTextColor(Color.argb(87, 0, 0, 0));

        TextView nextTextView = (TextView) getView().findViewById(R.id.nextAction);
        nextTextView.setTextColor(Color.argb(54, 0, 0, 0));

        TextView nextnextTextView = (TextView) getView().findViewById(R.id.nextNextAction);
        nextnextTextView.setTextColor(Color.argb(54, 0, 0, 0));

        //things from below are move to this area/////////

        Log.d(DEBUG, "onActivityCreated called");
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            selectedScripts = bundle.getStringArrayList("SELECTED");
            Log.d(DEBUG, selectedScripts.toString());
            Toast.makeText(getActivity(), bundle.getStringArrayList("SELECTED").toString(), Toast.LENGTH_SHORT).show();
            ActionSQLHelper db = new ActionSQLHelper(getActivity());
            displayList = new ArrayList<String>();

            for (String script : selectedScripts) {
                List<Action> actionList = db.getAllActionsFromTable(script);
                for (Action act : actionList) {
                    displayList.add(act.humanize());
                }
            }

            Log.d("IF GOT LIST", displayList.toString());
        } else {
            Log.d("NO LIST", "Nothing is sent yet");
        }

        /*
        final Switch recordSwitch= (Switch) getView().findViewById(R.id.recSwitch);

        final String serverIP = this.sharedPreferences.getString(MainActivity.SERVER_IP, null);
        final int serverPORT = this.sharedPreferences.getInt(MainActivity.SERVER_PORT, -1);
        final int resolution = this.sharedPreferences.getInt(MainActivity.RES_POS, -1);


        recordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if (isChecked) {
                    banana = new Banana(0,isChecked,resolution);
                } else {
                    banana = new Banana(0,isChecked,resolution);
                }

                telepathyToServer = new TelepathyToServer(serverIP, serverPORT);
                telepathyToServer.execute(banana.fruit());
            }
        });

*/
    }

}