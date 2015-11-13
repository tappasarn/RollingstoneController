package xyz.rollingstone;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import xyz.rollingstone.packet.CommandPacketBuilder;
import xyz.rollingstone.packet.CommandPacketReader;

/**
 * HeartBeat now using only a byte
 * __  |___ ___|
 * REQ | OTHER |
 * ACK |
 * OK  |
 * ERR |
 * [--A BYTE--]
 */
public class HeartBeat extends AsyncTask<Void, Void, Void> {
    private String serverAddress;
    private int port;
    private int heartBeatPort;

    private Socket socket;
    private Socket heartBeatSocket;

    private OutputStream outputStream;
    private OutputStream heartBeatoutputStream;

    private InputStream inputStream;
    private InputStream heartBeatinputStream;

    private CommandPacketReader commandPacketReader;

    private Handler handler;

    public ResumeIndicator resumeIndicator;


    public static final String TAG = "AutoCommand.DEBUG";
    public static final String TAG2 = "HeartBeat.DEBUG";
    private List<int[]> packetList = new ArrayList<>();
    private TextView[] TVList = new TextView[5];

    public HeartBeat(String serverAddress, int port, int heartBeatPort, List<int[]> packetList, Handler handler, ResumeIndicator resumeIndicator) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.heartBeatPort = heartBeatPort;
        this.packetList = packetList;
        this.handler = handler;
        this.resumeIndicator = resumeIndicator;
    }

    public void openSocket() throws UnknownHostException, IOException {
        this.socket = new Socket(this.serverAddress, this.port);
    }

    public void openHeartBeatSocket() throws UnknownHostException, IOException {
        this.heartBeatSocket = new Socket(this.serverAddress, this.heartBeatPort);
    }

    public void closeSocket() throws Exception {
        this.socket.close();
    }

    public void closeHeartBeatSocket() throws Exception {
        this.heartBeatSocket.close();
    }

    public void sendCommand(int highByte, int lowByte) throws IOException, Exception {
        this.openSocket();
        this.outputStream = this.socket.getOutputStream();
        outputStream.write(highByte);
        outputStream.write(lowByte);
        outputStream.flush();
    }

    public void sendHeartBeat(int heartBeat) throws IOException, Exception {
        this.openHeartBeatSocket();
        this.heartBeatoutputStream = this.heartBeatSocket.getOutputStream();
        this.heartBeatoutputStream.write(heartBeat);
        this.heartBeatoutputStream.flush();
    }

    public byte[] receive() throws SocketTimeoutException, SocketException, IOException, TimeoutException, NullPointerException {

        byte[] ans = new byte[2];

        this.inputStream = this.socket.getInputStream();
        this.inputStream.read(ans, 0, 2);

        return ans;
    }

    public byte[] receiveHeartBeat() throws SocketTimeoutException, SocketException, IOException, TimeoutException, NullPointerException {

        byte[] ans = new byte[1];

        this.heartBeatSocket.setSoTimeout(5 * 100000);
        this.heartBeatinputStream = this.heartBeatSocket.getInputStream();
        this.heartBeatinputStream.read(ans, 0, 1);
        return ans;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    /**
     * for each command:
     * send a command
     * if ACK receive :
     * continuously send HEARTBEAT until got DONE, ERR
     * else :
     * try to send a command again for 5 times
     */
    @Override
    protected Void doInBackground(Void... avoid) {

        // loop through all commands
        for (int[] HiLo : packetList) {

            //initiate counter and its limit
            int count = 0;
            int maxTries = 5;
            boolean tryNotExceed = true;

            while (tryNotExceed) {
                try {

                    // send a command to the robot
                    this.sendCommand(HiLo[0], HiLo[1]);
                    commandPacketReader = new CommandPacketReader(HiLo);
                    Log.d(TAG, commandPacketReader.toString());
                    Log.d(TAG, "Send1 = " + String.format("%8s", Integer.toBinaryString(HiLo[0])).replace(' ', '0'));
                    Log.d(TAG, "Send2 = " + String.format("%8s", Integer.toBinaryString(HiLo[1])).replace(' ', '0'));
                    CommandPacketReader commandPacketReader1 = new CommandPacketReader(HiLo);

                    // get answer as raw byte then convert into int
                    byte[] ans;
                    ans = this.receive();

                    commandPacketReader = new CommandPacketReader(ans);
                    Log.d(TAG, commandPacketReader.toString());
                    Log.d(TAG, "Recv1 = " + String.format("%8s", Integer.toBinaryString(commandPacketReader.getHighByte())).replace(' ', '0'));
                    Log.d(TAG, "Recv2 = " + String.format("%8s", Integer.toBinaryString(commandPacketReader.getLowByte())).replace(' ', '0'));

                    if (commandPacketReader.getType() == 3) { // if get the type of ACK_A_TYPE
                        Log.d(TAG, "GOT ACK_A_TYPE from the robot");
                        boolean getHeartBeatYet = false;

                        // loop sending HeartBeat every 5*1000 with unknown unit until get the ACK
                        // if the ack is received, exit this loop and continue with next command
                        while (!getHeartBeatYet) {
                            try {
                                // send HEART_REQ
                                this.sendHeartBeat(0b0000_0000);
                                Log.d(TAG2, "Heart Beat is sent and waiting for answer");
                                byte[] heartBeatAnswer = this.receiveHeartBeat();

                                // just to convert byte to (unsigned)int
                                int heartBeatAnswerInteger = (int) heartBeatAnswer[0] & 0xFF;

                                Log.d(TAG2, "The HB answer " + String.format("%8s", Integer.toBinaryString(heartBeatAnswerInteger)).replace(' ', '0'));

                                // got HEART_ACK
                                if ((heartBeatAnswerInteger & 0b1100_0000) == 0b0100_0000) {
                                    Log.d(TAG2, "HEART_ACK received");

                                // HEART_OK, it means that the action is done.
                                // so we continue with the next action
                                } else if ((heartBeatAnswerInteger & 0b1100_0000) == 0b1000_0000) {
                                    Log.d(TAG2, "HEART_OK from the robot");
                                    getHeartBeatYet = true;
                                    //Do this to break the tryNotExceed Loop and send the next command
                                    tryNotExceed = false;

                                    Message msg = Message.obtain();
                                    Bundle b = new Bundle();
                                    b.putSerializable("status", "OK");
                                    msg.setData(b);
                                    handler.sendMessage(msg);

                                // got HEART_ERR, the robot stops.
                                } else if ((heartBeatAnswerInteger & 0b1100_0000) == 0b1100_0000) {
                                    Log.d(TAG2, "HEART_ERR from the robot");

                                    Message msg = Message.obtain();
                                    Bundle b = new Bundle();
                                    b.putSerializable("status", "ERR");
                                    msg.setData(b);
                                    handler.sendMessage(msg);

                                    while (resumeIndicator.getInt() != 5){

                                    }

                                    getHeartBeatYet = true;
                                    // Do this to break the tryNotExceed Loop and send the next command
                                    tryNotExceed = false;

                                    // send OK to the AutoTab so that it will call scriptSlider and display the correct action
                                    msg = Message.obtain();
                                    b = new Bundle();
                                    b.putSerializable("status", "OK");
                                    msg.setData(b);
                                    handler.sendMessage(msg);
                                } else {
                                    Log.d(TAG2, "GOT STH strange from HeartBeat " + String.format("%8s", Integer.toBinaryString(heartBeatAnswerInteger)).replace(' ', '0'));
                                }


                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                                Log.d(TAG2, e.getMessage());
                            } catch (ConnectException e) {
                                Log.d(TAG2, e.getMessage());
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                Log.d(TAG2, e.getMessage());
                            } catch (Exception e) {
                                Log.d(TAG2, e.getMessage());
                            }
                        }
                    } else {
                        Log.d(TAG, "error occurred");
                    }
                    Thread.sleep(3000);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.d(TAG, "try " + count + " out of " + maxTries + e.getMessage());
                } catch (ConnectException e) {
                    Log.d(TAG, "try " + count + " out of " + maxTries + e.getMessage());
                } catch (Exception e) {
                    Log.d(TAG, "try " + count + " out of " + maxTries + e.getMessage());
                } finally {
                    if (++count >= maxTries) {
                        Log.d(TAG, "Max tries exceeded");
                        tryNotExceed = false;

                        Message msg = Message.obtain();
                        Bundle b = new Bundle();
                        b.putSerializable("status", "CNNERR");
                        msg.setData(b);
                        handler.sendMessage(msg);

                        return null;
                    }
                }

                //END tryNotExceed
            }
        }
        Message msg = Message.obtain();
        Bundle b = new Bundle();
        b.putSerializable("status", "DONE");
        msg.setData(b);
        handler.sendMessage(msg);
        Log.d(TAG, "DONE");
        return null;
    }
}
