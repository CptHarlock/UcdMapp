package com.mapapp.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ucdmap.R;
import android.R.drawable;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.mapapp.helpers.PointD;
import com.mapapp.main.MapAppActivity;
import com.mapapp.mapapp.AZ;
import com.mapapp.tileManagement.Tile;
import com.mapapp.tileManagement.TilesManager;
import com.mapapp.tileManagement.TilesProvider;

public class MapView extends View
{
	// Needed to pass to View constructor
	protected Context context;

	// MapView dimensions
	protected int viewWidth, viewHeight;
	protected ArrayList<String> data;

	// Provides us with tiles
	protected TilesProvider tileProvider;

	// Handles calculations
	protected TilesManager tileManager;
	int markerX ;
	int markerY ;
	// Different paints
	protected Paint fontPaint;
	protected Paint bitmapPaint = new Paint();
	protected Paint circlePaint = new Paint();
	protected Paint dot = new Paint();
	protected Paint markdot = new Paint();

	// The location of the view center in longitude, latitude
	protected PointD seekLocation = new PointD(0, 0);
	protected PointD scope = new PointD(0, 0);
	// Location of the phone using Gps data
	protected Location gpsLocation = null;
	// If true then seekLocation will always match gpsLocation
	protected boolean autoFollow = false;

	// An image to draw at the phone's position
	protected Bitmap positionMarker;
	Bitmap mBitmap = null;
	// touch position values kept for panning\dragging
	protected PointD lastTouchPos = new PointD(-1, -1);
	public MapView(Context context, int viewWidth, int viewHeight, TilesProvider tilesProvider, Bitmap positionMarker,ArrayList<String> data)
	{
		super(context);
		this.context = context;
		this.data = data;
		// Tiles provider is passed not created.
		// The idea is to hide the actual tiles source from the view
		// This way the view doesn't care whether the source is a database or
		// the internet
		this.tileProvider = tilesProvider;

		// These values will be used later
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;

		// Get the marker image
		this.positionMarker = positionMarker;

		// Creating a TilesManager assuming that the tile size is 256*256.
		// You might want to pass tile size as a parameter or even calculate it
		// somehow
		tileManager = new TilesManager(256, viewWidth, viewHeight);

		// Initializes paints
		initPaints();

		// Fetching tiles from the tilesProvider
		fetchTiles();


	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// Setting width,height that was passed in the constructor as the view's
		// dimensions
		setMeasuredDimension(viewWidth, viewHeight);
	}

	void initPaints()
	{
		// Font paint is used to draw text
		fontPaint = new Paint();
		fontPaint.setColor(Color.DKGRAY);
		fontPaint.setShadowLayer(1, 1, 1, Color.RED);
		fontPaint.setTextSize(20);

		// Used to draw a semi-transparent circle at the phone's gps location
		circlePaint.setARGB(70, 170, 170, 80);
		circlePaint.setAntiAlias(true);
		dot.setARGB(255,255,255,10);
	}

	void fetchTiles()
	{
		// Update tilesManager to have the center of the view as its location
		tileManager.setLocation(seekLocation.x, seekLocation.y);

		// Get the visible tiles indices as a Rect
		Rect visibleRegion = tileManager.getVisibleRegion();

		// Tell tiles provider what tiles we need and which zoom level.
		// The tiles will be stored inside the tilesProvider.
		// We can get those tiles later when drawing the view
		tileProvider.fetchTiles(visibleRegion, tileManager.getZoom());
	}

