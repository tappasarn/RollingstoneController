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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Created by Common Room on 10/1/2015.
 */

public class TelepathyToRobot extends AsyncTask<Integer, Void, Void> {
    private String serverAddress;
    private int port;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private CommandPacketReader commandPacketReader;
    private int[] availableId;
    private FragmentActivity fragmentActivity;
    public static final String TAG = "TelepathyToRobot.DEBUG";

    public TelepathyToRobot(FragmentActivity fa, String serverAddress, int port, int[] availableId) {
        this.fragmentActivity = fa;
        this.serverAddress = serverAddress;
        this.port = port;
        this.availableId = availableId;
    }

    public void openSocket() throws UnknownHostException, IOException {
        this.socket = new Socket(this.serverAddress, this.port);
    }

    public void send(int highByte, int lowByte) throws IOException, Exception {
        this.openSocket();
        this.outputStream = this.socket.getOutputStream();
        outputStream.write(highByte);
        outputStream.write(lowByte);
        outputStream.flush();

    }

    public void closeSocket() throws Exception {
        this.socket.close();
    }

    public byte[] receive() throws SocketTimeoutException, SocketException, IOException, TimeoutException, NullPointerException {
        byte[] ans = new byte[2];

        this.socket.setSoTimeout(1000);
        this.inputStream = this.socket.getInputStream();
        this.inputStream.read(ans, 0, 2);

        return ans;
    }

    @Override
    protected Void doInBackground(Integer... params) {

        try {
            this.send(params[0], params[1]);
            Log.d("socketHelperSend1", String.format("%8s", Integer.toBinaryString(params[0])).replace(' ', '0'));
            Log.d("socketHelperSend2", String.format("%8s", Integer.toBinaryString(params[1])).replace(' ', '0'));

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

        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.d(TAG, "Please check your ip");
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
            Log.d("TelepathyToRobot", "Trying to close the socket, the server is already closed");
        }
    }

    @Override
    public String toString() {
        return "TelepathyToRobot{" +
                "serverAddress='" + serverAddress + '\'' +
                ", port=" + port +
                ", socket=" + socket +
                ", input=" + inputStream +
                ", out=" + outputStream +
                '}';
    }

    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }
}
