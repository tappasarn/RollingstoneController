package xyz.rollingstone.tabs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import xyz.rollingstone.MainActivity;
import xyz.rollingstone.R;

public class SettingTab extends Fragment {

    private SharedPreferences sharedPreferences;

    private EditText robotIpEditText, robotPortEditText, serverIpEditText, serverPortEditText, namePatternEditText;
    private Button settingSaveBtn;
    private Spinner resolutionSpinner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.sharedPreferences = getActivity().getSharedPreferences(
                MainActivity.PREFERENCES, Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_tab, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Locate UI Elements
        this.robotIpEditText = (EditText) getView().findViewById(R.id.robot_ip_editText);
        this.robotPortEditText = (EditText) getView().findViewById(R.id.robot_port_editText);

        this.serverIpEditText = (EditText) getView().findViewById(R.id.server_ip_editText);
        this.serverPortEditText = (EditText) getView().findViewById(R.id.server_port_editText);

        this.resolutionSpinner = (Spinner) getView().findViewById(R.id.resolutionSpinner);
        this.settingSaveBtn = (Button) getView().findViewById(R.id.settingSaveBtn);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.res_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        this.resolutionSpinner.setAdapter(adapter);

        /*
            Populate values of robot&server IP and PORT into EditTexts if the values exist
         */
        String robotIP = sharedPreferences.getString(MainActivity.LIVEVIEW_IP, null);
        int robotPORT = sharedPreferences.getInt(MainActivity.LIVEVIEW_PORT, -1);

        String serverIP = sharedPreferences.getString(MainActivity.SERVER_IP, null);
        int serverPORT = sharedPreferences.getInt(MainActivity.SERVER_PORT, -1);

        int spinnerPosition = sharedPreferences.getInt(MainActivity.RES_POS, -1);

        if (robotIP != null) {
            robotIpEditText.setText(robotIP);
        }

        if (robotPORT != -1) {
            robotPortEditText.setText(String.valueOf(robotPORT));
        }

        if (serverIP != null) {
            serverIpEditText.setText(serverIP);
        }

        if (serverPORT != -1) {
            serverPortEditText.setText(String.valueOf(serverPORT));
        }

        Log.d("SettingTab", Integer.toString(spinnerPosition));
        if (spinnerPosition != -1) {
            resolutionSpinner.setSelection(spinnerPosition);
        }


        // Add event listener for save button
        this.settingSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    Log.d("SettingTab", Integer.toString(resolutionSpinner.getSelectedItemPosition()));

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(MainActivity.LIVEVIEW_IP, robotIpEditText.getText().toString());
                    editor.putInt(MainActivity.LIVEVIEW_PORT, Integer.parseInt(robotPortEditText.getText().toString()));
                    editor.putString(MainActivity.SERVER_IP, serverIpEditText.getText().toString());
                    editor.putInt(MainActivity.SERVER_PORT, Integer.parseInt(serverPortEditText.getText().toString()));

                    // Position
                    editor.putInt(MainActivity.RES_POS, resolutionSpinner.getSelectedItemPosition());

                    editor.commit();

                    hideSoftKeyboard(getActivity());

                    Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();

                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Format error! Please check again!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        IBinder window = this.settingSaveBtn.getWindowToken();
        if (window != null) {
            inputMethodManager.hideSoftInputFromWindow(window, 0);
        }
    }
}