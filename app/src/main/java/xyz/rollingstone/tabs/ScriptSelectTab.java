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
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_select_script, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView listView = (ListView)getView().findViewById(R.id.list);

        db = new ActionSQLHelper(getContext());
        //CRUD goes here
        allBigsName = db.getAllBigsName();
        listAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_activated_1, allBigsName);
        listView.setAdapter(listAdapter);
    }

    /**
     * Create a Dialog for the Script name
     *
     * @param view
     */
    public void addOnClick(View view) {

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

    public void editOnClick(View view) {

        Intent intent = new Intent(getContext(), ActionListActivity.class);
        intent.putExtra(ActionListActivity.EXTRA_TBNAME, allBigsName.get(position));
        startActivity(intent);

    }

    public void runOnClick(View view) {

    }

}
