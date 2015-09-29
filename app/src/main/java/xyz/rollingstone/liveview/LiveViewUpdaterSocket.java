package xyz.rollingstone.liveview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;

import xyz.rollingstone.BuildConfig;
import xyz.rollingstone.tabs.*;

public class LiveViewUpdaterSocket extends Thread {

    private Fragment fragment;
    private boolean failed = false;
    private boolean killed = false;

    // IP and PORT of the server
    private String IP ;
    private int PORT;

    // Thread sleep time between each request
    final private static int refreshRate = 5;

    // Create option object for decoder
    final BitmapFactory.Options options = new BitmapFactory.Options();

    // inBitmap and buffer is used for caching and performance improvement
    private Bitmap inBitmap = null;
    private byte[] buffer = new byte[16 * 1024];

    public LiveViewUpdaterSocket(Fragment fragment, String IP, int PORT) {
        this.fragment = fragment;
        this.IP = IP;
        this.PORT = PORT;

        // inBitmap works with mutable only
        options.inMutable = true;
        // assign buffer to avoid reallocating 16K buffer
        options.inTempStorage = buffer;
    }

    @Override
    public void run() {

        // Set priority
        Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);

        Log.d(ManualTab.DEBUG, String.format("LiveViewUpdaterSocket started %s:%s", IP, PORT));

        while( !failed && !killed ) {

            /*
                Reassign inBitmap field with the previous decoded Bitmap.
                According to the doc, decodeStream will reuse our Bitmap object.

                // Note // Reading the source code confirmed this already.
             */
            if( inBitmap != null ) {
                options.inBitmap = inBitmap;
            }

            /*
                Scope stuffs .. it has to be here unless
                we cannot access it from anywhere but in try {} catch block
             */
            InputStream is = null;
            Socket socket = null;

            /*
                Get new Bitmap data from input stream.
                This requires us to allocate Socket for every request.

                We use decodeStream with Bitmap.Options so that we can
                reuse buffer and also Bitmap.
             */
            try {

                socket = new Socket(IP, PORT);
                is = socket.getInputStream();

                inBitmap = BitmapFactory.decodeStream(is, null, options);

            } catch( MalformedURLException e ) {

                Log.d(ManualTab.DEBUG, e.getMessage());
                failed = true;
                break;

            } catch( IOException e ) {

                Log.d(ManualTab.DEBUG, e.getMessage());
                failed = true;
                break;

            } finally {

                try {
                    if (socket != null) {
                        socket.shutdownInput();
                        socket.close();
                    }
                } catch( IOException e ) {
                    Log.d(ManualTab.DEBUG, e.getMessage());
                    failed = true;
                    break;
                }

            }

            /*
                Send new Bitmap from stream via handler on UI Thread
                via a Message and Bundle.
             */
            if( inBitmap != null ) {
                // Create bundle and message
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putInt(ManualTab.MSG, ManualTab.LIVEVIEW_MSG);
                msg.setData(bundle);

                // send message to main thread's handler
                ((ManualTab) fragment).setLiveViewData(inBitmap);
                ((ManualTab) fragment).getHandler().sendMessage(msg);
            }

            /*
                Let's thread wait and do nothing for value in ms
                specified in refreshRate. This allows us to adjust thread not
                to be too aggressive consuming all the cpu time.
             */
            try {
                // If it's killed, no need to sleep, let it die fast.
                if(!killed) {
                    Thread.sleep(refreshRate, 0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        Log.d(ManualTab.DEBUG, "LiveViewUpdaterSocket stopped.");

    }

    public void kill() {
        this.killed = true;
    }

}
