package org.brandroid.iconcycle;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Implementation of App Widget functionality. App Widget Configuration
 * implemented in {@link IconCyclerConfigureActivity
 * IconCyclerConfigureActivity}
 */
public class IconCycler extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// There may be multiple widgets active, so update all of them
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// When the user deletes the widget, delete the preference associated
		// with it.
		final int N = appWidgetIds.length;
		for (int i = 0; i < N; i++) {
			IconCyclerConfigureActivity.deleteTitlePref(context,
                    appWidgetIds[i]);
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

    public static String BundleToString(Bundle b)
    {
        String ret = "{";
        for(String key : b.keySet())
            ret += (key != "{" ? "," : "") + key + ":" + b.get(key).toString();
        ret += "}";
        return ret;
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager man,
                                          int appWidgetId, Bundle b) {
        super.onAppWidgetOptionsChanged(context, man, appWidgetId, b);
        Log.v("IconCycle", "Widget Options Changed: " + BundleToString(b));
        boolean wide2 = b.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 90) > 196;
        boolean wide = b.getBoolean("wide", wide2);
        CycleWidgetFactory.mSelectedColor = context.getResources().getColor(android.R.color.primary_text_dark);
        CycleWidgetFactory.mSelectedColor = context.getResources().getColor(android.R.color.tertiary_text_dark);
        if(wide != wide2)
            updateAppWidget(context, man, appWidgetId);
    }

    public static class CycleWidgetService extends RemoteViewsService
    {
        @Override
        public RemoteViewsFactory onGetViewFactory(Intent intent) {
            return new CycleWidgetFactory(this.getApplicationContext(), intent);
        }
    }

    static class CycleWidgetFactory implements RemoteViewsService.RemoteViewsFactory
    {
        private final Context mContext;
        private final int mWidgetId;
        private File[] mFiles;
        private String mPath;
        private int mSelectedIndex = -1;
        public static int mSelectedColor = Color.BLACK;
        public static int mUnselectedColor = Color.DKGRAY;

        public CycleWidgetFactory(Context context, Intent intent)
        {
            mContext = context;
            mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            mPath = intent.getStringExtra("path");
            mSelectedIndex = intent.getIntExtra("sel", -1);
            mFiles = new File(mPath).listFiles(IconCyclerConfigureActivity.PNGFinder);
        }

        @Override
        public RemoteViews getViewAt(int i) {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);
            File f = mFiles[i];
            rv.setTextViewText(android.R.id.text1, f.getName());
            rv.setViewVisibility(android.R.id.icon, View.VISIBLE);
            try {
                rv.setImageViewUri(android.R.id.icon, Uri.parse(f.getAbsolutePath()));
            } catch(Exception e) {
            }
            Bundle b = new Bundle();
            b.putInt("pos", i);
            Intent fillin = new Intent();
            fillin.putExtras(b);
            rv.setOnClickFillInIntent(R.id.list_item, fillin);
            return rv;
        }

        @Override
        public void onCreate() {
            mFiles = new File(mPath).listFiles(IconCyclerConfigureActivity.PNGFinder);
        }

        @Override
        public void onDataSetChanged() {
            mFiles = new File(mPath).listFiles(IconCyclerConfigureActivity.PNGFinder);
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return mFiles.length;
        }

        @Override
        public RemoteViews getLoadingView() {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);
            return rv;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager man = AppWidgetManager.getInstance(context);
        if("switch".equals(intent.getAction())) {
            int w = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);
            int pos = intent.getIntExtra("pos", 0);
            //Toast.makeText(context, "New pos: " + pos, Toast.LENGTH_SHORT).show();
            Bundle b = man.getAppWidgetOptions(w);
            b.putInt("pos", pos);
            man.updateAppWidgetOptions(w, b);
            updateAppWidget(context, man, w);
        }
        super.onReceive(context, intent);
    }

    static void updateAppWidget(Context context,
			AppWidgetManager man, int appWidgetId) {

		final String path = IconCyclerConfigureActivity.loadTitlePref(
                context, appWidgetId);
        File dir = new File(path);

        if(!dir.exists()) return;

        Bundle b = man.getAppWidgetOptions(appWidgetId);
        int pos = b.getInt("pos", 0);

        File[] files = dir.listFiles(IconCyclerConfigureActivity.PNGFinder);
        if(files.length == 0) return;
        File file = files[pos % files.length];

        b.putInt("pos", pos);

        int layout = R.layout.icon_cycler;
        boolean wide = false;
        if(b.containsKey(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) &&
                b.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) > 196)
            wide = true;
        if(wide)
            layout = R.layout.widget_wide;
        b.putBoolean("wide", wide);
        man.updateAppWidgetOptions(appWidgetId, b);

		// Construct the RemoteViews object
		RemoteViews views = new RemoteViews(context.getPackageName(), layout);

        if(wide)
        {
            Intent intent = new Intent(context, CycleWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.putExtra("path", path);
            intent.putExtra("sel", pos);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            views.setRemoteAdapter(R.id.list_files, intent);
            Intent switchIntent = new Intent(context, IconCycler.class);
            switchIntent.setAction("switch");
            switchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            switchIntent.putExtra("path", path);
            switchIntent.setData(Uri.parse(switchIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent switchPending = PendingIntent.getBroadcast(context, 0, switchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.list_files, switchPending);
        }

		views.setTextViewText(R.id.appwidget_text, file.getName());;

        Bitmap bmp = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            bmp = new BitmapDrawable(context.getResources(), in).getBitmap();
        } catch(Exception e) {
        } finally {
            if(in != null)
                try {
                    in.close();
                } catch (IOException e) {
                }
        }
        views.setImageViewBitmap(R.id.appwidget_icon, bmp);

        Intent intent = new Intent(context, IconCyclerConfigureActivity.class);
        intent.setAction("cycle");
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra("widget", appWidgetId);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_frame, pi);

		// Instruct the widget manager to update the widget
		man.updateAppWidget(appWidgetId, views);
	}
}
