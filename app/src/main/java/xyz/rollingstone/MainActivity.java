package xyz.rollingstone;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    // Declaring Your View and Variables
    private CustomViewPager pager;
    private ViewPagerAdapter adapter;
    private SlidingTabLayout tabs;
    private CharSequence titles[] = {"Manual", "List", "Add", "Settings"};
    private int numTabs = 4;
    private boolean toggle = false;
    private SharedPreferences sharedPreferences;

    private Banana banana;
    private static TelepathyToServer telepathyToServer;

    private String serverIP;
    private int serverPORT;
    private int resolution;


    public static final String PREFERENCES = "xyz.rollingstone.preferences";
    public static final String LIVEVIEW_IP = "xyz.rollingstone.liveview.ip";
    public static final String LIVEVIEW_PORT = "xyz.rollingstone.liveview.port";
    public static final String SERVER_IP = "xyz.rollingstone.server.ip";
    public static final String SERVER_PORT = "xyz.rollingstone.server.port";
    public static final String RES_POS = "xyz.rollingstone.resolution.pos";
    public static final String CONTROL_PORT = "xyz.rollingstone.control.port";
    //res_pos 0 = 480p, 1 = 720p, 2 = 1080p

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, titles fot the Tabs and Number Of Tabs.
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, numTabs);

        // Assigning ViewPager View and setting the adapter
        pager = (CustomViewPager) findViewById(R.id.pager);
        pager.setPagingEnabled(false);
        pager.setAdapter(adapter);

        // Assign the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsClickColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        sharedPreferences = this.getSharedPreferences(
                MainActivity.PREFERENCES, Context.MODE_PRIVATE);

        serverIP = this.sharedPreferences.getString(MainActivity.SERVER_IP, null);
        serverPORT = this.sharedPreferences.getInt(MainActivity.SERVER_PORT, -1);
        resolution = this.sharedPreferences.getInt(MainActivity.RES_POS, -1);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    //Override to specifically handle the volume buttons
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();

        // if the volume up or down is pressed, then pass value of (REQ on/off, resolution) connect to the server
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                toggle = !toggle;
                banana = new Banana(0, toggle, resolution);
                telepathyToServer = new TelepathyToServer(serverIP, serverPORT);
                telepathyToServer.execute(banana.fruit());
                return true;
            }
        } else {
            return super.dispatchKeyEvent(event);
        }
        return true;
    }

}
