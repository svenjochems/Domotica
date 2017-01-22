package be.jochems.sven.domotica;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;

import be.jochems.sven.domotica.connection.Connection;
import be.jochems.sven.domotica.connection.Importer;
import be.jochems.sven.domotica.data.Group;
import be.jochems.sven.domotica.data.Module;
import be.jochems.sven.domotica.view.OnTaskComplete;

/**
 * Created by sven on 20/03/16.
 */
public class Domotica extends Application {

    private List<Group> mgroups;
    private List<Module> modules;

    private Context context;
    private SharedPreferences prefs;


    @Override
    public void onCreate() {
        context = getApplicationContext();
        prefs = context.getSharedPreferences("connection", MODE_PRIVATE);
        super.onCreate();
    }

    protected Domotica getInstance(){
        return this;
    }

    public boolean init(OnTaskComplete listener) {
        Connection connection = new Connection();
        try {
            connection.openConnection(listener);
            importData();
            return true;
        } catch (Exception e) {
            Log.e("Application", "No connection found, nothing more we can do now");
        }
        return false;
    }

    public List<Group> getMgroups() {
        return mgroups;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void importData() {
        retreiveData(false);
    }

    public void retreiveData(boolean forceRetreive) {
        Importer dataImporter = new Importer(context, forceRetreive);
        mgroups = dataImporter.getGroups();
        modules = dataImporter.getModules();
    }
}
