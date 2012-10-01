package mindsnacks.challenges.numberslider;

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

public class DirectoryBrowser extends Activity {

    String ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
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
	String state = Environment.getExternalStorageState();
	if(!(Environment.MEDIA_MOUNTED.equals(state) ||
	     Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))) {
	    setResult(-1);
	    finish();
	}

	Configuration config = this.getResources().getConfiguration();
	if(config.orientation == 1) {
            setContentView(R.layout.directory);
        } else if(config.orientation == 2) {
            setContentView(R.layout.directory_wide);
	}

	list = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
					android.R.id.text1);
	filter = (FileFilter)getIntent().getExtras().getParcelable("filter");
	setupUI();
	setDirView();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
	if(config.orientation == 1) {
            setContentView(R.layout.directory);
        } else if(config.orientation == 2) {
            setContentView(R.layout.directory_wide);
	}
	setupUI();
	setDirView();
    }

    public void setupUI() {
	label = (TextView)findViewById(R.id.dir_label);
	listView = (ListView)findViewById(R.id.dir_list);

	Button b = (Button)findViewById(R.id.dir_back);
	b.setOnClickListener(backListener);
	b = (Button)findViewById(R.id.dir_root);
	b.setOnClickListener(rootListener);
	b = (Button)findViewById(R.id.dir_cancel);
	b.setOnClickListener(cancelListener);

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
	list.addAll(items);
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

    private OnClickListener backListener = new OnClickListener() {
	    public void onClick(View v) {
		if(!path.equals(ROOT)) {
		    int ind = path.lastIndexOf("/");
		    path = path.substring(0, ind);
		    setDirView();
		}
	    }
	};

    private OnClickListener rootListener = new OnClickListener() {
	    public void onClick(View v) {
		path = ROOT;
		setDirView();
	    }
	};

    private OnClickListener cancelListener = new OnClickListener() {
	    public void onClick(View v) {
		setResult(0);
		finish();
	    }
	};

}