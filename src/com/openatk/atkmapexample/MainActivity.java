package com.openatk.atkmapexample;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.openatk.openatklib.atkmap.ATKMap;
import com.openatk.openatklib.atkmap.ATKSupportMapFragment;
import com.openatk.openatklib.atkmap.listeners.ATKMapClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPointClickListener;
import com.openatk.openatklib.atkmap.listeners.ATKPointDragListener;
import com.openatk.openatklib.atkmap.listeners.ATKPolygonClickListener;
import com.openatk.openatklib.atkmap.models.ATKPoint;
import com.openatk.openatklib.atkmap.models.ATKPolygon;
import com.openatk.openatklib.atkmap.views.ATKPointView;
import com.openatk.openatklib.atkmap.views.ATKPolygonView;

import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity implements ATKMapClickListener, ATKPointClickListener, ATKPointDragListener, ATKPolygonClickListener {

	// Startup position
	private static final float START_LAT = 40.428712f;
	private static final float START_LNG = -86.913819f;
	private static final float START_ZOOM = 17.0f;
	
	private ATKMap map;
	private ATKSupportMapFragment atkmapFragment;
	private UiSettings mapSettings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		FragmentManager fm = getSupportFragmentManager();
		atkmapFragment = (ATKSupportMapFragment) fm.findFragmentById(R.id.map);

		if (savedInstanceState == null) {
			// First incarnation of this activity.
			atkmapFragment.setRetainInstance(true);
		} else {
			// Reincarnated activity. The obtained map is the same map instance in the previous
			// activity life cycle. There is no need to reinitialize it.
			map = atkmapFragment.getAtkMap();
		}
		setUpMapIfNeeded();
	}

	private void setUpMapIfNeeded() {
		if (map == null) {
			//Map is null try to find it
			map = ((ATKSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getAtkMap();
		}

		if (atkmapFragment.getRetained() == false) {
			//New map, we need to set it up
			setUpMap();
			
			//Move to where we were last time
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			Float startLat = prefs.getFloat("StartupLat", START_LAT);
			Float startLng = prefs.getFloat("StartupLng", START_LNG);
			Float startZoom = prefs.getFloat("StartupZoom", START_ZOOM);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(startLat, startLng), startZoom));
		}
		
		//Setup atkmap listeners, note: these are the atkmap listeners, per object listeners can override these ie. (ATKPoint.setOnClickListener())
		map.setOnPointClickListener(this);
		map.setOnPointDragListener(this);
		map.setOnPolygonClickListener(this);
		map.setOnMapClickListener(this);
	}
	
	private void setUpMap() {
		//Set map settings
		mapSettings = map.getUiSettings();
		mapSettings.setZoomControlsEnabled(false);
		mapSettings.setMyLocationButtonEnabled(false);
		mapSettings.setTiltGesturesEnabled(false);
		map.setMyLocationEnabled(true);
		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if(item.getItemId() == R.id.add_point){
			AddPoint();
		} else if(item.getItemId() == R.id.add_polygon){
			AddPolygon();
		} else if(item.getItemId() == R.id.update_polygon){
			UpdatePolygon();
		} else if(item.getItemId() == R.id.draw_polygon){
			DrawPolygon();
		} else if(item.getItemId() == R.id.close_polygon){
			ClosePolygon();
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void AddPoint(){
		//Get spot to add point to (Center of screen)
		LatLng where = map.getCameraPosition().target;
		
		//Add point to map
		//ATKPoint(Object id, LatLng position)
		//Id is used to identify the point later so should be unique, can be any type
		int id = map.getPointViews().size(); //Get the number of points on the map at this time
		ATKPoint newPoint = new ATKPoint(id, where);
		//This adds the point to the map so it should be visible after this
		ATKPointView added = map.addPoint(newPoint);
		//Now we can attach additional data of any type if we want to.
		added.setData("I am point number:" + Integer.toString(id));
		//We can change some settings about our point
		added.setIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.point_icon));
		added.setAnchor(0.5f, 0.5f); //Anchor of the icon
		added.setSuperDraggable(true); //Drag this object on touch, no long hold necessary
	}
	
	private void AddPolygon(){
		//Look for a polygon with this id, if it returns null it means it doesn't exist
		if(map.getPolygonView("aPolygonId") == null){
			//Add a polygon to the map without the user drawing it
			List<LatLng> points = new ArrayList<LatLng>();
			points.add(new LatLng(40.45f,-86.915f));
			points.add(new LatLng(40.44f,-86.91f));
			points.add(new LatLng(40.43f,-86.90f));
			points.add(new LatLng(40.45f,-86.89f));

			String id = "aPolygonId"; //The id of the polygon is used to find it later, it can be any type but should be unique
			ATKPolygon polygon = new ATKPolygon(id, points);
			//This adds the polygon to the map so it should be visible after this
			ATKPolygonView added = map.addPolygon(polygon);
			//Lets change some settings on the polygon
			added.setLabel("Blue"); //Label that appears in the middle of the polygon
			added.setFillColor(1.0f, 0, 0, 255);
			
			//Add a click listener to this specific polygon
			added.setOnClickListener(new ATKPolygonClickListener(){
				@Override
				public boolean onPolygonClick(ATKPolygonView polygonView) {
					if(polygonView.getLabel() == null || polygonView.getLabel().contentEquals("Yellow") != true){
						polygonView.setFillColor(1.0f, 255, 255, 0); //Change the fill to Yellow
						polygonView.setLabel("Yellow"); //Change the label to Yellow
						return true; //Consume the click event
					}
					return false; //Don't consume the click event, so ATKMap's PolygonClickListener will be called
				}
			});
		}
	}
	
	private void UpdatePolygon(){
		//Change the points of the polygon that was added
		//Make array of new points
		List<LatLng> points = new ArrayList<LatLng>();
		points.add(new LatLng(40.45f,-86.915f));
		points.add(new LatLng(40.44f,-86.91f));
		points.add(new LatLng(40.43f,-86.97f));
		
		ATKPolygonView polygon = map.getPolygonView("aPolygonId"); //Find the polygon
		if(polygon != null){
			//Update the points			
			polygon.getAtkPolygon().boundary = points;
			polygon.update();
		}
		
		/* Another way to update the same polygon
		ATKPolygon polygon = new ATKPolygon("aPolygonId", points);
		map.updatePolygon(polygon); //Find and update the ATKPolygonView that matches this ATKPolygon
		*/
	}
	
	private void DrawPolygon(){
		//Allow the user to draw a polygon on the map
		int id = map.getPolygonViews().size(); //Get the number of points on the map at this time
		ATKPolygonView polygonBeingDrawn = map.drawPolygon(id);
		//Set some settings for what it should appear like when being drawn
		polygonBeingDrawn.setFillColor(0.7f, 0, 255, 0); //Opacity, Red, Green, Blue
	}
	
	private void ClosePolygon(){
		ATKPolygonView polygonComplete = map.completePolygon();
		polygonComplete.setFillColor(1.0f, 255, 0, 0); //Change fill so user knows drawing is complete
	}
	

	@Override
	public boolean onPointDrag(ATKPointView pointView) {
		return false;
	}

	@Override
	public boolean onPointDragEnd(ATKPointView pointView) {
		LatLng pos = pointView.getAtkPoint().position;
		Log.d("MainActivity", "Point with id: " + Integer.toString((Integer) pointView.getAtkPoint().id) + " dropped at-> lat:" + 
				Double.toString(pos.latitude) + " lng:" + Double.toString(pos.longitude));
		return true;
	}

	@Override
	public boolean onPointDragStart(ATKPointView pointView) {
		return false;
	}

	@Override
	public boolean onPointClick(ATKPointView pointView) {
		//As of April 8, 2014 in this example this function is never called because SuperDraggable is set to true on the points
		//so all clicks are interpreted as drags, I will fix this later.
		Log.d("MainActivity", "Point with id: " + Integer.toString((Integer) pointView.getAtkPoint().id) + " clicked.");
		return true;
	}

	@Override
	public void onMapClick(LatLng position) {
		Log.d("MainActivity", "Map clicked.");
	}

	@Override
	public boolean onPolygonClick(ATKPolygonView polygonView) {
		//In this example this function is called when a polygon is clicked on the map and
		//it's click event is not consumed by the click listener associated with that
		//specific polygon ie. (this is the catch all for polygons without an assigned click listener
		//or ones who have a click listener but the listener chose not to consume the click event)
		
		polygonView.setFillColor(1.0f, 0, 0, 255); //Change the fill to Blue
		polygonView.setLabel("Blue"); //Change the label to Blue
		
		return false;
	}

}
