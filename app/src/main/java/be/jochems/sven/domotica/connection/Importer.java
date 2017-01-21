package be.jochems.sven.domotica.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import be.jochems.sven.domotica.R;
import be.jochems.sven.domotica.data.Group;
import be.jochems.sven.domotica.data.Module;
import be.jochems.sven.domotica.data.Mood;
import be.jochems.sven.domotica.data.Output;

/**
 * Created by sven on 2/10/16.
 */

public class Importer {
    private static final int MOODINDEX = 999;

    private SharedPreferences prefs;

    private List<Group>  groups;
    private List<Module> modules;

    public Importer(Context context, boolean noLocalData){
        prefs = context.getSharedPreferences("connection", Context.MODE_PRIVATE);

        groups  = new ArrayList<>();
        modules = new ArrayList<>();

        RawDate rawDate = loadData(context, noLocalData);
        convertDate(rawDate, context);
    }

    public Importer(Context context) {
        this(context, false);   // try data from shared prefs
    }

    public List<Group> getGroups() {
        return groups;
    }

    public List<Module> getModules() {
        return modules;
    }

    private Group getGroupWithIndex(int index){
        for (Group group : groups) {
            if (group.getIndex() == index) {
                return group;
            }
        }
        return null;
    }


    private void convertDate(RawDate rawDate, Context c) {
        createGroups(rawDate);
        createMoodGroup(c);
        createModules(rawDate);
        createMoods(rawDate);
    }

    private void createGroups(RawDate rawDate) {
        String[] groups = rawDate.getGroups();
        for (int i = 0; i < groups.length; i++) {
            Group g = new Group(groups[i]);
            g.setIndex(i);
            this.groups.add(g);
        }
    }

    private void createModules(RawDate rawDate) {
        for (int i = 0; i < rawDate.getOutputs().length; i++) {
            Module m = new Module((byte)(i+1));     // modules start with address 1
            m.setOutputs(createOutputs(rawDate, m));
            this.modules.add(m);
        }
    }

    private List<Output> createOutputs(RawDate rawDate, Module module) {
        List<Output> outputs = new ArrayList<>();

        int index = module.getAddress() - 1;    // modules start with address 1

        String[] outs = rawDate.getOutputs()[index];
        int[][] outputIndex = rawDate.getOutputIndex();
        int[][] outputIcon = rawDate.getOutputIcon();

        for (int i = 0; i < outs.length; i++) {
            Group group = getGroupWithIndex(outputIndex[index][i]);
            Output o = new Output(group, (byte)i, module, outs[i], outputIcon[index][i]);

            outputs.add(o);
            group.addItem(o);
        }
        return outputs;
    }

    private void createMoodGroup(Context c) {
        Group mood = new Group(c.getString(R.string.lstMoods));
        mood.setIndex(MOODINDEX);
        this.groups.add(mood);
    }

    private void createMoods(RawDate rawDate) {
        String[] moods = rawDate.getMoods();
        for (int i = 0; i < moods.length; i++) {
            Group moodGroup = getGroupWithIndex(MOODINDEX);
            Mood mood = new Mood(moodGroup, (byte)i, moods[i]);
            moodGroup.addItem(mood);
        }
    }



    private RawDate loadData(Context c, boolean noLocalData){
        RawDate rawDate;

        if(noLocalData || prefs.getString("groups",null) == null ||
                prefs.getString("outputs",null) == null ||
                prefs.getString("moods",null) == null ||
                prefs.getString("index",null) == null ||
                prefs.getString("icon",null) == null){

            rawDate = importData(c);
            saveData(rawDate);

        } else {
            String[]   groups      = loadStringArray("groups");
            String[][] outputs     = load2dStringArray("outputs");
            String[]   moods       = loadStringArray("moods");
            int[][]    outputIndex = load2dIntArray("index");
            int[][]    outputIcon  = load2dIntArray("icon");
            Log.i("Load Data", "Data available, loading");

            rawDate = new RawDate(groups, outputs, moods, outputIndex, outputIcon);
        }
        return rawDate;
    }

