package com.mapapp.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import com.mapapp.helpers.PointD;
import com.mapapp.mapapp.AZ;
import com.mapapp.mapapp.AndroidTabLayoutActivity;

import com.ucdmap.R;
import com.mapapp.tileManagement.TilesManager;
import com.mapapp.tileManagement.TilesProvider;
import com.mapapp.views.MapView;
import com.mapapp.views.MapViewLocationListener;

public class MapAppActivity extends Activity
{

	private final class Save
	{
		public final static String GPS_LON = "gpsLon";
		public final static String GPS_LAT = "gpsLAT";
		public final static String GPS_ALT = "gpsALT";
		public final static String GPS_ACC = "gpsAcc";
	}

	private final class Pref
	{
		public final static String SEEK_LON = "seek_lon";
		public final static String SEEK_LAT = "seek_lat";
		public final static String ZOOM = "zoom";
	}
	public SQLiteDatabase dataDB;
	public static boolean oi=false; 
	// only view, created in code
	MapView mapView;
	// Provides with Tiles objects, passed to MapView
	TilesProvider tilesProvider;

	// Updates marker location in MapView
	MapViewLocationListener locationListener;

	Location savedGpsLocation;


	
	@Override
	protected void onResume()
	{
		Bundle extras = getIntent().getExtras();
		// Create MapView
		initViews();

		// Restore zoom and location data for the MapView
		restoreMapViewSettings();

		// Creating and registering the location listener
		locationListener = new MapViewLocationListener(mapView);
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		 //Set our MapView as the main view for the activity
		setContentView(R.layout.mapappactivity);
	    // Get the RelativeLayout which is parent of all other views
	    RelativeLayout frame = (RelativeLayout) findViewById(R.id.frame);
	         
	    // Add the mapView at position 0 which means it'll be the first view to get rendered.
	    frame.addView(mapView, 0);
	 
	    ZoomControls zoomControls = (ZoomControls) findViewById(R.id.zoom);
	    zoomControls.setOnZoomInClickListener(new OnClickListener()
	    {
	        @Override
	        public void onClick(View v)
	        {
	            mapView.zoomIn();
	        }
	    });
	    zoomControls.setOnZoomOutClickListener(new OnClickListener()
	    {
	        @Override
	        public void onClick(View v)
	        {
	            mapView.zoomOut();
	        }
	    });
	    Button follow = (Button) findViewById(R.id.follow);
	    
	    follow.setOnClickListener(new View.OnClickListener()
	    {
	        @Override
	        public void onClick(View v)
	        {	
	        	mapView.invalidate();
	        	mapView.followMarker();
	        }
	    });
	    
 Button dots = (Button) findViewById(R.id.dots);

	    dots.setOnClickListener(new View.OnClickListener()
	    {
	        @Override
	        public void onClick(View v)
	        {	
	        	oi=!oi;
	        	mapView.refresh();
	        }
	    });
	    
	    Button listmode = (Button) findViewById(R.id.listmode);
	    
		listmode.setOnClickListener(new View.OnClickListener()
	    {
	        @Override
	        public void onClick(View v)
	        {	
	        	
	        	Intent intent = new Intent(getBaseContext(), AndroidTabLayoutActivity.class);
	    		startActivity(intent);
	    		finish();
	        }
	    });
	    
		if (extras!=null){
			
			String data = extras.getString(AZ.EXTRA_MESSAGE);	
			if (data!=null){
			String[] result = data.split(",");
			double lat = Double.parseDouble(result[0]);
			double lon = Double.parseDouble(result[1]);

			mapView.setSeekLocation(lon,lat);
			mapView.refresh();
			data =null;
			}
		}
		super.onResume();
	}

	
	void initViews()
	{
		// Creating the bitmap of the marker from the resources
		Bitmap marker = BitmapFactory.decodeResource(getResources(), R.drawable.marker);

		// Creating our database tilesProvider to pass it to our MapView
		File f = new File(getCacheDir()+"/oink1.sqlitedb");
		
		 if (!f.exists()) try {

			    InputStream is = getAssets().open("oink1.sqlitedb");
			    int size = is.available();
			    byte[] buffer = new byte[size];
			    is.read(buffer);
			    is.close();
			    Log.d("oink","OOOOO");

			    FileOutputStream fos = new FileOutputStream(f);
			    fos.write(buffer);
			    fos.close();
			  } catch (Exception e) { throw new RuntimeException(e); }

		
		String path =  f.getPath();
		tilesProvider = new TilesProvider(path);
		Display display = getWindowManager().getDefaultDisplay();
	
		File D = new File(getCacheDir()+"/data.sqlitedb");
		
		 if (!D.exists()) try {

			    InputStream iso = getAssets().open("data.sqlitedb");
			    int size = iso.available();
			    byte[] buffero = new byte[size];
			    iso.read(buffero);
			    iso.close();
			    Log.d("data","OOOOO");

			    FileOutputStream foso = new FileOutputStream(D);
			    foso.write(buffero);
			    foso.close();
			  } catch (Exception e) { throw new RuntimeException(e); }

		
		String path0 =  D.getPath();
		ArrayList<String> data = new ArrayList<String>();
		
		data = getAZlistmap(path0);
		
		
		mapView = new MapView(this, display.getWidth(), display.getHeight(), tilesProvider, marker,data);

		// If a location was saved while pausing the app then use it.
		if (savedGpsLocation != null) mapView.setGpsLocation(savedGpsLocation);
		

		// Update and draw the map view
		mapView.refresh();
	}

