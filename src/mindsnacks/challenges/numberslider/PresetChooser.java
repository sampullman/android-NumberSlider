package com.threeDBJ.numberSlider;

import android.app.Activity;
import android.os.Bundle;

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

public class PresetChooser extends Activity {

    static String[] labels = new String[] { "Default", "Kitten", "Puppy", "Koala", "Swans", "Mona Lisa" };
    static int[] drawables = new int[] { 0, R.drawable.kitten, R.drawable.puppy, R.drawable.koala,
					 R.drawable.swans, R.drawable.mona_lisa };

    ListView listView;
    TextView label;
    ArrayAdapter<String> list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	Configuration config = this.getResources().getConfiguration();
	if(config.orientation == 1) {
            setContentView(R.layout.preset);
        } else if(config.orientation == 2) {
            setContentView(R.layout.preset_wide);
	}

	list = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
					android.R.id.text1, labels);
	setupUI();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
	if(config.orientation == 1) {
            setContentView(R.layout.preset);
        } else if(config.orientation == 2) {
            setContentView(R.layout.preset_wide);
	}
	setupUI();
    }

    public void setupUI() {
	label = (TextView)findViewById(R.id.preset_label);
	listView = (ListView)findViewById(R.id.preset_list);
	listView.setAdapter(list);

	Button b = (Button)findViewById(R.id.preset_dir);
	b.setOnClickListener(dirListener);
	b = (Button)findViewById(R.id.preset_cancel);
	b.setOnClickListener(cancelListener);

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

    private OnClickListener dirListener = new OnClickListener() {
	    public void onClick(View v) {
		Intent intent = new Intent(PresetChooser.this, DirectoryBrowser.class);
		intent.putExtra("filter", new ImageFileFilter());
		startActivityForResult(intent, 1);
	    }
	};

    private OnClickListener cancelListener = new OnClickListener() {
	    public void onClick(View v) {
		setResult(0);
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