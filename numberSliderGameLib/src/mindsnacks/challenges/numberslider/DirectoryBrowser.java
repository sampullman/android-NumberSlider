package com.threeDBJ.numberSlider;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.res.Configuration;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class DirectoryBrowser extends SherlockActivity {

    static final int BACK_ID=1, ROOT_ID=2, CANCEL_ID=3;

    String ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    //String ROOT = "/mnt/";
    String path = ROOT;

    FileFilter filter;

    ArrayList<String> items = new ArrayList<String>();
    ArrayList<File> files = new ArrayList<File>();
    ArrayAdapter<String> list;

    ListView listView;
    TextView label;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	ActionBar bar = getSupportActionBar();
	bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	bar.setDisplayShowHomeEnabled(false);
	bar.setDisplayShowTitleEnabled(false);

	String state = Environment.getExternalStorageState();
	if(!(Environment.MEDIA_MOUNTED.equals(state) ||
	     Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))) {
	    setResult(-1);
	    finish();
	}

	setContentView(R.layout.directory);

	list = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
					android.R.id.text1);
	filter = (FileFilter)getIntent().getExtras().getParcelable("filter");
	setupUI();
	setDirView();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	menu.add(0, BACK_ID, 0, "Back")
	    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	menu.add(0, ROOT_ID, 0, "Root")
	    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	menu.add(0, CANCEL_ID, 0, "Cancel")
	    .setIcon(R.drawable.ic_menu_close_clear_cancel)
	    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	Intent intent;
	switch (item.getItemId()) {
	case BACK_ID:
	    if(!path.equals(ROOT)) {
		int ind = path.lastIndexOf("/");
		path = path.substring(0, ind);
		setDirView();
	    }
	    break;
	case ROOT_ID:
	    path = ROOT;
	    setDirView();
	    break;
	case CANCEL_ID:
	    setResult(0);
	    finish();
	    break;
	}
	return true;
    }

    public void setupUI() {
	label = (TextView)findViewById(R.id.dir_label);
	listView = (ListView)findViewById(R.id.dir_list);
	listView.setOnItemClickListener(listListener);
	listView.setAdapter(list);
    }

    @Override
    public void onBackPressed() {
	if(path.equals(ROOT)) {
	    setResult(0);
	    finish();
	} else {
	    int ind = path.lastIndexOf("/");
	    path = path.substring(0, ind);
	    setDirView();
	}
    }

    private void setDirView() {
	items.clear();
	files.clear();
	File dir = new File(path);
	File[] fileList;
	if(filter == null) {
	    fileList = dir.listFiles();
	} else {
	    fileList = dir.listFiles(filter);
	}
	for(int i=0;i<fileList.length;i+=1) {
	    items.add(fileList[i].getName());
	    files.add(fileList[i]);
	}
	list.clear();
	for(String item : items)
	    list.add(item);
	label.setText("Showing directory: " + path);
    }

    private OnItemClickListener listListener = new OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view,
				    int position, long id) {
		path = files.get(position).getAbsolutePath();
		if(files.get(position).isDirectory()) {
		    setDirView();
		} else {
		    Intent intent = new Intent();
		    intent.putExtra("path", path);
		    setResult(1, intent);
		    finish();
		}
	    }
	};
}