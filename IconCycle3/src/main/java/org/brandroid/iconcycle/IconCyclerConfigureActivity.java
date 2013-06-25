package org.brandroid.iconcycle;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The configuration screen for the {@link IconCycler IconCycler} AppWidget.
 */
public class IconCyclerConfigureActivity extends Activity {

	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	EditText mAppWidgetText;
	public static final String PREFS_NAME = "org.brandroid.iconcycle.IconCycler";
	public static final String PREF_PREFIX_KEY = "appwidget_";
    public static final FileFilter PNGFinder = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.getName().toLowerCase().endsWith(".png");
        }
    };

	public IconCyclerConfigureActivity() {
		super();
	}

    public ResolveInfo[] getResolves(Intent intent)
    {
        return getPackageManager().queryIntentActivities(intent, 0).toArray(new ResolveInfo[0]);
    }

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

        if("cycle".equals(getIntent().getAction()))
        {
            cycleWidgets();
            finish();
            return;
        }

		// Set the result to CANCELED. This will cause the widget host to cancel
		// out of the widget placement if the user presses the back button.
		setResult(RESULT_CANCELED);

		setContentView(R.layout.icon_cycler_configure);
		mAppWidgetText = (EditText) findViewById(R.id.appwidget_text);
        Intent pintent = new Intent(Intent.ACTION_PICK);
        pintent.setType("folder");
        pintent.putExtra("path", mAppWidgetText.getText().toString());
        pintent.setData(Uri.parse("file://" + mAppWidgetText.getText().toString()));
        if(getResolves(pintent).length == 0)
            findViewById(android.R.id.button1).setVisibility(View.GONE);
        else {
            findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("folder");
                    intent.putExtra("path", mAppWidgetText.getText().toString());
                    intent.setData(Uri.parse("file://" + mAppWidgetText.getText().toString()));
                    if(getResolves(intent).length > 0)
                        startActivityForResult(intent, 1);
                    else
                        Toast.makeText(view.getContext(), "No intents.", Toast.LENGTH_SHORT).show();
                }
            });
            findViewById(android.R.id.button1).setVisibility(View.VISIBLE);
        }
		findViewById(R.id.add_button).setOnClickListener(mOnClickListener);
        findViewById(R.id.test_button).setOnClickListener(mOnClickListener);

		// Find the widget id from the intent.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If this activity was started with an intent without an app widget ID,
		// finish with an error.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            cycleWidgets();
            finish();
			return;
		}

		mAppWidgetText.setText(loadTitlePref(IconCyclerConfigureActivity.this,
				mAppWidgetId));


	}

    public void cycleWidgets()
    {
        AppWidgetManager man = AppWidgetManager.getInstance(this);
        ComponentName comp = new ComponentName(this, IconCycler.class);
        int[] widgets = man.getAppWidgetIds(comp);
//            if(extras != null && extras.containsKey("widget") && Arrays.binarySearch(widgets, extras.getInt("widget")) > -1)
//            {
//                IconCycler.updateAppWidget(this, man, extras.getInt("widget"));
//            } else {
        for(int w : widgets)
        {
            Bundle b = man.getAppWidgetOptions(w);
            b.putInt("pos", b.getInt("pos") + 1);
            man.updateAppWidgetOptions(w, b);
            IconCycler.updateAppWidget(this, man, w);
        }
//            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK)
        {
            String path = data.getDataString();
            if(!path.startsWith("file://"))
                path = "file://" + path;
            URI uri = null;
            try {
                uri = new URI(path);
                File f = new File(uri);
                if(!f.exists())
                    return;
                if(!f.isDirectory())
                    f = f.getParentFile();
                mAppWidgetText.setText(f.getPath());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private class IconPreviewAdapter extends BaseAdapter
    {
        private File[] kids;

        IconPreviewAdapter(String path)
        {
            kids = new File(path).listFiles(PNGFinder);
        }

        @Override
        public File getItem(int i) {
            return kids[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public int getCount() {
            return kids.length;
        }

        @Override
        public View getView(int i, View old, ViewGroup viewGroup) {
            View ret = old;
            Context ctx = viewGroup.getContext();
            if(ret == null)
                ret = LayoutInflater.from(ctx)
                        .inflate(R.layout.icon_cycler, null);
            File f = getItem(i);
            if(f == null) return ret;
            FileInputStream in = null;
            try {
                ((TextView)ret.findViewById(R.id.appwidget_text)).setText(f.getName());
                in = new FileInputStream(f);
                ((ImageView)ret.findViewById(R.id.appwidget_icon)).setImageBitmap(
                        new BitmapDrawable(getResources(), in).getBitmap());
            } catch(Exception e) {

            } finally {
                if(in != null)
                    try {
                        in.close();
                    } catch (IOException e) {

                    }
            }
            return ret;
        }
    }

    private void testPath()
    {
        TextView tvStatus = (TextView)findViewById(R.id.text_status);
        GridView grid = (GridView)findViewById(R.id.grid_preview);
        grid.setAdapter(null);
        String path = mAppWidgetText.getText().toString();
        tvStatus.setText("Testing " + path + "... ");
        try {
            File f = new File(path);
            if(!f.exists())
            {
                tvStatus.append("Invalid path!");
                return;
            } else if(!f.isDirectory())
            {
                tvStatus.append("Not a directory!");
                return;
            } else tvStatus.append("Valid directory. ");
            File[] kids = f.listFiles(PNGFinder);
            if(kids.length == 0)
            {
                tvStatus.append("No PNG files!");
                return;
            } else {
                tvStatus.append(kids.length + " icons found!");
            }
            grid.setAdapter(new IconPreviewAdapter(path));
        } catch(Exception e) {
            tvStatus.setText("Error!\n" + e.toString());
        }
    }

	View.OnClickListener mOnClickListener = new View.OnClickListener() {
		public void onClick(View v) {
            if(v.getId() == R.id.test_button)
            {
                testPath();
                return;
            }
			final Context context = IconCyclerConfigureActivity.this;

			// When the button is clicked, store the string locally
			String widgetText = mAppWidgetText.getText().toString();
			saveTitlePref(context, mAppWidgetId, widgetText);

			// It is the responsibility of the configuration activity to update
			// the app widget
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			IconCycler.updateAppWidget(context, appWidgetManager, mAppWidgetId);

			// Make sure we pass back the original appWidgetId
			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
		}
	};

	// Write the prefix to the SharedPreferences object for this widget
	static void saveTitlePref(Context context, int appWidgetId, String text) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
				PREFS_NAME, 0).edit();
		prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
		prefs.commit();
	}

	// Read the prefix from the SharedPreferences object for this widget.
	// If there is no preference saved, get the default from a resource
	static String loadTitlePref(Context context, int appWidgetId) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
		String titleValue = prefs
				.getString(PREF_PREFIX_KEY + appWidgetId, null);
		if (titleValue != null) {
			return titleValue;
		} else {
			return context.getString(R.string.appwidget_text);
		}
	}

	static void deleteTitlePref(Context context, int appWidgetId) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(
				PREFS_NAME, 0).edit();
		prefs.remove(PREF_PREFIX_KEY + appWidgetId);
		prefs.commit();
	}
}