    private RawDate importData(Context c){
        Log.i("Load Data", "No data available, importing");
        Connection con = new Connection(c);
        con.importData();

        String[]   groups      = con.getGroups();
        String[][] outputs     = con.getOutputs();
        String[]   moods       = con.getMoods();
        int[][]    outputIndex = con.getOutputIndex();
        int[][]    outputIcon  = con.getOutputIcon();

        RawDate rawDate = new RawDate(groups, outputs, moods, outputIndex, outputIcon);

        return rawDate;
    }



    // Save loaded data to shared preferences
    private boolean saveData(RawDate rawDate){
        boolean a = saveStringArray(rawDate.getGroups(), "groups");
        boolean b = save2dStringArray(rawDate.getOutputs(), "outputs");
        boolean c = saveStringArray(rawDate.getMoods(), "moods");
        boolean d = save2dIntArray(rawDate.getOutputIndex(), "index");
        boolean e = save2dIntArray(rawDate.getOutputIcon(),"icon");

        Log.d("Save", "Saving imported data");

        return a && b && c && d && e;
    }

    private boolean saveStringArray(String[] array, String arrayName) {
        SharedPreferences.Editor editor = prefs.edit();
        try {
            JSONArray json = new JSONArray(array);
            editor.putString(arrayName, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return editor.commit();
    }

    private String[] loadStringArray(String arrayName) {
        String jsonString = prefs.getString(arrayName, null);
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            String[] array = new String[jsonArray.length()];
            for (int i = 0; i < array.length; i++)
                array[i] = jsonArray.getString(i);
            return array;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new String[]{};
    }

    private boolean save2dStringArray(String[][] array, String arrayName) {
        SharedPreferences.Editor editor = prefs.edit();
        try {
            JSONArray json = new JSONArray(array);
            editor.putString(arrayName, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return editor.commit();
    }

    private String[][] load2dStringArray(String arrayName) {
        String jsonString = prefs.getString(arrayName, null);
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            String[][] array = new String[jsonArray.length()][];
            for (int i = 0; i < array.length; i++){
                JSONArray sub = new JSONArray(jsonArray.getString(i));
                array[i] = new String[sub.length()];
                for (int j = 0; j < array[i].length; j++) {
                    array[i][j] = sub.getString(j);
                }
            }

            return array;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new String[][]{};
    }

    private boolean save2dIntArray(int[][] array, String arrayName) {
        SharedPreferences.Editor editor = prefs.edit();
        try {
            JSONArray json = new JSONArray(array);
            editor.putString(arrayName, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return editor.commit();
    }

    private int[][] load2dIntArray(String arrayName) {
        String jsonString = prefs.getString(arrayName, null);
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            int[][] array = new int[jsonArray.length()][];
            for (int i = 0; i < array.length; i++){
                JSONArray sub = new JSONArray(jsonArray.getString(i));
                array[i] = new int[sub.length()];
                for (int j = 0; j < array[i].length; j++) {
                    array[i][j] = Integer.parseInt(sub.getString(j));
                }
            }

            return array;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new int[][]{};
    }

    class RawDate {
        private String[]    groups;         // name with index
        private String[][]  outputs;        // name with index on module index
        private String[]    moods;          // name with index
        private int[][]     outputIndex;    // index from group
        private int[][]     outputIcon;     // icon index

        public RawDate(String[] groups, String[][] outputs, String[] moods, int[][] outputIndex, int[][] outputIcon) {
            this.groups         = groups;
            this.outputs        = outputs;
            this.moods          = moods;
            this.outputIndex    = outputIndex;
            this.outputIcon     = outputIcon;
        }

        public String[] getGroups() {
            return groups;
        }

        public void setGroups(String[] groups) {
            this.groups = groups;
        }

        public String[][] getOutputs() {
            return outputs;
        }

        public void setOutputs(String[][] outputs) {
            this.outputs = outputs;
        }

        public String[] getMoods() {
            return moods;
        }

        public void setMoods(String[] moods) {
            this.moods = moods;
        }

        public int[][] getOutputIndex() {
            return outputIndex;
        }

        public void setOutputIndex(int[][] outputIndex) {
            this.outputIndex = outputIndex;
        }

        public int[][] getOutputIcon() {
            return outputIcon;
        }

        public void setOutputIcon(int[][] outputIcon) {
            this.outputIcon = outputIcon;
        }
    }
}
