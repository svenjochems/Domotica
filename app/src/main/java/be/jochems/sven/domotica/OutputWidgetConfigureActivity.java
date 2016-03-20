package be.jochems.sven.domotica;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * The configuration screen for the {@link OutputWidget OutputWidget} AppWidget.
 */
public class OutputWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "be.jochems.sven.domotica.OutputWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private ListView lstWidgetGroups;
    private ListView lstWidgetOutputs;
    private ArrayAdapter<String> adpGroups;
    private ArrayAdapter<String> adpOutputs;
    private Domotica application;

    public OutputWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveSettingsPref(Context context, int appWidgetId, int module, int address, String name) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + "MOD_" + appWidgetId, module);
        prefs.putInt(PREF_PREFIX_KEY + "ADD_" + appWidgetId, address);
        prefs.putString(PREF_PREFIX_KEY + "NAME_" + appWidgetId, name);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static int loadModulePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int module = prefs.getInt(PREF_PREFIX_KEY + "MOD_" + appWidgetId, -1);
        if (module != -1) {
            return module;
        } else {
            return 1;
        }
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static int loadAddrPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int address = prefs.getInt(PREF_PREFIX_KEY + "ADD_" + appWidgetId, -1);
        if (address != -1) {
            return address;
        } else {
            return 1;
        }
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadNamePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String name = prefs.getString(PREF_PREFIX_KEY + "NAME_" + appWidgetId, null);
        if (name != null) {
            return name;
        } else {
            return "error";
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.output_widget_configure);

        lstWidgetGroups = (ListView) findViewById(R.id.lstWidgetGroups);
        lstWidgetOutputs = (ListView) findViewById(R.id.lstWidgetOutputs);

        application = (Domotica)getApplicationContext();

        final String[] groups = application.getGroups();
        final String[][] outputs = application.getOutputs();
        final String[] moods = application.getMoods();
        final int[][] outputIndex = application.getOutputIndex();
        final int[][] outputIcon = application.getOutputIcon();


        adpGroups = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groups);
        lstWidgetGroups.setAdapter(adpGroups);

        lstWidgetGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lstWidgetGroups.setVisibility(View.GONE);
                List<String> outputList = new ArrayList<String>();

                outputList.add(getString(R.string.lstBack));

                for (int i = 0; i < outputIndex.length; i++) {
                    for (int j = 0; j < outputIndex[i].length; j++) {
                        if (outputIndex[i][j] == position)
                            outputList.add(outputs[i][j]);
                    }
                }

                adpOutputs = new ArrayAdapter<>(OutputWidgetConfigureActivity.this, android.R.layout.simple_list_item_1, outputList);
                lstWidgetOutputs.setAdapter(adpOutputs);
                lstWidgetOutputs.setVisibility(View.VISIBLE);

            }
        });

        lstWidgetOutputs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String data = (String) parent.getItemAtPosition(position);
                if (data.equals(getString(R.string.lstBack))) {
                    lstWidgetOutputs.setVisibility(View.GONE);
                    lstWidgetGroups.setVisibility(View.VISIBLE);
                } else {
                    int module = -1;
                    int address = -1;
                    for (int i = 0; i < outputs.length; i++) {
                        for (int j = 0; j < outputs[i].length; j++) {
                            if (outputs[i][j].equals(data)) {
                                module = i + 1;
                                address = j;
                            }
                        }
                    }
                    final Context context = OutputWidgetConfigureActivity.this;

                    // When the button is clicked, store the string locally
                    //String widgetText = mAppWidgetText.getText().toString();
                    //saveTitlePref(context, mAppWidgetId, widgetText);
                    saveSettingsPref(context,mAppWidgetId,module,address, data);

                    // It is the responsibility of the configuration activity to update the app widget
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    OutputWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

                    // Make sure we pass back the original appWidgetId
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    setResult(RESULT_OK, resultValue);
                    finish();
                }
            }
        });


        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        //mAppWidgetText.setText(loadTitlePref(OutputWidgetConfigureActivity.this, mAppWidgetId));
    }
}