	void drawMarkers(Canvas canvas,Point offset){
		ArrayList<String> azItems = new ArrayList<String>();
		
		azItems = data;
		int i;
		 String[] stockArr = new String[data.size()];
		    stockArr = data.toArray(stockArr);
		for(i=0;i<data.size();i++){
			String[] result = stockArr[i].split(",");
			Double lon =  Double.parseDouble(result[1]);
			Double lat =  Double.parseDouble(result[0]);
		Point markerPos1 = tileManager.lonLatToPixelXY(lon,lat);
		//markdot.setARGB(70, 170, 170, 80);
        Paint oink = new Paint();
		// Add offset to the marker position
		 int markerX1 = markerPos1.x - offset.x;
		 int markerY1 = markerPos1.y - offset.y;
		
		 canvas.drawCircle((float) markerX1, (float) markerY1,6,bitmapPaint);
		}
	
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		// Clear the view to grey
		canvas.drawARGB(255, 100, 100, 100);

		/*
		 * To draw the map we need to find the position of the pixel representing the center of the view.
		 * We need the position to be relative to the full world map, lets call this pixel position "pix"
		 * pix.x will range from 0 to (2^zoom)*tileSize-1, same for pix.y		
		 * To draw anything on the map we subtract pix from the original position
		 * It's just like dragging the map so that the pixel representing the gps location gets into the center of the view 		 
		*/

		// In a square world map,
		// we need to know pix location as two values from 0.0 to 1.0
		PointD pixRatio = TilesManager.calcRatio(seekLocation.x, seekLocation.y);

		// Full world map width in pixels
		int mapWidth = tileManager.mapSize() * 256;
		Point pix = new Point((int) (pixRatio.x * mapWidth), (int) (pixRatio.y * mapWidth));

		/*
		 * Subtracting pix from each tile position will result in pix being drawn at the top left corner of the view 
		 * To drag it to the center we add (viewWidth/2, viewHeight/2) to the final result
		 * pos.x = pos.x - pix.x + viewWidth/2f
		 * pos.x = pox.x - (pix.x - viewWidth/2f)
		 * ---> offset.x =  (pix.x - viewWidth/2f)
		 * same for offset.y
		 */

		Point offset = new Point((int) (pix.x - viewWidth / 2f), (int) (pix.y - viewHeight / 2f));
		// offset is now ready to use

		// Drawing tiles in a separate function to make the code more readable
		drawTiles(canvas, offset);
		//drawOverlay(canvas, int x, int y)
		// Draw the marker that pinpoints the user's location
		drawMarker(canvas, offset);
		if (MapAppActivity.oi==true){
		drawMarkers(canvas, offset);
		}
		
	}

	void drawTiles(Canvas canvas, Point offset)
	{
		// Get tiles from the Hashtable inside tilesProvider
		Collection<Tile> tilesList = tileProvider.getTiles().values();

		// x,y are the calculated offset
		Rect dst = new Rect(0, 0, 0, 0);

		// Go trough all the available tiles
		for (Tile tile : tilesList)
		{
			// We act as if we're drawing a map of the whole world at a specific
			// zoom level
			// The top left corner of the map occupies the pixel (0,0) of the
			// view
			int tileSize = tileManager.getTileSize();
			long tileX = tile.x * tileSize;
			long tileY = tile.y * tileSize;

			// Subtract offset from the previous calculations
			long finalX = tileX - offset.x;
			long finalY = tileY - offset.y;

			// Draw the bitmap of the tiles using a simple paint
			//canvas.drawBitmap(tile.img, finalX, finalY, bitmapPaint);
			dst.set((int) finalX,(int) finalY,(int) finalX + tileSize,(int) finalY + tileSize);
			canvas.drawBitmap(tile.img, null, dst, bitmapPaint);
			
		}
		
		
	}

	
	
