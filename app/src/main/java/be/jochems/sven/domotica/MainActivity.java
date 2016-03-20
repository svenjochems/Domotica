package be.jochems.sven.domotica;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Widgets
    private ListView    lstGroups;
    private ListView    lstOutputs;

    // Fields
    private ArrayAdapter<String> adpGroups;
    private ArrayAdapter<String> adpOutputs;

    private Domotica application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lstGroups = (ListView) findViewById(R.id.lstGroups);
        lstOutputs = (ListView) findViewById(R.id.lstOutputs);

        application = (Domotica)getApplicationContext();
        boolean data = application.loadData();

        if (data) {
            String[] groups = application.getGroups();
            String[] lstData  = Arrays.copyOf(groups, groups.length + 1);
            lstData[lstData.length - 1] = getString(R.string.lstMoods);

            adpGroups = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lstData);
            lstGroups.setAdapter(adpGroups);

            lstGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    lstGroups.setVisibility(View.GONE);
                    List<String> outputList = new ArrayList<>();

                    outputList.add(getString(R.string.lstBack));

                    // Load moods
                    if (((String) parent.getItemAtPosition(position)).equals(getString(R.string.lstMoods))){

                        String[] moods = application.getMoods();
                        for (int i = 0; i < moods.length; i++){
                            outputList.add(moods[i]);
                        }

                    // Load outputs
                    } else {

                        int[][] outputIndex = application.getOutputIndex();
                        String[][] outputs = application.getOutputs();

                        for (int i = 0; i < outputIndex.length; i++) {
                            for (int j = 0; j < outputIndex[i].length; j++) {
                                if (outputIndex[i][j] == position)
                                    outputList.add(outputs[i][j]);
                            }
                        }
                    }

                    adpOutputs = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, outputList);
                    lstOutputs.setAdapter(adpOutputs);
                    lstOutputs.setVisibility(View.VISIBLE);
                }
            });
        }

        lstOutputs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String data = (String) parent.getItemAtPosition(position);

                // Back button
                if (data.equals(getString(R.string.lstBack))) {
                    lstOutputs.setVisibility(View.GONE);
                    lstGroups.setVisibility(View.VISIBLE);

                // Moods
                    // TODO: data is not called moods but names in the moods
                    // TODO: Generalise loaded data
                } else if (data.equals(getString(R.string.lstMoods))) {
                    int address = -1;
                    String[] moods = application.getMoods();

                    for (int i = 0; i < moods.length; i++){
                        if (moods[i].equals(data)){
                            address = i;
                            break;
                        }
                    }
                    boolean test = application.toggleMood(address);

                // Groups
                } else {
                    int module = -1;
                    int address = -1;
                    String[][] outputs = application.getOutputs();

                    for (int i = 0; i < outputs.length; i++) {
                        for (int j = 0; j < outputs[i].length; j++) {
                            if (outputs[i][j].equals(data)) {
                                module = i + 1;
                                address = j;
                                break;
                            }
                        }
                    }
                    boolean test = application.toggleOutput(module, address);
                }
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        } else if (id == R.id.action_exit){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}
