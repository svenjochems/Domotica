package be.jochems.sven.domotica.data;

import java.io.Serializable;

public class ActionIdentifier implements Serializable {
    private String name;
    private byte address;
    private byte module;

    public ActionIdentifier(String name, byte address, byte module) {
        this.name = name;
        this.address = address;
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public byte getAddress() {
        return address;
    }

    public byte getModule() {
        return module;
    }
}
