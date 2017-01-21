package be.jochems.sven.domotica;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import be.jochems.sven.domotica.connection.Connection;
import be.jochems.sven.domotica.connection.Importer;
import be.jochems.sven.domotica.data.ActionInterface;
import be.jochems.sven.domotica.data.Group;
import be.jochems.sven.domotica.data.Module;
import be.jochems.sven.domotica.data.Output;

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
        importData();
    }

    protected Domotica getInstance(){
        return this;
    }


    public List<Group> getMgroups() {
        if (mgroups == null)
            importData();
        return mgroups;
    }

    public List<Module> getModules() {
        if (modules == null)
            importData();
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
