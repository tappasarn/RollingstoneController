package xyz.rollingstone.tabs;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import xyz.rollingstone.ActionListActivity;
import xyz.rollingstone.ActionSQLHelper;
import xyz.rollingstone.Big;
import xyz.rollingstone.R;

/**
 * Created by admin on 9/28/2015.
 */
public class ScriptSelectTab extends Fragment {

    EditText input;
    ActionSQLHelper db;
    ArrayAdapter<String> listAdapter;
    List<String> allBigsName; //Bigs stands for Script, me so sry i can't remember the name at that time
    private int addToPosition, old_position = -1,current_position=-1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_select_script, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ListView listView = (ListView) getView().findViewById(R.id.list);

        db = new ActionSQLHelper(getContext());
        //CRUD goes here
        allBigsName = db.getAllBigsName();
        listAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_activated_1, allBigsName);
        listView.setAdapter(listAdapter);

        //defult adding position will be appending to the last of the list
        addToPosition = allBigsName.size();

        //implement the listview so it can receive long click and normal click to delete
        listView.setLongClickable(true);
        listView.setClickable(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //set listenner for both longclick and normal click
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                Toast toast = Toast.makeText(getContext(), String.format("The script %s is removed", allBigsName.get(pos)), Toast.LENGTH_SHORT);
                toast.show();
                allBigsName.remove(pos);
                listAdapter.notifyDataSetChanged();
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                //unchecking item in list view
                if (old_position == pos) {
                    Toast toast = Toast.makeText(getContext(), String.format("The script %s is uncheck", allBigsName.get(pos)), Toast.LENGTH_SHORT);
                    toast.show();
                    listView.setItemChecked(pos, false);
                    //use for moving old_position to unreal pos allowing toggle
                    old_position = allBigsName.size();
                    current_position = -1;
                }
                //for checking item on list view
                else {
                    Toast toast = Toast.makeText(getContext(), String.format("The script %s is check", allBigsName.get(pos)), Toast.LENGTH_SHORT);
                    toast.show();
                    listView.setItemChecked(pos, true);
                    old_position = pos;
                    current_position = pos;
                }

            }
        });

        ImageButton addBtn = (ImageButton) getView().findViewById(R.id.addButton);
        ImageButton uploadBtn = (ImageButton) getView().findViewById(R.id.uploadButton);
        ImageButton editBtn = (ImageButton) getView().findViewById(R.id.editButton);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Add ActionList");
                builder.setMessage("Please specify the name");

                input = new EditText(getContext());
                builder.setView(input);

                // set positive button
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String txt = input.getText().toString();
                        Toast.makeText(getContext(), txt, Toast.LENGTH_LONG).show();
                        db.createActionTable(txt);
                        db.addBig(new Big(txt));
                        listAdapter.add(txt);
                        listAdapter.notifyDataSetChanged();
                    }
                });

                // set negative Button
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog ad = builder.create();
                ad.show();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ActionListActivity.class);
                intent.putExtra(ActionListActivity.EXTRA_TBNAME, allBigsName.get(current_position));

                startActivity(intent);
            }
        });

    }

    public void editOnClick(View view) {
/*
        Intent intent = new Intent(getContext(), ActionListActivity.class);
        intent.putExtra(ActionListActivity.EXTRA_TBNAME, allBigsName.get(position));
        startActivity(intent);
*/
    }

    public void runOnClick(View view) {

    }

}
