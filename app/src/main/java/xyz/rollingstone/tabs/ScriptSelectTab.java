package xyz.rollingstone.tabs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
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
import xyz.rollingstone.MainActivity;
import xyz.rollingstone.R;
import xyz.rollingstone.ViewPagerAdapter;

public class ScriptSelectTab extends Fragment {

    EditText scriptNameEditText;
    ActionSQLHelper db;
    ArrayAdapter<String> listAdapter;
    List<String> allBigsName; //Bigs stands for Script, me so sry i can't remember the name at that time
    ArrayList<Boolean> selectedChecker;
    ArrayList<String> selectedScripts;
    ListView listView;
    // SQLite reserved words
    String[] reservedList = new String[]{"ABORT", "ACTION", "ADD", "AFTER", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ATTACH", "AUTOINCREMENT", "BEFORE", "BEGIN", "BETWEEN", "BY", "CASCADE", "CASE", "CAST", "CHECK", "COLLATE", "COLUMN", "COMMIT", "CONFLICT", "CONSTRAINT", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "DATABASE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DETACH", "DISTINCT", "DROP", "EACH", "ELSE", "END", "ESCAPE", "EXCEPT", "EXCLUSIVE", "EXISTS", "EXPLAIN", "FAIL", "FOR", "FOREIGN", "FROM", "FULL", "GLOB", "GROUP", "HAVING", "IF", "IGNORE", "IMMEDIATE", "IN", "INDEX", "INDEXED", "INITIALLY", "INNER", "INSERT", "INSTEAD", "INTERSECT", "INTO", "IS", "ISNULL", "JOIN", "KEY", "LEFT", "LIKE", "LIMIT", "MATCH", "NATURAL", "NO", "NOT", "NOTNULL", "NULL", "OF", "OFFSET", "ON", "OR", "ORDER", "OUTER", "PLAN", "PRAGMA", "PRIMARY", "QUERY", "RAISE", "RECURSIVE", "REFERENCES", "REGEXP", "REINDEX", "RELEASE", "RENAME", "REPLACE", "RESTRICT", "RIGHT", "ROLLBACK", "ROW", "SAVEPOINT", "SELECT", "SET", "TABLE", "TEMP", "TEMPORARY", "THEN", "TO", "TRANSACTION", "TRIGGER", "UNION", "UNIQUE", "UPDATE", "USING", "VACUUM", "VALUES", "VIEW", "VIRTUAL", "WHEN", "WHERE", "WITH", "WITHOUT"};
    private int current_position = -1;

    private ViewPagerAdapter viewPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.script_select_tab, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get view pager adapter from main activity
        viewPagerAdapter = ((MainActivity) getActivity()).getViewPagerAdapter();

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


        // Implement the list view so it can receive long click and normal click to delete
        listView.setLongClickable(true);
        listView.setClickable(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

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
        FloatingActionButton addBtn = (FloatingActionButton) getView().findViewById(R.id.addButton);
        FloatingActionButton uploadBtn = (FloatingActionButton) getView().findViewById(R.id.uploadButton);
        FloatingActionButton editBtn = (FloatingActionButton) getView().findViewById(R.id.editButton);
        FloatingActionButton deleteBtn = (FloatingActionButton) getView().findViewById(R.id.deleteButton);

        // Add Button Listener
        addBtn.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {

                                          AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                          builder.setTitle("Add ActionList");
                                          builder.setMessage("Please specify the name");

                                          scriptNameEditText = new EditText(getActivity());
                                          builder.setView(scriptNameEditText);
                                          InputFilter filter = new InputFilter() {
                                              public CharSequence filter(CharSequence source, int start, int end,
                                                                         Spanned dest, int dstart, int dend) {
                                                  if (!source.toString().matches("[a-zA-Z.? ]*")) {
                                                      Toast aviso = Toast.makeText(getActivity(), "Numbers & Special characters are not allowed", Toast.LENGTH_LONG);
                                                      aviso.show();
                                                      return "";
                                                  }
                                                  return null;
                                              }
                                          };
                                          scriptNameEditText.setFilters(new InputFilter[]{filter});
                                          // set positive button
                                          builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                      @Override
                                                      public void onClick(DialogInterface dialog, int which) {

                                                          String txt = scriptNameEditText.getText().toString();
                                                          List<String> allBigsName = db.getAllBigsName();

                                                          boolean reservedWordMatch = false;
                                                          boolean dbNameExist = false;
                                                          for (String reserve : reservedList) {
                                                              if (reserve.toLowerCase().equals(txt.toLowerCase())) {
                                                                  reservedWordMatch = true;
                                                              }
                                                          }
                                                          for (String dbName : allBigsName) {
                                                              if (dbName.toLowerCase().equals(txt.toLowerCase())) {
                                                                  dbNameExist = true;
                                                              }
                                                          }


                                                          if (txt.equals("")) {
                                                              Toast.makeText(getActivity(), "Please Input Script Name", Toast.LENGTH_SHORT).show();
                                                              dialog.dismiss();
                                                          } else if (reservedWordMatch) {
                                                              Toast.makeText(getActivity(), "Please Don't Use Reserved Words", Toast.LENGTH_SHORT).show();
                                                              dialog.dismiss();
                                                          } else if (dbNameExist) {
                                                              Toast.makeText(getActivity(), "Script Exist", Toast.LENGTH_SHORT).show();
                                                              dialog.dismiss();
                                                          } else {
                                                              Toast.makeText(getActivity(), "Script " + txt + " is added", Toast.LENGTH_LONG).show();
                                                              db.createActionTable(txt);
                                                              db.addBig(new Big(txt));

                                                              //new added
                                                              selectedChecker.add(false);

                                                              listView.setItemChecked(selectedChecker.size() - 1, false);
                                                              listAdapter.add(txt);
                                                              listAdapter.notifyDataSetChanged();
                                                          }
                                                      }
                                                  }

                                          );

                                          // set negative Button
                                          builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()

                                                  {
                                                      @Override
                                                      public void onClick(DialogInterface dialog, int which) {
                                                          dialog.dismiss();
                                                      }
                                                  }

                                          );