	void drawMarker(Canvas canvas, Point offset)
	{
		if (gpsLocation != null)
		{

			scope = getSeekLocation();
			int mapWidth0 = tileManager.mapSize() * 256;
			PointD pixRatio0 = TilesManager.calcRatio(scope.x, scope.y);
			Point pix0 = new Point((int) (pixRatio0.x * mapWidth0), (int) (pixRatio0.y * mapWidth0));
			Point offset0 = new Point((int) (pix0.x - viewWidth / 2f), (int) (pix0.y - viewHeight / 2f));
			Point markerPos0 = tileManager.lonLatToPixelXY(scope.x, scope.y);
			int markerX0 = markerPos0.x - offset0.x;
			int  markerY0 = markerPos0.y - offset0.y;
			
			Point markerPos = tileManager.lonLatToPixelXY(gpsLocation.getLongitude(), gpsLocation.getLatitude());

			// Add offset to the marker position
			 markerX = markerPos.x - offset.x;
			 markerY = markerPos.y - offset.y;

			// Draw the marker and make sure you draw the center of the marker
			// at the marker location
			canvas.drawBitmap(positionMarker, markerX - positionMarker.getWidth() / 2, markerY - positionMarker.getHeight() / 2,
					bitmapPaint);


			
			// Calculate how many meters one pixel represents
			float ground = (float) tileManager.calcGroundResolution(gpsLocation.getLatitude());
 
			// Location.getAccuracy() returns the accuracy in meters.
			float rad = gpsLocation.getAccuracy() / ground;
			int pen = 1;
			{
			canvas.drawText("lon:" + gpsLocation.getLongitude(), 0, 20 * pen++, fontPaint);
			canvas.drawText("lat:" + gpsLocation.getLatitude(), 0, 20 * pen++, fontPaint);
			Paint dot2 = new Paint();
			dot2.setARGB(255,0,255,0);
			canvas.drawCircle(markerX0 - positionMarker.getWidth() / 2, markerY0 - positionMarker.getHeight() / 2,10,dot2);
			
			}
		}
		else if (gpsLocation == null)
			scope = getSeekLocation();
		int mapWidth0 = tileManager.mapSize() * 256;
		PointD pixRatio0 = TilesManager.calcRatio(scope.x, scope.y);
		Point pix0 = new Point((int) (pixRatio0.x * mapWidth0), (int) (pixRatio0.y * mapWidth0));
		Point offset0 = new Point((int) (pix0.x - viewWidth / 2f), (int) (pix0.y - viewHeight / 2f));
		Point markerPos0 = tileManager.lonLatToPixelXY(scope.x, scope.y);
		int markerX0 = markerPos0.x - offset0.x;
		int markerY0 = markerPos0.y - offset0.y;
		{
		Paint dot1 = new Paint();
		dot1.setARGB(100,255,0,0);
		canvas.drawCircle(markerX0 - positionMarker.getWidth() / 2, markerY0 - positionMarker.getHeight() / 2,10,dot1);
			
			int pen = 1;
			canvas.drawText("lon:" + "--", 0, 20 * pen++, fontPaint);
			canvas.drawText("lat:" + "--", 0, 20 * pen++, fontPaint);
		}
	}


	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		int action = event.getAction();
		
		int x = (int) event.getX();
		int y = (int) event.getY();
		
		PointD pixRatio = TilesManager.calcRatio(seekLocation.x, seekLocation.y);
		int mapWidth = tileManager.mapSize() * 256;
		Point pix = new Point((int) (pixRatio.x * mapWidth), (int) (pixRatio.y * mapWidth));
		Point offset = new Point((int) (pix.x - viewWidth / 2f), (int) (pix.y - viewHeight / 2f));
		ArrayList<String> azItems = new ArrayList<String>();	
		azItems = data;
		int i;
		 String[] stockArr = new String[data.size()];
		    stockArr = data.toArray(stockArr);
		
		if (action == MotionEvent.ACTION_DOWN)
		{
			if (MapAppActivity.oi==true){
			for(i=0;i<data.size();i++){
				String[] result = stockArr[i].split(",");
				Double lon =  Double.parseDouble(result[1]);
				Double lat =  Double.parseDouble(result[0]);
			Point markerPos1 = tileManager.lonLatToPixelXY(lon,lat);
			 int markerX1 = markerPos1.x - offset.x;
			 int markerY1 = markerPos1.y - offset.y;
			if ((x >= markerX1 && x < (markerX1 + 50))
		            && 	(y >= markerY1) && (y < (markerY1 + 50))) {
				String t="";
				 if (gpsLocation != null){
				double k = haversine_km(lat, lon, gpsLocation.getLatitude(), gpsLocation.getLongitude());
				 t = walking(k);
				 }
				CharSequence text = result[2]+ "\n"+t;
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.setGravity(Gravity.CENTER|Gravity.CENTER, 0, 0);
				toast.show();
			 }
			}
			}
			// Keep touch position for later use (dragging)
			lastTouchPos.x = (int) event.getX();
			lastTouchPos.y = (int) event.getY();
		
			return true;
		}
		else if (action == MotionEvent.ACTION_MOVE)
		{
			autoFollow = false;

			PointD current = new PointD(event.getX(), event.getY());
			
			// Find how many pixels the users finger moved in both x and y
			PointD diff = new PointD(current.x - lastTouchPos.x, current.y - lastTouchPos.y);

			// In a full wolrd map, get the position of the center of the view in pixels
			Point pixels1 = tileManager.lonLatToPixelXY(seekLocation.x, seekLocation.y);
			
			// Subtract diff from that position
			Point pixels2 = new Point(pixels1.x - (int) diff.x, pixels1.y - (int) diff.y);

			// Recnovert the final result to longitude, latitude point
			PointD newSeek = tileManager.pixelXYToLonLat((int) pixels2.x, (int) pixels2.y);

			// Finally move the center of the view to the new location
			seekLocation = newSeek;

			// Refresh the view
			fetchTiles();
			invalidate(); // Causes the view to redraw itself
			refresh();
			// Prepare for the next drag event
			lastTouchPos.x = current.x;
			lastTouchPos.y = current.y;

			return true;
		}

