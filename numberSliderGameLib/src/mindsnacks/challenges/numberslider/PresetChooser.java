package com.threeDBJ.numberSlider;

import android.app.Activity;
import android.os.Bundle;

import android.graphics.Typeface;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.ArrayList;

import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class PresetChooser extends SherlockActivity {

    static final int BROWSE_PHONE=1, CANCEL=2;

    static String[] labels = new String[] { "Default", "Kitten", "Puppy", "Koala", "Swans", "Mona Lisa" };
    static int[] drawables = new int[] { 0, R.drawable.kitten, R.drawable.puppy, R.drawable.koala,
					 R.drawable.swans, R.drawable.mona_lisa };

    ListView listView;
    TextView label;
    ArrayAdapter<String> list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	ActionBar bar = getSupportActionBar();
	bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	bar.setDisplayShowHomeEnabled(false);
	bar.setDisplayShowTitleEnabled(false);

	setContentView(R.layout.preset);

	list = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
					android.R.id.text1, labels);
	setupUI();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	menu.add(0, BROWSE_PHONE, 0, "Browse Phone")
	    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	menu.add(0, CANCEL, 0, "Cancel")
	    .setIcon(R.drawable.ic_menu_close_clear_cancel)
	    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	Intent intent;
	switch (item.getItemId()) {
	case BROWSE_PHONE:
	    intent = new Intent(PresetChooser.this, DirectoryBrowser.class);
	    intent.putExtra("filter", new ImageFileFilter());
	    startActivityForResult(intent, 1);
	    break;
	case CANCEL:
	    setResult(0);
	    finish();
	    break;
	}
	return true;
    }

    public void setupUI() {
	Typeface title = Typeface.createFromAsset(getAssets(), "Roboto-LightItalic.ttf");
	label = (TextView)findViewById(R.id.preset_label);
	label.setTypeface(title);
	listView = (ListView)findViewById(R.id.preset_list);
	listView.setAdapter(list);

	listView.setOnItemClickListener(listListener);
    }

    private OnItemClickListener listListener = new OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
		if(position == 0) {
		    setResult(3);
		} else {
		    Intent intent = new Intent();
		    intent.putExtra("resource", drawables[position]);
		    setResult(2, intent);
		}
		finish();
	    }
	};

     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	 /* Propogate directory chooser result. */
	 if(resultCode == 1) {
	     setResult(1, data);
	     finish();
	 } else if(resultCode == -1) {
	     setResult(-1);
	     finish();
	 }
     }

}