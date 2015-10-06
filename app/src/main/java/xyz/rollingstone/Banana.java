package xyz.rollingstone;

/**
 * Created by Common Room on 10/6/2015.
 */
public class Banana {
    private int type;
    private boolean on;
    private int resolution;
    private int fromPipe;

    public Banana(int type, boolean on, int resolution) {
        this.type = type;
        this.on = on;
        this.resolution = resolution;
    }

    public Banana(byte fromPipe) {
        this.fromPipe = unsignedByteToInt(fromPipe);
    }

    public void squeeze(){
        this.type = (this.fromPipe & 0b1000_0000) == 0b1000_0000 ? 0:1;
        this.on = (this.fromPipe & 0b0010_0000) >> 5 == 0 ? false:true;
        this.resolution = (this.fromPipe & 0b000_11_000) >> 3;
    }

    public int fruit() {
        int word = this.type == 0 ? 0b1000_0000 : 0b0100_0000; // REQ-ACK
        word |= on ? 0b0010_0000 : 0b0000_0000; // in-line for loop if on then first statement else another
        word |= this.resolution << 3;
        return word;
    }

    @Override
    public String toString() {
        return "Banana [type=" + type + ", on=" + on + ", resolution=" + resolution + "]";
    }

    public static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

}