		return super.onTouchEvent(event);
	}


	double haversine_km(double lat1, double long1, double lat2, double long2)
	{
		double d2r =(Math.PI/ 180.0);
	    double dlong = (long2 - long1) * d2r;
	    double dlat = (lat2 - lat1) * d2r;
	    double a = Math.pow(Math.sin(dlat/2.0), 2) + Math.cos(lat1*d2r) * Math.cos(lat2*d2r) * Math.pow(Math.sin(dlong/2.0), 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double d = 6367 * c;

	    return d;
	}
	
	String walking(double d){
		double k = (d*1000)/60;
		k = k * d;
		int j = (int)k;
		String wt=Double.toString(j);
		if (j < 1){
			wt ="Walking Distace: less than a minute";
		}else{	
			wt ="Walking Distace:" + j + " minutes";
		}
		return wt;
	}
	

	// Fetch the tiles then draw, don't call to often
	public void refresh()
	{
		fetchTiles();
		invalidate();
	}

	// Like refresh but called from a non UI thread
	public void postRefresh()
	{
		fetchTiles();
		postInvalidate();
	}

	// Simply sets seek location to gpsLocation (if exists) 
	public void followMarker()
	{
		if (gpsLocation != null)
		{
			seekLocation.x = gpsLocation.getLongitude();
			seekLocation.y = gpsLocation.getLatitude();
			autoFollow = true;
			
			fetchTiles();
			invalidate();
		}
	}

	public void zoomIn()
	{
		tileManager.zoomIn();
		onMapZoomChanged();
	}

	public void zoomOut()
	{
		tileManager.zoomOut();
		onMapZoomChanged();
	}
	
	protected void onMapZoomChanged()
	{
		tileProvider.clear();
		fetchTiles();
		
		invalidate();
	}

	// Returns the gps coordinates of the user
	public Location getGpsLocation()
	{
		return gpsLocation;
	}

	// Returns the gps coordinates of our view center
	public PointD getSeekLocation()
	{
		return seekLocation;
	}

	// Centers the given gps coordinates in our view
	public void setSeekLocation(double longitude, double latitude)
	{
		seekLocation.x = longitude;
		seekLocation.y = latitude;
	}

	// Sets the marker position
	public void setGpsLocation(Location location)
	{
		setGpsLocation(location.getLongitude(), location.getLatitude(), location.getAltitude(), location.getAccuracy());
	
	}

	// Sets the marker position
	public void setGpsLocation(double longitude, double latitude, double altitude, float accuracy)
	{
		if (gpsLocation == null) gpsLocation = new Location("");
		gpsLocation.setLongitude(longitude);
		gpsLocation.setLatitude(latitude);
		gpsLocation.setAltitude(altitude);
		gpsLocation.setAccuracy(accuracy);
		
		if (autoFollow) followMarker();

	}

	public int getZoom()
	{
		return tileManager.getZoom();
	}

	public void setZoom(int zoom)
	{
		tileManager.setZoom(zoom);
		onMapZoomChanged();
	}


}