                                          AlertDialog alertDialog = builder.create();
                                          alertDialog.show();
                                      }
                                  }

        );

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
                                   }

        );

        // Upload Button Listener
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checked = listView.getCheckedItemPositions();

                /*
                    Log.d("uploadBtn Checked", checked.toString());
                    Log.d("SelectedScript", selectedScripts.toString());
                */

                if (selectedScripts.size() > 0) {

                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("SELECTED", selectedScripts);

                    MainActivity.selectedScripts = selectedScripts;
                    MainActivity.autoTabcurrentIndex = 0;

                    viewPagerAdapter.getAutoTab().updateScript();

                } else {
                    Toast.makeText(getActivity(), "Please select the script first", Toast.LENGTH_SHORT).show();
                }

            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SparseBooleanArray checked = listView.getCheckedItemPositions();
                /*
                    Log.d("deleteBtn Checked", checked.toString());
                    Log.d("SelectedScript", selectedScripts.toString());
                */
                if (selectedScripts.size() > 0) {
                    for (String script : selectedScripts) {
                        db.deleteBigByName(script);
                        allBigsName.remove(script);
                    }

                    // allocate these tappasarn things
                    selectedScripts = new ArrayList<String>();
                    selectedChecker = new ArrayList<Boolean>();

                    // appending all false to the selected checker so it is saying that nothing has been check yet
                    for (int i = 0; i < allBigsName.size(); i++) {
                        selectedChecker.add(i, false);
                    }

                    //loop for changing color back to unchecked
                    for (int i = 0; i < selectedChecker.size(); i++) {
                        listView.setItemChecked(i, false);
                    }

                    listAdapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), selectedScripts.toString() + " are deleted", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getActivity(), "Please select the script first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
