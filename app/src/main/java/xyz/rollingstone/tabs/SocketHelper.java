package xyz.rollingstone.tabs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Common Room on 10/1/2015.
 */
public class SocketHelper {
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

    public void send(int input) {
        this.openSocket();
        try {
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            out.println(input);
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String receive(int input) {
        this.openSocket();
        String answer = "none";
        try {
            this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            answer = this.input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return answer;
    }

}