	public ArrayList<String> getAZlistmap(String path) {
		dataDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY);
		String query = "SELECT * FROM data order by Name";
		Cursor cursor;
		cursor = dataDB.rawQuery(query, null);
		ArrayList<String> azItems = new ArrayList<String>();
		int k = cursor.getCount();
		cursor.moveToFirst();
		int i;
		for (i=1;i<=k;i++){
			String place = ( cursor.getString(1) + "," +cursor.getString(2) + "," +cursor.getString(3) + "," +cursor.getString(4) + "," +cursor.getString(5));
			azItems.add(place);
			cursor.moveToNext();
		}
		cursor.close();
		return azItems;
	}
	
	@Override
	protected void onPause()
	{
		// Save settings before leaving
		saveMapViewSettings();

		// Mainly releases the MapView pointer inside the listener
		locationListener.stop();

		// Unregistering our listener
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationManager.removeUpdates(locationListener);

		// Closes the source of the tiles (Database in our case)
		tilesProvider.close();
		// Clears the tiles held in the tilesProvider
		tilesProvider.clear();

		// Release mapView pointer
		mapView = null;

		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// Zooming
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_Z)
		{
			mapView.zoomIn();
			return true;
		}
		// Zooming
		else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_X)
		{
			mapView.zoomOut();
			return true;
		}
		

		return super.onKeyDown(keyCode, event);
	}

	// Called manually to restore settings from SharedPreferences
	void restoreMapViewSettings()
	{
		SharedPreferences pref = getSharedPreferences("View_Settings", MODE_PRIVATE);

		double lon, lat;
		int zoom;

		lon = Double.parseDouble(pref.getString(Pref.SEEK_LON, "0"));
		lat = Double.parseDouble(pref.getString(Pref.SEEK_LAT, "0"));
		zoom = pref.getInt(Pref.ZOOM, 0);

		mapView.setSeekLocation(lon, lat);
		mapView.setZoom(zoom);
		mapView.refresh();
	}

	// Called manually to save settings in SharedPreferences
	void saveMapViewSettings()
	{
		SharedPreferences.Editor editor = getSharedPreferences("View_Settings", MODE_PRIVATE).edit();

		PointD seekLocation = mapView.getSeekLocation();
		editor.putString(Pref.SEEK_LON, Double.toString(seekLocation.x));
		editor.putString(Pref.SEEK_LAT, Double.toString(seekLocation.y));
		editor.putInt(Pref.ZOOM, mapView.getZoom());

		editor.commit();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		if (mapView.getGpsLocation() != null)
		{
			outState.putDouble(Save.GPS_LON, mapView.getGpsLocation().getLongitude());
			outState.putDouble(Save.GPS_LAT, mapView.getGpsLocation().getLatitude());
			outState.putDouble(Save.GPS_ALT, mapView.getGpsLocation().getAltitude());
			outState.putFloat(Save.GPS_ACC, mapView.getGpsLocation().getAccuracy());
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		double gpsLon, gpsLat, gpsAlt;
		float gpsAcc;

		gpsLon = savedInstanceState.getDouble(Save.GPS_LON, 999);
		gpsLat = savedInstanceState.getDouble(Save.GPS_LAT, 999);
		gpsAlt = savedInstanceState.getDouble(Save.GPS_ALT, 999);
		gpsAcc = savedInstanceState.getFloat(Save.GPS_ACC, 999);

		if (gpsLon != 999 && gpsLat != 999 && gpsAlt != 999 && gpsAcc != 999)
		{
			savedGpsLocation = new Location(LocationManager.GPS_PROVIDER);
			savedGpsLocation.setLongitude(gpsLon);
			savedGpsLocation.setLatitude(gpsLat);
			savedGpsLocation.setAltitude(gpsAlt);
			savedGpsLocation.setAccuracy(gpsAcc);
		}

		super.onRestoreInstanceState(savedInstanceState);
	}
}