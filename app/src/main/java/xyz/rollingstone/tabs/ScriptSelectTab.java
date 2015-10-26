package xyz.rollingstone.tabs;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import xyz.rollingstone.ActionListActivity;
import xyz.rollingstone.ActionSQLHelper;
import xyz.rollingstone.Big;
import xyz.rollingstone.R;

public class ScriptSelectTab extends Fragment {

    EditText scriptNameEditText;
    ActionSQLHelper db;
    ArrayAdapter<String> listAdapter;
    List<String> allBigsName; //Bigs stands for Script, me so sry i can't remember the name at that time
    ArrayList<Boolean> selectedChecker;
    ArrayList<String> selectedScripts;
    private int addToPosition, old_position = -1, current_position = -1;
    private boolean isSelected = false;
    int selectedScriptsPosition = 0;
    ListView listView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.script_select_tab, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        listView = (ListView) getView().findViewById(R.id.list);

        db = new ActionSQLHelper(getActivity());

        // CRUD goes here
        allBigsName = db.getAllBigsName();

        listAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1, allBigsName);
        listView.setAdapter(listAdapter);

        selectedScripts = new ArrayList<String>();
        selectedChecker = new ArrayList<Boolean>();

        // appending all false to the selected checker so it is saying that nothing has been check yet
        for (int i = 0; i < allBigsName.size(); i++) {
            selectedChecker.add(i, false);
        }

        // Default adding position will be appending to the last of the list
        addToPosition = allBigsName.size();

        // Implement the list view so it can receive long click and normal click to delete
        listView.setLongClickable(true);
        listView.setClickable(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Set listener for both long click and normal click
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                Toast toast = Toast.makeText(getActivity(), String.format("The script %s is removed", allBigsName.get(pos)), Toast.LENGTH_SHORT);
                toast.show();

                //remove from check list
                selectedChecker.remove(pos);
                //Log.d("Time checker", selectedChecker.toString());

                //remove from database
                db.deleteBigByName(allBigsName.get(pos));

                //remove from showing list
                allBigsName.remove(pos);

                //loop for changing color
                for (int i = 0; i < selectedChecker.size(); i++) {
                    listView.setItemChecked(i, selectedChecker.get(i));
                }
                selectedScripts.remove(allBigsName.indexOf(allBigsName.get(pos)));

                listAdapter.notifyDataSetChanged();
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                Boolean bool = selectedChecker.get(pos);
                if (bool == true) {
                    selectedChecker.set(pos, false);
                    selectedScripts.remove(allBigsName.get(pos));
                } else {
                    selectedChecker.set(pos, true);
                    current_position = pos;
                    selectedScripts.add(allBigsName.get(pos));
                }
                /*
                Log.d("Time checker", selectedChecker.toString());
                Log.d("Selected Scripts", selectedScripts.toString());
                */
            }
        });


        // init FAB button and create their listener
        FloatingActionButton addBtn    = (FloatingActionButton) getView().findViewById(R.id.addButton);
        FloatingActionButton uploadBtn = (FloatingActionButton) getView().findViewById(R.id.uploadButton);
        FloatingActionButton editBtn   = (FloatingActionButton) getView().findViewById(R.id.editButton);

        // Add Button Listener
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Add ActionList");
                builder.setMessage("Please specify the name");

                scriptNameEditText = new EditText(getActivity());
                builder.setView(scriptNameEditText);

                // set positive button
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String txt = scriptNameEditText.getText().toString();

                        /**
                         * TODO: implement reserved keyword blocking
                         */
                        if (txt.equals("")) {
                            Toast.makeText(getActivity(), "Please Input Script Name", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), txt, Toast.LENGTH_LONG).show();
                            db.createActionTable(txt);
                            db.addBig(new Big(txt));

                            //new added
                            selectedChecker.add(false);
                            //Log.d("Time checker", selectedChecker.toString());

                            listView.setItemChecked(selectedChecker.size() - 1, false);
                            listAdapter.add(txt);
                            listAdapter.notifyDataSetChanged();
                        }
                    }
                });

                // set negative Button
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        // Edit Button Listener
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //to check if user select more than 1 script before they start edit
                int checkNumberCount = 0;
                for (int i = 0; i < selectedChecker.size(); i++) {
                    if (selectedChecker.get(i)) {
                        checkNumberCount++;
                    }
                }

                //Log.d("editBtnOnClick", Integer.toString(checkNumberCount));
                //three possible cases
                if (checkNumberCount == 1) {
                    Intent intent = new Intent(getActivity(), ActionListActivity.class);
                    intent.putExtra(ActionListActivity.EXTRA_TBNAME, allBigsName.get(current_position));
                    startActivity(intent);
                } else if (checkNumberCount == 0) {
                    Toast.makeText(getActivity(), "Please select the script first", Toast.LENGTH_SHORT).show();
                } else if (checkNumberCount > 1) {
                    Toast.makeText(getActivity(), "Please select only one script for edit", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("editBtnOnClick", "Error occurred here");
                    Log.d("editBtnOnClick", selectedChecker.toString());
                }

            }
        });

        // Upload Button Listener
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checked = listView.getCheckedItemPositions();

                Log.d("uploadBtn Checked", checked.toString());
                Log.d("SelectedScript", selectedScripts.toString());

                int count = 0;
                if (checked.size() > 0) {

                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("SELECTED", selectedScripts);

                    AutoTab autoTab = new AutoTab();
                    autoTab.setArguments(bundle);
                    // in .replace use id.container instead of the old one to fix bug that removes this own page
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.container, autoTab, "SELECTED")
                            .addToBackStack("SELECTED").commit();

                    Toast.makeText(getActivity(), String.format("%d", count), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Please select the script first", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
