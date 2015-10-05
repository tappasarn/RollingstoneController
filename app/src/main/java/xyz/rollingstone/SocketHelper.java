package xyz.rollingstone;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Common Room on 10/1/2015.
 */

public class SocketHelper extends AsyncTask<Integer, Void, Void> {
    private String serverAddress;
    private int port;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter out;


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
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            out.write(highByte);
            out.write(lowByte);
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

    public String receive() {
//        this.openSocket();
        String answer = "0";

        try {
            this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            answer = this.input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return answer;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Integer... params) {
        Log.d("socketHelperSend", String.format("%16s", Integer.toBinaryString(params[0])).replace(' ', '0'));
        this.send(params[0], params[1]);

        String answer = this.receive();
        Log.d("socketHelperReceive", String.format("%16s", Integer.toBinaryString(Integer.parseInt(answer))).replace(' ', '0'));
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
