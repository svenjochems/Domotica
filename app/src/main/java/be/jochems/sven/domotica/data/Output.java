package be.jochems.sven.domotica.data;

/**
 * Created by sven on 2/10/16.
 */

public class Output implements ActionInterface{
    private Group group;
    private byte address;
    private Module module;
    private String name;
    private int icon;
    private boolean status = false;

    public Output(Group group, byte address, Module module, String name, int icon) {
        this.group = group;
        this.address = address;
        this.module = module;
        this.name = name;
        this.icon = icon;
    }

    @Override
    public Group getGroup() {
        return this.group;
    }

    @Override
    public byte getAddress() {
        return this.address;
    }

    public Module getModule() {
        return this.module;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getIcon() {
        return this.icon;
    }

    @Override
    public boolean getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
