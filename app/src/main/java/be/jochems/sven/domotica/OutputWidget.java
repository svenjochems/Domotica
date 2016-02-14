package be.jochems.sven.domotica;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.RemoteViews;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link OutputWidgetConfigureActivity OutputWidgetConfigureActivity}
 */
public class OutputWidget extends AppWidgetProvider {
    public static String WIDGET_BUTTON = "android.appwidget.action.APPWIDGET_BUTTON";


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        //CharSequence widgetText = OutputWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        int module = OutputWidgetConfigureActivity.loadModulePref(context, appWidgetId);
        int address = OutputWidgetConfigureActivity.loadAddrPref(context, appWidgetId);
        String name = OutputWidgetConfigureActivity.loadNamePref(context, appWidgetId);



        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.output_widget);
        views.setTextViewText(R.id.appwidget_text, name);



        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.output_widget);
        Intent intent = new Intent(WIDGET_BUTTON);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
        //TODO: Fix widget onclick

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            OutputWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("Click", "Widget clicked");
        if (WIDGET_BUTTON.equals(intent.getAction())) {
            Log.d("Click", "Widget button clicked ");

        }
    }
}

