package xyz.rollingstone.packet;

public class CommandPacketReader {

    private int highByte;
    private int lowByte;
    private int type;
    private int id;
    private int command;
    private int value;

    public CommandPacketReader(int[] packet) {
        this.highByte = packet[0];
        this.lowByte = packet[1];

        this.read();
    }

    public CommandPacketReader(Integer[] packet){
        this.highByte = packet[0];
        this.lowByte = packet[1];

        this.read();
    }
    public CommandPacketReader(byte[] fromPipe) {
        this.highByte = unsignedByteToInt(fromPipe[0]);
        this.lowByte = unsignedByteToInt(fromPipe[1]);

        this.read();
    }

    public int getHighByte() {
        return highByte;
    }

    public int getLowByte() {
        return lowByte;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public int getCommand() {
        return command;
    }

    public int getValue() {
        return value;
    }

    public void read() {
//      handle Packet types
//      Perform AND operation with received high-byte
//      and check for equality
        if ((this.highByte & 0b1100_0000) == 0b0000_0000) {
            this.type = 0;
        }
        if ((this.highByte & 0b1100_0000) == 0b0100_0000) {
            this.type = 1;
        } else if ((this.highByte & 0b1100_0000) == 0b1000_0000) {
            this.type = 2;
        } else if ((this.highByte & 0b1100_0000) == 0b1100_0000) {
            this.type = 3;
        }

//      handle IDs
//      Mask other bits except id bits and perform 4 shift right
        this.id = (this.highByte & 0b0011_0000) >> 4;

//      handle Commands
//      Mask other bits except last 4 LSB bits
        this.command = this.highByte & 0b0000_1111;

//      handle Value
//      Copy value over
        this.value = this.lowByte;

    }

    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    @Override
    public String toString() {
        String TYPE = "";
        String COMM = "";

        switch (type) {
            case 0:
                TYPE = "REQ_M_TYPE";
                break;
            case 1:
                TYPE = "ACK_M_TYPE";
                break;
            case 2:
                TYPE = "REQ_A_TYPE";
                break;
            case 3:
                TYPE = "ACK_A_TYPE";
                break;
        }

        switch (command) {
            case 0:
                COMM = "STICK_NONE";
                break;
            case 1:
                COMM = "STICK_UP";
                break;
            case 2:
                COMM = "STICK_UPRIGHT";
                break;
            case 3:
                COMM = "STICK_RIGHT";
                break;
            case 4:
                COMM = "STICK_DOWNRIGHT";
                break;
            case 5:
                COMM = "STICK_DOWN";
                break;
            case 6:
                COMM = "STICK_DOWNLEFT";
                break;
            case 7:
                COMM = "STICK_LEFT";
                break;
            case 8:
                COMM = "STICK_UPLEFT";
                break;
        }

        return TYPE + " " + COMM + " val: " + value;
    }
}
