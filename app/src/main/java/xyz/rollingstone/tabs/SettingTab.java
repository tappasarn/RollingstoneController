package xyz.rollingstone.tabs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import xyz.rollingstone.MainActivity;
import xyz.rollingstone.R;

public class SettingTab extends Fragment {

    private SharedPreferences sharedPreferences;

    private EditText ipEditText, portEditText;
    private Button settingSaveBtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.sharedPreferences = getActivity().getSharedPreferences(
                MainActivity.PREFERENCES, Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_tab,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Locate UI Elements
        this.ipEditText = (EditText) getView().findViewById(R.id.ipEditText);
        this.portEditText = (EditText) getView().findViewById(R.id.portEditText);
        this.settingSaveBtn = (Button) getView().findViewById(R.id.settingSaveBtn);

        // Add event listener for save button
        this.settingSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(MainActivity.LIVEVIEW_IP, ipEditText.getText().toString());
                    editor.putInt(MainActivity.LIVEVIEW_PORT, Integer.parseInt(portEditText.getText().toString()));
                    editor.commit();

                    hideSoftKeyboard(getActivity());

                    Toast.makeText(getActivity(), "Saved", Toast.LENGTH_SHORT).show();

                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Format error! Please check again!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // Populate values of IP and PORt if existed
        String IP = sharedPreferences.getString(MainActivity.LIVEVIEW_IP, null);
        int PORT = sharedPreferences.getInt(MainActivity.LIVEVIEW_PORT, -1);

        if( IP != null ) {
            ipEditText.setText(IP);
        }

        if( PORT != -1 ) {
            portEditText.setText(String.valueOf(PORT));
        }

    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}