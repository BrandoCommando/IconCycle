package org.brandroid.iconcycle;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private ListView mList;
    private BaseAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        mAdapter = new WidgetListAdapter(this, new ComponentName(this, IconCycler.class),
                AppWidgetManager.getInstance(this));
        if(mAdapter.getCount() == 0)
            return;
        ((TextView)findViewById(R.id.text_message)).setText("Please select widget to configure:");
        mList = (ListView)findViewById(R.id.list_widgets);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int w = ((WidgetListAdapter)mAdapter).getItem(i);
                Intent intent = new Intent(MainActivity.this, IconCyclerConfigureActivity.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, w);
                startActivityForResult(intent, 2);
            }
        });
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2 && resultCode == RESULT_OK)
            mAdapter.notifyDataSetChanged();
    }

    private class WidgetListAdapter extends BaseAdapter
    {
        final ComponentName mComponent;
        final AppWidgetManager mManager;
        private int[] mWidgetIDs;

        public WidgetListAdapter(Context c, ComponentName comp, AppWidgetManager man)
        {
            mComponent = comp;
            mManager = man;
            notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetChanged() {
            final ArrayList<Integer> widgets = new ArrayList<Integer>();
            for(int w : mManager.getAppWidgetIds(mComponent))
            {
                File f = new File(IconCyclerConfigureActivity.loadTitlePref(MainActivity.this, w));
                if(f == null || !f.exists() || !f.isDirectory())
                    continue;
                widgets.add(w);
            }
            this.mWidgetIDs = new int[widgets.size()];
            for(int i = 0; i < mWidgetIDs.length; i++)
                mWidgetIDs[i] = widgets.get(i);
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mWidgetIDs.length;
        }

        @Override
        public Integer getItem(int i) {
            return mWidgetIDs[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.widget_list_item, null);
            TextView tv = (TextView)view.findViewById(android.R.id.text1);
            int w = mWidgetIDs[i];
            String path = IconCyclerConfigureActivity.loadTitlePref(MainActivity.this, w);
            int pos = AppWidgetManager.getInstance(MainActivity.this).getAppWidgetOptions(w)
                    .getInt("pos", 0);
            Bitmap bmp = null;
            InputStream in = null;
            try {
                File[] files = new File(path).listFiles(IconCyclerConfigureActivity.PNGFinder);
                File file = files[pos % files.length];
                tv.setText(path + " (" + files.length + " files)");
                in = new FileInputStream(file);
                bmp = new BitmapDrawable(getResources(), in).getBitmap();
            } catch(Exception e) {

            } finally {
                if(in != null)
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            ((ImageView)view.findViewById(android.R.id.icon)).setImageBitmap(bmp);
            return view;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

}
