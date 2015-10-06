package xyz.rollingstone;

/**
 * Created by Common Room on 10/6/2015.
 */
public class Banana {
    private int type;
    private boolean on;
    private int resolution;

    public Banana(int type, boolean on, int resolution) {
        this.type = type;
        this.on = on;
        this.resolution = resolution;
    }

    public int fruit() {
        int word = this.type == 0 ? 0b1000_0000 : 0b0100_0000; // REQ-ACK
        word |= on ? 0b0010_0000 : 0b0000_0000; // in-line for loop if on then first statement else another
        word |= this.resolution << 3;
        return word;
    }



}
