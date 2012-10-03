package com.threeDBJ.numberSlider;

import android.os.Parcelable;
import android.os.Parcel;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;

import android.util.Log;

public class ImageFileFilter implements FileFilter, Parcelable {

    HashSet<String> types = new HashSet<String>();

    public ImageFileFilter() {
	types.add("jpg");
	types.add("jpeg");
	types.add("png");
	types.add("gif");
	types.add("bmp");
    }

    public boolean accept(File f) {
	if(f.isDirectory()) {
	    return !f.getName().startsWith(".");
	}
	String name = f.getName().toLowerCase();
	int ind = name.lastIndexOf(".");
	Log.e("name",f.getName()+" "+name.substring(ind + 1));
	return (ind > 0) && (ind < name.length()-1)
	    && types.contains(name.substring(ind + 1));
    }

    public void writeToParcel(Parcel out, int flags) {
    }

    public int describeContents() {
	return 0;
    }

    public static final Parcelable.Creator<ImageFileFilter> CREATOR
	= new Parcelable.Creator<ImageFileFilter>() {
	public ImageFileFilter createFromParcel(Parcel in) {
	    return new ImageFileFilter();
	}

	public ImageFileFilter[] newArray(int size) {
	    return new ImageFileFilter[size];
	}
    };

}
