package xyz.rollingstone;

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

	public void read(){
//      handle Packet types
//      Perform AND operation with received high-byte
//      and check for equality
		if ((this.highByte & 0b1000_0000) == 0b1000_0000) {
			this.type = 0;
		} else if ((this.highByte & 0b0100_0000) == 0b0100_0000) {
			this.type = 1;
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
}
