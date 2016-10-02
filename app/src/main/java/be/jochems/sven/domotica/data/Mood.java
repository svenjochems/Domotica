package be.jochems.sven.domotica.data;

/**
 * Created by sven on 2/10/16.
 */

public class Mood implements ActionInterface {
    private Group group;
    private byte address;
    private boolean status = false;
    private String name;

    public Mood(Group group, byte address, String name) {
        this.group = group;
        this.address = address;
        this.name = name;
    }

    @Override
    public byte getAddress() {
        return this.address;
    }

    @Override
    public Group getGroup() {
        return this.group;
    }

    @Override
    public int getIcon() {
        return 999;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean getStatus() {
        return false;
    }

    @Override
    public void setStatus(boolean status) {

    }
}
