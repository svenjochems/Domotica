package be.jochems.sven.domotica.connection;

import android.content.Context;

import java.util.List;

import be.jochems.sven.domotica.data.ActionInterface;
import be.jochems.sven.domotica.data.Group;
import be.jochems.sven.domotica.data.Output;

/**
 * Created by sven on 21/01/17.
 */

public class ActionHelper {

    public static boolean updateStatus(List<Group> groups, Context context) {
        Connection con = new Connection(context);
        byte[][] status = con.getStatus();
        for (Group group : groups) {
            for (ActionInterface item : group.getItems()) {
                if (item instanceof Output) {
                    Output output = (Output) item;
                    int moduleIndex = output.getModule().getAddress() - 1;
                    int outputIndex = output.getAddress();
                    boolean st = status[moduleIndex][outputIndex] == 1;
                    item.setStatus(st);
                }
            }
        }
        return true;
    }

    public static boolean toggleAction(ActionInterface item, Context context) {
        Connection con = new Connection(context);

        if (item instanceof Output)
            return con.toggleOutput(((Output) item).getModule().getAddress(), item.getAddress());
        else
            return con.toggleMood(item.getAddress());
    }
}
