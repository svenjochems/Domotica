package be.jochems.sven.domotica.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sven on 2/10/16.
 */

public class Group {
    private String name;
    private int index;
    private List<ActionInterface> items;

    public Group() {
        this.items = new ArrayList<>();
    }

    public Group(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ActionInterface> getItems() {
        return items;
    }

    public void addItem(ActionInterface item){
        items.add(item);
    }

    public void setItems(List<ActionInterface> items) {
        this.items = items;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return name;
    }
}
