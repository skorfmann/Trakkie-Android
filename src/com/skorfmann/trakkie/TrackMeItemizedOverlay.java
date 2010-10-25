package com.skorfmann.trakkie;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class TrackMeItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private Context mContext;
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	public TrackMeItemizedOverlay(Drawable defaultMarker, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
	}
	
	
	@Override
	public int size() {
	  return mOverlays.size();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
	  return mOverlays.get(i);
	}
	
	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = mOverlays.get(index);
	  Intent i = new Intent(Intent.ACTION_VIEW,
			Uri.parse("google.navigation:q=" + item.routableAddress()));
	  mContext.startActivity(i); 
	  /*AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();*/
	  return true;
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
}
