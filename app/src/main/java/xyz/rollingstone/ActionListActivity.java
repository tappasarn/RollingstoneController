package xyz.rollingstone;

/**
 * main idea is to make change only on displayList then if user click save, just extract from
 * displayList and push them all in database
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class ActionListActivity extends Activity {

    public static final String EXTRA_TBNAME = "table_name";
    public static final String TAG = "ActionEditor.DEBUG";

    private static String tableName;
    public ListView listActs;
    public List<Action> actionList;
    private ArrayAdapter<String> listAdapter;
    private List<String> displayList;
    private int old_position = -1, current_position = -1;
    private boolean isSaved = true;
    private RadioGroup rg;
    private NumberPicker np;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_list);

        // get table name
        tableName = getIntent().getStringExtra(EXTRA_TBNAME);
        listActs = (ListView) findViewById(R.id.list_options);
        Log.d("listAct", listActs.toString());

        try {
            // connect to db and get all actions from the table
            ActionSQLHelper db = new ActionSQLHelper(this);
            actionList = db.getAllActionsFromTable(tableName);
            displayList = new ArrayList<String>();

            // reformat an object of Act class to be readable by Noppadol
            for (Action act : actionList) {
                displayList.add(act.humanize());
            }

            //set the select pointer to the last, in case user didn't select any of them
            old_position = actionList.size();

            listAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_activated_1, displayList);

            // set the adapter & LongClickListener
            listActs.setAdapter(listAdapter);
            listActs.setLongClickable(true);
            listActs.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            // delete is here
            listActs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                               int pos, long id) {
                    Toast toast = Toast.makeText(ActionListActivity.this, String.format("The %s is removed", displayList.get(pos)), Toast.LENGTH_SHORT);
                    toast.show();

                    displayList.remove(pos);
                    listAdapter.notifyDataSetChanged();
                    old_position = displayList.size();

                    // in case we delete the last row, so we need to set in back in scope
                    // -1 is used for append the list
                    if (current_position >= displayList.size()) {
                        listActs.setItemChecked(current_position, false);
                        current_position = -1;
                    }

                    return true;
                }
            });

            listActs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                    /*
                    Log.d(TAG, String.format("old_position = %d pos = %d", old_position, pos));
                    Log.d(TAG, String.format("Checked = %d", listActs.getSelectedItemPosition()));
                    */
                    //unchecking item in list view
                    if (old_position == pos) {
                        /*
                        Toast toast = Toast.makeText(ActionListActivity.this, String.format("The script %s is uncheck", displayList.get(pos)), Toast.LENGTH_SHORT);
                        toast.show();
                        */
                        listActs.setItemChecked(pos, false);

                        //use for moving old_position to unreal pos allowing toggle
                        //un-select the current position and old position since there are nothing selected
                        old_position = -1;
                        current_position = -1;
                    }
                    //for checking item on list view
                    else {
                        /*
                        Toast toast = Toast.makeText(ActionListActivity.this, String.format("The script %s is check", displayList.get(pos)), Toast.LENGTH_SHORT);
                        toast.show();
                        */
                        listActs.setItemChecked(pos, true);
                        old_position = pos; // for compare position of the next check
                        current_position = pos; //setting the current position
                    }

                }
            });

        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        np = (NumberPicker) findViewById(R.id.numberPicker);
        np.setMaxValue(100);
        np.setMinValue(30);
        np.setWrapSelectorWheel(false);

        rg = (RadioGroup) findViewById(R.id.radioGroup);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
            // disable number picker if Left or Right is pressed
            if (checkedId == R.id.LeftRadioButton || checkedId == R.id.RightRadioButton) {
                np.setEnabled(false);
            } else {
                np.setEnabled(true);
            }
            }
        });
    }

    // working on displaying data only,
    public void AddButtonOnClick(View view) {

        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup);
        int id = rg.getCheckedRadioButtonId();
        NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker);

        Action act = new Action();

        switch (id) {
            case R.id.ForwardRadioButton:
                act.setDirection("FORWARD");
                act.setLength(np.getValue());
                break;
            case R.id.LeftRadioButton:
                act.setDirection("LEFT");
                act.setLength(0);
                break;
            case R.id.RightRadioButton:
                act.setDirection("RIGHT");
                act.setLength(0);
                break;
            case R.id.BackRadioButton:
                act.setDirection("BACK");
                act.setLength(np.getValue());
                break;
            default:
                Toast toast = Toast.makeText(ActionListActivity.this, "Please select the direction", Toast.LENGTH_SHORT);
                toast.show();
                return;
        }

        if (current_position != -1) {
            displayList.add(current_position + 1, act.humanize());
        } else {
            displayList.add(displayList.size(), act.humanize());
            old_position = -1;//make sure the tracking position is detach from the list
        }

        listAdapter.notifyDataSetChanged();
        Toast toast = Toast.makeText(ActionListActivity.this, act.humanize(), Toast.LENGTH_SHORT);
        toast.show();
        isSaved = false;

    }

    /*
        Create delete an existing database, create a new one then get what's on displayList
        make it as a Action class then fucking put them into the database with the specify table
     */
    public void SaveButtonOnClick(View view) {
        ActionSQLHelper db = new ActionSQLHelper(this);
        db.deleteTableByName(tableName);
        db.createActionTable(tableName);

        ContentValues actValues = new ContentValues();
        for (String str : displayList) {
            Action act = new Action();
            String block[] = str.split("\\s+");
            if (block[0].equals("FORWARD") || block[0].equals("BACK")) {
                act.setDirection(block[0]);
                act.setLength(Integer.parseInt(block[1]));
            } else {
                act.setDirection(block[0]);
                act.setLength(0);
            }
            db.addActionToTable(tableName, act);
        }
        Toast.makeText(ActionListActivity.this, String.format("Save %s successfully", tableName), Toast.LENGTH_SHORT).show();
        isSaved = true;
    }

    //this method ask user if they have saved their list
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK && !isSaved) {

            //Ask the user if they want to quit
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("DID YOU SAVED?")
                    .setMessage("if you didn't, the progress will be lost")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //Stop the activity
                            ActionListActivity.this.finish();
                        }

                    })
                    .setNegativeButton("cancel", null)
                    .show();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}

