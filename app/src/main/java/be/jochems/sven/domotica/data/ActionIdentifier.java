package be.jochems.sven.domotica.data;

import java.io.Serializable;

/**
 * This identifier contains all necessary information to perform actions
 */

public class ActionIdentifier implements Serializable {
    private String name;
    private byte address;
    private byte module;

    public ActionIdentifier(String name, byte address, byte module) {
        this.name = name;
        this.address = address;
        this.module = module;
    }

    public static ActionIdentifier fromString(String data) throws Exception {
        String[] split = data.split("_");
        if (split.length != 3) {
            throw new Exception("Invalid string data");
        }
        String name = split[0];
        byte address = new Byte(split[1]);
        byte module = new Byte(split[2]);
        return new ActionIdentifier(name, address, module);
    }

    public String toString() {
        return this.name + "_" + this.address + "_" + this.module;
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
