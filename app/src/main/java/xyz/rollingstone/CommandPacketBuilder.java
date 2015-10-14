package xyz.rollingstone;

import android.util.Log;

import java.util.HashMap;

public class CommandPacketBuilder {

    private int type;
    private int id;
    private int command;
    private int value;

    public void setType(int type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCommand(String command) {
        switch (command) {
            case "COMMAND_NONE":
                this.command = 0;
                break;
            case "COMMAND_UP":
                this.command = 1;
                break;
            case "COMMAND_UPRIGHT":
                this.command = 2;
                break;
            case "COMMAND_RIGHT":
                this.command = 3;
                break;
            case "COMMAND_DOWNRIGHT":
                this.command = 4;
                break;
            case "COMMAND_DOWN":
                this.command = 5;
                break;
            case "COMMAND_DOWNLEFT":
                this.command = 6;
                break;
            case "COMMAND_LEFT":
                this.command = 7;
                break;
            case "COMMAND_UPLEFT":
                this.command = 8;
                break;
            default:
                System.out.println("SOMETHING WRONG");
        }
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public CommandPacketBuilder() {
        super();
    }

    public CommandPacketBuilder(Action action) {
        super();

        String direction = action.getDirection();
        switch (direction) {
            case "FORWARD":
                this.command = 1;
                break;
            case "BACKWARD":
                this.command = 5;
                break;
            case "LEFT":
                this.command = 7;
                break;
            case "RIGHT":
                this.command = 3;
                break;
            default:
                Log.d("CmdPacketBuilder.ERROR", "PLEASE CHECK THE ACTION YOU USED");
        }
        this.value = action.getLength();
    }

    public int[] Create() {

        int highByte = 0b0000_0000;
        int lowByte = 0b0000_0000;

//	    handle packet types
//	    10XXXXXX - REQ
//	    01XXXXXX - ACK
//      00XXXXXX - ERR
//      11XXXXXX - OK
        if (this.type == 0) {
            highByte |= 0b1000_0000;
        } else if (this.type == 1) {
            highByte |= 0b0100_0000;
        } else if (this.type == 2) {
            highByte |= 0b0000_0000;
        } else if (this.type == 3) {
            highByte |= 0b1100_0000;
        }

//		handle IDs
//      This is done by using shifting 4 position
        highByte |= this.id << 4;

//      handle Commands
//      Perform OR operation with command constant
        highByte |= this.command;

//      handle Values
//      Perform OR operation to set value
        lowByte |= this.value;

        int[] ret = new int[2];
        ret[0] = highByte;
        ret[1] = lowByte;

        return ret;
    }

}
