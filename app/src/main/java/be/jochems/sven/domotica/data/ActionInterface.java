package be.jochems.sven.domotica.data;

import java.io.Serializable;

/**
 * Created by sven on 2/10/16.
 */

public interface ActionInterface {

    byte getAddress();

    Group getGroup();

    int getIcon();

    String getName();

    boolean getStatus();

    void setStatus(boolean status);

    ActionIdentifier getIdentifier();
}
