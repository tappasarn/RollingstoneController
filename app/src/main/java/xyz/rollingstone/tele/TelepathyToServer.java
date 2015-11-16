package xyz.rollingstone.tele;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import xyz.rollingstone.packet.Banana;

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
    private Banana receivedBanana;
    public static final String TAG = "TeleToServer.DEBUG";
    private int mask = 0b00_1_11_111;
    private Handler handler;

    public TelepathyToServer(String serverAddress, int port, Handler handler) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.handler = handler;
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
        this.socket.setSoTimeout(10);
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
            Log.d(TAG, "Sending this" + String.format("%8s", Integer.toBinaryString(params[0])).replace(' ', '0'));

            // put raw byte into banana class and retrieve command
            receivedBanana = new Banana(this.receive()[0]);

            if ((params[0] & mask) == (receivedBanana.fruit() & mask)) {
                Log.d(TAG, "Yeah, we got the correct REQ/ACK");
                Log.d(TAG, receivedBanana.toString());

                Message msg = Message.obtain();
                Bundle b = new Bundle();
                b.putSerializable("status", "OK");
                msg.setData(b);
                handler.sendMessage(msg);

            } else {
                Log.d(TAG, "an error occured while communicate with the server. Please try again");
                Log.d(TAG, receivedBanana.toString());

                Message msg = Message.obtain();
                Bundle b = new Bundle();
                b.putSerializable("status", "NO");
                msg.setData(b);
                handler.sendMessage(msg);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());

            Message msg = Message.obtain();
            Bundle b = new Bundle();
            b.putSerializable("status", "NO");
            msg.setData(b);
            handler.sendMessage(msg);
        } catch (ConnectException e) {
            Log.d(TAG, e.getMessage());

            Message msg = Message.obtain();
            Bundle b = new Bundle();
            b.putSerializable("status", "NO");
            msg.setData(b);
            handler.sendMessage(msg);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());

            Message msg = Message.obtain();
            Bundle b = new Bundle();
            b.putSerializable("status", "NO");
            msg.setData(b);
            handler.sendMessage(msg);
        } catch (Exception e) {
            Message msg = Message.obtain();
            Bundle b = new Bundle();
            b.putSerializable("status", "NO");
            msg.setData(b);
            handler.sendMessage(msg);
        }

        return null;
    }

    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

}
