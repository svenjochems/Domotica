package be.jochems.sven.domotica.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.jochems.sven.domotica.connection.ActionHelper;
import be.jochems.sven.domotica.connection.Connection;
import be.jochems.sven.domotica.Domotica;
import be.jochems.sven.domotica.R;
import be.jochems.sven.domotica.data.ActionInterface;
import be.jochems.sven.domotica.data.Group;
import be.jochems.sven.domotica.data.Mood;

public class MainActivity extends AppCompatActivity {

    // Widgets
    private ListView    lstGroups;
    private ListView    lstOutputs;

    // Fields
    private ArrayAdapter<Group> adpGroups;
    private OutputListAdapter adpOutputs;

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

        final List<Group> groups = application.getMgroups();

        adpGroups = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groups);
        lstGroups.setAdapter(adpGroups);

        // load list with moods or outputs
        lstGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lstGroups.setVisibility(View.GONE);
                ActionHelper.updateStatus(groups, getApplicationContext());
                ArrayList<ActionInterface> items = new ArrayList<>();
                items.addAll(groups.get(position).getItems());
                adpOutputs = new OutputListAdapter(MainActivity.this, R.layout.list_outputs, items);
                lstOutputs.setAdapter(adpOutputs);
                adpOutputs.notifyDataSetChanged();      // TODO: test notify instead of rebuild array
                lstOutputs.setVisibility(View.VISIBLE);
            }
        });

        // perform action on list
        lstOutputs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ActionInterface item = (ActionInterface) parent.getItemAtPosition(position);

                boolean toggleTest = ActionHelper.toggleAction(item, getApplicationContext());
                boolean statusTest = ActionHelper.updateStatus(groups, getApplicationContext());

                // update list with new statusses
                if (toggleTest && statusTest) {
                    ArrayList<ActionInterface> items = new ArrayList<>();
                    items.addAll(item.getGroup().getItems());
                    adpOutputs = new OutputListAdapter(MainActivity.this, R.layout.list_outputs, items);
                    lstOutputs.setAdapter(adpOutputs);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        int visibility = lstOutputs.getVisibility();
        if (visibility == View.VISIBLE){
            lstOutputs.setVisibility(View.GONE);
            lstGroups.setVisibility(View.VISIBLE);
        }
        else{
            super.onBackPressed();
        }
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
