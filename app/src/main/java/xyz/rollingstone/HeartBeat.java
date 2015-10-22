package xyz.rollingstone;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

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


    public static final String TAG = "AutoCommand.DEBUG";
    public static final String TAG2 = "HeartBeat.DEBUG";
    private List<int[]> packetList = new ArrayList<>();
    private TextView[] TVList = new TextView[5];

    public HeartBeat(String serverAddress, int port, int heartBeatPort, List<int[]> packetList) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.heartBeatPort = heartBeatPort;
        this.packetList = packetList;
    }

    public void setTVList(TextView a, TextView b, TextView c, TextView d, TextView e) {
        this.TVList[0] = a;
        this.TVList[1] = b;
        this.TVList[2] = c;
        this.TVList[3] = d;
        this.TVList[4] = e;
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

        this.heartBeatSocket.setSoTimeout(5*1000);
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
     *      send a command
     *      if ACK receive :
     *          continuously send HEARTBEAT until got DONE, ERR
     *      else :
     *          try to send a command again for 5 times
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
                    Log.d(TAG, String.format("%8s", Integer.toBinaryString(HiLo[0])).replace(' ', '0'));
                    Log.d(TAG, String.format("%8s", Integer.toBinaryString(HiLo[1])).replace(' ', '0'));

                    // get answer as raw byte then convert into int
                    byte[] ans;
                    ans = this.receive();

                    commandPacketReader = new CommandPacketReader(ans);
                    Log.d(TAG, String.format("%8s", Integer.toBinaryString(commandPacketReader.getHighByte())).replace(' ', '0'));
                    Log.d(TAG, String.format("%8s", Integer.toBinaryString(commandPacketReader.getLowByte())).replace(' ', '0'));

                    // just to remind
                    //	    10XXXXXX - REQ
                    //	    01XXXXXX - ACK
                    //      00XXXXXX - ERR
                    //      11XXXXXX - OK
                    if (commandPacketReader.getType() == 1) { // if get the type of ACK back
                        boolean getHeartBeatYet = false;

                        // loop sending HeartBeat every 5*1000 with unknown unit until get the ACK
                        // if the ack is received, exit this loop and continue with next command
                        while (!getHeartBeatYet) {
                            try {
                                this.sendHeartBeat(0b1000_0000);
                                Log.d(TAG2, "Heart Beat is sent");
                                byte[] heartBeatAnswer = this.receiveHeartBeat();

                                // just to convert byte to (unsigned)int
                                int heartBeatAnswerInteger = (int) heartBeatAnswer[0] & 0xFF;

                                Log.d(TAG2, String.format("%8s", Integer.toBinaryString(heartBeatAnswerInteger)).replace(' ', '0'));
                                if ((heartBeatAnswerInteger & 0b0100_0000) == 0b0100_0000) {
                                    Log.d(TAG2, "ACK from the robot");
                                    getHeartBeatYet = true;
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

                    // just delaying for my pleasure
                    Thread.sleep(1000);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.d(TAG, "try " + count + " out of " + maxTries + e.getMessage());
                } catch (ConnectException e) {
                    Log.d(TAG, "try " + count + " out of " + maxTries + e.getMessage());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Log.d(TAG, "try " + count + " out of " + maxTries + e.getMessage());
                } catch (Exception e) {
                    Log.d(TAG, "try " + count + " out of " + maxTries + e.getMessage());
                } finally {
                    if (++count == maxTries) {
                        Log.d(TAG, "Max tries exceeded");
                        tryNotExceed = false;
                    }
                }
            }
            if (!tryNotExceed) {
                break;
            }
        }
        Log.d(TAG, "DONE");
        return null;
    }
}
