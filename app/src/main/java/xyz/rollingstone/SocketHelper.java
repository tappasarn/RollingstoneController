package xyz.rollingstone;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by Common Room on 10/1/2015.
 */

public class SocketHelper extends AsyncTask<Integer, Void, Void> {
    private String serverAddress;
    private int port;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter out;
    private OutputStream outputStream;
    private InputStream inputStream;


    public SocketHelper(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }

    public void openSocket() {
        try {
            this.socket = new Socket(this.serverAddress, this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(int highByte, int lowByte) {
        this.openSocket();
        Log.d("socketHelper", this.toString());
        try {
            this.outputStream = this.socket.getOutputStream();
            outputStream.write(highByte);
            outputStream.write(lowByte);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] receive() {
        byte[] ans = new byte[2];
        try {
            this.inputStream = this.socket.getInputStream();
            this.inputStream.read(ans,0,2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ans;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Integer... params) {
        Log.d("socketHelperSend1", String.format("%8s", Integer.toBinaryString(params[0])).replace(' ', '0'));
        Log.d("socketHelperSend2", String.format("%8s", Integer.toBinaryString(params[1])).replace(' ', '0'));
        this.send(params[0], params[1]);

        byte[] ans = new byte[2];
        ans = this.receive();

        String[] binArray = new String[2];
        for(int index = 0; index < ans.length; index++) {
            binArray[index] = Integer.toBinaryString(ans[index]);
        }

//        Log.d("socketHelperRecv12", Integer.toBinaryString(Integer.parseInt(str)));

        Log.d("socketHelperRecv1", String.format("%8s", binArray[0]).replace(' ', '0'));
        Log.d("socketHelperRecv2", String.format("%8s", binArray[1]).replace(' ', '0'));
//        Log.d("socketHelperRecv2", binArray[1]);

//        Log.d("socketHelperRecv1", String.format("%8s", Integer.toBinaryString(ans[0])).replace(' ', '0'));
//        Log.d("socketHelperRecv2", String.format("%8s", Integer.toBinaryString(ans[1])).replace(' ', '0'));

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        this.closeSocket();
    }

    @Override
    public String toString() {
        return "SocketHelper{" +
                "serverAddress='" + serverAddress + '\'' +
                ", port=" + port +
                ", socket=" + socket +
                ", input=" + input +
                ", out=" + out +
                '}';
    }
}
