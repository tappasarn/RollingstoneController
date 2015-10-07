package xyz.rollingstone;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Common Room on 10/6/2015.
 * Basically share the same code as TelepathyToRobot, consider merging soon
 */
public class TelepathyToServer extends AsyncTask<Integer, Void, Void> {
    private String serverAddress;
    private int port;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Banana banana;
    public static final String TAG = "TeleToServer.DEBUG";

    public TelepathyToServer(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }

    public void openSocket() throws UnknownHostException, IOException {
        this.socket = new Socket(this.serverAddress, this.port);
    }

    public void sendBanana(int banana) throws IOException, Exception {
        this.openSocket();
        this.outputStream = this.socket.getOutputStream();
        outputStream.write(banana);
        outputStream.flush();
    }

    public void closeSocket() throws Exception {
        this.socket.close();
    }

    public byte[] receive() throws SocketTimeoutException, SocketException, IOException, TimeoutException, NullPointerException {
        byte[] ans = new byte[1];

        //not sure if this setTimeOut works
        this.socket.setSoTimeout(1000);
        this.inputStream = this.socket.getInputStream();
        this.inputStream.read(ans, 0, 1);

        return ans;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        try {
            this.closeSocket();
        } catch (Exception e) {
            Log.d(TAG, "Trying to close the socket, the server is already closed");
        }
    }

    @Override
    protected Void doInBackground(Integer... params) {
        try {
            this.sendBanana(params[0]);
            Log.d("SendToServer", String.format("%8s", Integer.toBinaryString(params[0])).replace(' ', '0'));

            // put raw byte into banana class and retrieve command
            banana = new Banana(this.receive()[0]);
            banana.squeeze();

            Log.d("Receive", banana.toString());

        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.d(TAG, "Please check your Server ip");
        } catch (ConnectException e) {
            Log.d(TAG, "failed to connect to " + this.serverAddress + " port " + this.port + " ETIMEDOUT (Connection timed out)");
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d(TAG, "NullPointerException The server is closed");
        } catch (Exception e) {
            Log.d(TAG, "The server is already closed");
        } finally {
            return null;
        }

    }
    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

}
