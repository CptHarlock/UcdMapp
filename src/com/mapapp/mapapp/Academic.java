package com.mapapp.mapapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import com.mapapp.main.MapAppActivity;
import com.ucdmap.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class Academic extends ListActivity {
	public SQLiteDatabase dataDB;
	public final static String EXTRA_MESSAGE = "";
	String path;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_az);	

		File f = new File(getCacheDir()+"/data.sqlitedb");
		
		 if (!f.exists()) try {

			    InputStream is = getAssets().open("data.sqlitedb");
			    int size = is.available();
			    byte[] buffer = new byte[size];
			    is.read(buffer);
			    is.close();
			    Log.d("oink","OOOOO");

			    FileOutputStream fos = new FileOutputStream(f);
			    fos.write(buffer);
			    fos.close();
			  } catch (Exception e) { throw new RuntimeException(e); }

		
		path =  f.getPath();
		//dataDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase
		ArrayList<String> listItems = new ArrayList<String>();
		//String j = getData();
		//listItems.add(j);
		listItems = getAZlist(path);
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, listItems));
		
		ListView lv = getListView();
		// listening to single list item on click
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				String result = getCCData(id);
				// Launching new Activity that shows courses details on click
				Intent i = new Intent(getApplicationContext(), MapAppActivity.class);
				// sending data to new activity
				i.putExtra(EXTRA_MESSAGE, result);
				startActivity(i);
				finish();
				
			}
		});
	}
	
public String getCCData(long id) {
		
	dataDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
	String query = "select * from data where Type = 'Academic' order by Name;";
	Cursor cursor;
	cursor = dataDB.rawQuery(query, null);
	ArrayList<String> azItems = new ArrayList<String>();
	cursor.moveToFirst();
	cursor.moveToPosition((int) id);
	String place = ( cursor.getString(1)+","+ cursor.getString(2));
	cursor.close();
	return place;
	}
	
public ArrayList<String> getAZlist(String path) {
	dataDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
	String query = "select * from data where Type = 'Academic' order by Name;";
	Cursor cursor;
	cursor = dataDB.rawQuery(query, null);
	ArrayList<String> azItems = new ArrayList<String>();
	int k = cursor.getCount();
	cursor.moveToFirst();
	int i;
	for (i=1;i<=k;i++){
		String place = ( cursor.getString(3));
		azItems.add(place);
		cursor.moveToNext();
	}
	cursor.close();
	return azItems;
}



}
