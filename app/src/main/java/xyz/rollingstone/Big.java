package xyz.rollingstone;

/**
 * Created by Deeprom on 25/9/2558.
 */
public class Big {
    private int id;
    private String name;

    public Big() {

    }

    public Big(String name) {
        super();
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public int getId() {

        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Big{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
