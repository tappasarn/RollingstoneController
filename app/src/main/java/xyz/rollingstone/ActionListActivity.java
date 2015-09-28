package xyz.rollingstone;
/**
 * main idea is to make change only on displayList then if user click save, just extract from
 * displayList and push them all in database
 */
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
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
    private static String tableName;
    private SQLiteDatabase db;
    private Cursor allCursor;
    public static final String TAG = "TIME";
    public ListView listActs;
    public List<Action> actionList;
    public int old_position;
    public int time_add_counter = 0;
    private ArrayAdapter<String> listAdapter;
    private List<String> displayList;

    /**
     * need to set a RADIO id for checking of selection bcuz Android is too stupid
     */
    public static final int RADIO_ID_FORWARD = 2131492947;
    public static final int RADIO_ID_LEFT = 2131492948;
    public static final int RADIO_ID_RIGHT = 2131492949;
    public static final int RADIO_ID_BACK = 2131492950;

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

            listActs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                               int pos, long id) {
                    Toast toast = Toast.makeText(ActionListActivity.this, String.format("The %s is removed", displayList.get(pos)), Toast.LENGTH_SHORT);
                    toast.show();

                    // remove what's been select from the display list then set the position prior to
                    // what's been deleted
                    displayList.remove(pos);
                    listAdapter.notifyDataSetChanged();
                    old_position = pos;
                    return true;
                }
            });

            listActs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                    Log.d(TAG, String.format("old_position = %d pos = %d", old_position, pos));
                    Log.d(TAG, String.format("Checked = %d", listActs.getSelectedItemPosition()));

                    if (old_position == pos) {
                        listActs.setItemChecked(pos, false);
                        //use for moving old_position to unreal pos allowing toggle
                        old_position = actionList.size();
                    } else {
                        listActs.setItemChecked(pos, true);
                        old_position = pos;
                    }
                }
            });

        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker);
        np.setMaxValue(10);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
    }

    // working on displaying data only,
    public void AddButtonOnClick(View view) {
        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup);
        int id = rg.getCheckedRadioButtonId();
        NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker);

        StringBuilder commandBuilder = new StringBuilder();
        Action act = new Action();
        switch (id) {
            case RADIO_ID_FORWARD:
                act.setDirection("FORWARD");
                commandBuilder.append("FORWARD");
                break;
            case RADIO_ID_LEFT:
                act.setDirection("LEFT");
                commandBuilder.append("LEFT");
                break;
            case RADIO_ID_RIGHT:
                act.setDirection("RIGHT");
                commandBuilder.append("RIGHT");
                break;
            case RADIO_ID_BACK:
                act.setDirection("BACK");
                commandBuilder.append("BACK");
                break;
            default:
                Toast toast = Toast.makeText(ActionListActivity.this, "Please select the direction", Toast.LENGTH_SHORT);
                toast.show();
                return;
        }
        commandBuilder.append(" ");
        commandBuilder.append(np.getValue() + "m");
        act.setLength(np.getValue());

        if (old_position != actionList.size()) {
            displayList.add(old_position + 1, act.humanize());
        } else {
            displayList.add(old_position, act.humanize());
        }

        listAdapter.notifyDataSetChanged();
        Toast toast = Toast.makeText(ActionListActivity.this, commandBuilder.toString(), Toast.LENGTH_SHORT);
        toast.show();

        old_position++;
        time_add_counter++;
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
            act.setDirection(block[0]);
            act.setLength(Integer.parseInt(block[1]));
            db.addActionToTable(tableName, act);
        }
        Toast.makeText(ActionListActivity.this, String.format("Save %s successfully", tableName), Toast.LENGTH_SHORT).show();
    }

}

