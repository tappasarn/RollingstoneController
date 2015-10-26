package xyz.rollingstone;

/**
 * Created by Ratthanan Aramboonpong NoppadolFC on 25/9/2558.
 * Action class is used to be an instance of an action which has id,direction,length
 * ex. 1,LEFT,2
 * actually i intended to add unit too, but I think it was a terrible idea. I added it at humanize anyway
 *
 * TL;DR - Debugging use toString(), displaying use humanize()
 */
public class Action {
    private int id;
    private String direction;
    private Integer length;

    public Action(Integer length, String direction) {
        this.length = length;
        this.direction = direction;
    }
    public Action(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "Action{" +
                "id=" + id +
                ", direction='" + direction + '\'' +
                ", length=" + length +
                '}';
    }
    public String humanize(){
        /*
            If it's LEFT or RIGHT, only direction is needed since the length is always zero
         */
        if (direction.equals("FORWARD") || direction.equals("BACK")) {
            return direction + " " + length + " m";
        } else if (direction.equals("LEFT") || direction.equals("RIGHT")) {
            return direction;
        } else {
            return "ERROR";
        }
    }
}
