package xyz.rollingstone.tabs;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xyz.rollingstone.Action;
import xyz.rollingstone.ActionSQLHelper;
import xyz.rollingstone.R;

public class AutoTab extends Fragment {

    public static String tableName;
    private static List<String> displayList;
    private static List<String> selectedScripts;
    public static final String DEBUG = "AutoTab.DEBUG";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_run_script, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

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
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(DEBUG, "onActivityCreated called");
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            selectedScripts = bundle.getStringArrayList("SELECTED");
            Log.d(DEBUG, selectedScripts.toString());

            ActionSQLHelper db = new ActionSQLHelper(getContext());
            displayList = new ArrayList<String>();

            for (String script : selectedScripts) {
                List<Action> actionList = db.getAllActionsFromTable(script);
                for (Action act : actionList) {
                    displayList.add(act.humanize());
                }
            }

            Log.d(DEBUG, displayList.toString());
        } else {
            Log.d(DEBUG, "Nothing is sent yet");
        }

    }
}