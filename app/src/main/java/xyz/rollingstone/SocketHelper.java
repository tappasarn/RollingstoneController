package xyz.rollingstone;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
    private CommandPacketReader commandPacketReader;
    private int[] availableId;
    private FragmentActivity fragmentActivity;


    public SocketHelper(FragmentActivity fa, String serverAddress, int port, int[] availableId) {
        this.fragmentActivity = fa;
        this.serverAddress = serverAddress;
        this.port = port;
        this.availableId = availableId;
    }

    public void openSocket() {
        try {
            this.socket = new Socket(this.serverAddress, this.port);
        } catch (Exception e) {
            e.printStackTrace();
            this.fragmentActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(fragmentActivity, "The server is closed", Toast.LENGTH_SHORT).show();
                }
            });
            Log.d("Opensocket", "mayb the server is closed");
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
        } catch (Exception e) {
            e.printStackTrace();
            this.fragmentActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(fragmentActivity, "The server is closed", Toast.LENGTH_SHORT).show();
                }
            });
            Log.d("SendSock", "mayb the server is closed");
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
            this.socket.setSoTimeout(1000);
            this.inputStream = this.socket.getInputStream();
            this.inputStream.read(ans, 0, 2);

        } catch (Exception e) {
            e.printStackTrace();
            this.fragmentActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(fragmentActivity, "Time out", Toast.LENGTH_SHORT).show();
                }
            });
            Log.d("receiveSock", "Timeout");
            this.availableId[commandPacketReader.getId()] = 0;
        }
        return ans;
    }

    @Override
    protected Void doInBackground(Integer... params) {

        Log.d("socketHelperSend1", String.format("%8s", Integer.toBinaryString(params[0])).replace(' ', '0'));
        Log.d("socketHelperSend2", String.format("%8s", Integer.toBinaryString(params[1])).replace(' ', '0'));
        this.send(params[0], params[1]);

        byte[] ans = new byte[2];
        ans = this.receive();

        int[] intArray = new int[2];
        for (int index = 0; index < ans.length; index++) {
            intArray[index] = unsignedByteToInt(ans[index]);
        }

        commandPacketReader = new CommandPacketReader(intArray);

        this.availableId[commandPacketReader.getId()] = 0;

        Log.d("socketHelperRecv1", String.format("%8s", Integer.toBinaryString(commandPacketReader.getHighByte())).replace(' ', '0'));
        Log.d("socketHelperRecv2", String.format("%8s", Integer.toBinaryString(commandPacketReader.getLowByte())).replace(' ', '0'));

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
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

    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }
}
