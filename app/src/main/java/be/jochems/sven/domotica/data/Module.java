package be.jochems.sven.domotica.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sven on 2/10/16.
 */

public class Module {
    private byte address;
    private List<Output> outputs;

    public Module(byte address) {
        this.address = address;
        outputs = new ArrayList<>();
    }

    public byte getAddress() {
        return this.address;
    }

    public List<Output> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Output> outputs) {
        this.outputs = outputs;
    }

    public void addOutput(Output output){
        this.outputs.add(output);
    }

    public Output getOutputWithAddress(int address){
        for (Output output : outputs) {
            if (output.getAddress() == address) {
                return output;
            }
        }
        return null;
    }
}
