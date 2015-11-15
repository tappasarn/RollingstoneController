package xyz.rollingstone.tele;

import android.support.v4.app.FragmentActivity;
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

import xyz.rollingstone.packet.CommandPacketReader;

/**
 * Created by Common Room on 11/15/2015.
 */
public class TelepathyToRobotThread extends Thread {
    private String serverAddress;
    private int port;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private CommandPacketReader commandPacketReader;
    private int[] availableId;
    private FragmentActivity fragmentActivity;
    public static final String TAG = "Tele2Robot.DEBUG";
    private int[] params;

    public TelepathyToRobotThread(FragmentActivity fa, String serverAddress, int port, int[] availableId, int[] params) {
        Log.d(TAG, "TELE2ROBOT is Created !!");
        this.fragmentActivity = fa;
        this.serverAddress = serverAddress;
        this.port = port;
        this.availableId = availableId;
        this.params = params;
    }

    public void openSocket() throws UnknownHostException, IOException {
        this.socket = new Socket(this.serverAddress, this.port);
    }

    public void send(int highByte, int lowByte) throws IOException, Exception {
        Log.d(TAG, "Trying to open socket");
        this.openSocket();
        Log.d(TAG, "Trying to get outputStream");
        this.outputStream = this.socket.getOutputStream();
        Log.d(TAG, "Trying to socket & outputStream OK");
        outputStream.write(highByte);
        outputStream.write(lowByte);
        outputStream.flush();
        Log.d(TAG, "SEND&FLUSH");
    }

    public void closeSocket() throws Exception {
        this.socket.close();
    }

    public byte[] receive() throws SocketTimeoutException, SocketException, IOException, TimeoutException, NullPointerException {
        byte[] ans = new byte[2];
        Log.d(TAG, "Trying to receive");

        this.socket.setSoTimeout(1000);
        Log.d(TAG, "Trying to get InputStream");
        this.inputStream = this.socket.getInputStream();
        Log.d(TAG, "Trying to read2");
        this.inputStream.read(ans, 0, 2);

        return ans;
    }

    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }


    @Override
    public void run() {
        super.run();

        Log.d(TAG, "RATTHANAN IS HERE ");

        try {
            commandPacketReader = new CommandPacketReader(params);

            this.send(params[0], params[1]);
            Log.d(TAG + " S", commandPacketReader.toString());
            Log.d("Send-HiByte", String.format("%8s", Integer.toBinaryString(params[0])).replace(' ', '0'));
            Log.d("Send-LoByte", String.format("%8s", Integer.toBinaryString(params[1])).replace(' ', '0'));


            // get raw byte then convert into int
            byte[] ans = new byte[2];
            ans = this.receive();

            commandPacketReader = new CommandPacketReader(ans);
            if (commandPacketReader.getType() == 1){
                // clear the id
                this.availableId[commandPacketReader.getId()] = 0;
            }

            Log.d(TAG + " R", commandPacketReader.toString());
            Log.d("Receive-HiByte", String.format("%8s", Integer.toBinaryString(commandPacketReader.getHighByte())).replace(' ', '0'));
            Log.d("Receive-LoByte", String.format("%8s", Integer.toBinaryString(commandPacketReader.getLowByte())).replace(' ', '0'));

        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        } catch (ConnectException e) {
            Log.d(TAG, e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {

        }
    }
}
