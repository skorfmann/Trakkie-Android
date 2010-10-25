package com.skorfmann.trakkie;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class TrackMe extends MapActivity {
	
	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private TrackMeItemizedOverlay itemizedoverlay;
	private List<Overlay> mapOverlays;
	
	private static String URL = "http://192.170.67.187:3000/waypoints/current";  
	
    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    
		// create a map view
		RelativeLayout linearLayout = (RelativeLayout) findViewById(R.id.mainlayout);
		mapView = (MapView) findViewById(R.id.mapview);
	
		
	    mapView.setBuiltInZoomControls(true);
	    mapView.setStreetView(true);
                
	    mapOverlays = mapView.getOverlays();
	    Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
	    itemizedoverlay = new TrackMeItemizedOverlay(drawable, this);

		mapController = mapView.getController();
		mapController.setZoom(14); // Zoon 1 is world view

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
				0, new GeoUpdateHandler());
	    
	    CallWebServiceTask task = new CallWebServiceTask();
	    task.applicationContext = TrackMe.this;
	    task.execute();
	    
	    final MyLocationOverlay myLocationOverlay = new MyLocationOverlay(this, mapView);
	    mapOverlays.add(myLocationOverlay);
        myLocationOverlay.enableCompass();
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mapController.animateTo(myLocationOverlay.getMyLocation());
            }
        });
        
	}
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    public static String getCurrentWaypoint() {
    	String response = null;
    	RestClient client = new RestClient(URL); 
        client.AddParam("format", "json");
        try
        {
            client.Execute(RequestMethod.GET);
        	response = client.getResponse();
        }
        catch (Exception e)
        {
          response = "error" + e.getLocalizedMessage();   
        }
        return response;
    }
    


	public static GeoPoint parseJSONResponse(String jsonResponse) {
		int lat = 0;
		int lng = 0;
		
		JSONObject json;
		try {
			json = new JSONObject(jsonResponse);
			JSONObject result = json.getJSONObject("waypoint");
			lat = (int) (result.getDouble("lat") * 1E6);
			lng = (int) (result.getDouble("lng") * 1E6);
		} catch (JSONException e) {

			e.printStackTrace();		
		}
		return new GeoPoint(lat, lng);
	}

	public void addMarker(GeoPoint point){
        OverlayItem overlayitem = new OverlayItem(point, "Generated", point.toString()); 
	    itemizedoverlay.addOverlay(overlayitem);
	    mapOverlays.add(itemizedoverlay);
	}

	public class GeoUpdateHandler implements LocationListener {

		public void onLocationChanged(Location location) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);

			GeoPoint point = new GeoPoint(lat,lng);
			mapController.animateTo(point); //	mapController.setCenter(point);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}
	
	

	public class CallWebServiceTask extends AsyncTask {
			private ProgressDialog dialog;
			protected Context applicationContext;

			@Override
			protected void onPreExecute() {
				this.dialog = ProgressDialog.show(applicationContext, "Locating Car", "get current Position...", true);
			}

			@Override
			protected String doInBackground(Object... params) {
				 Log.i("location develop", "do Background"); 
				return TrackMe.getCurrentWaypoint();

			}

			@Override
			protected void onPostExecute(Object result) {
				this.dialog.cancel();
				GeoPoint point = parseJSONResponse((String) result);
				TrackMe.this.addMarker(point);
			}


		}
	
	

}