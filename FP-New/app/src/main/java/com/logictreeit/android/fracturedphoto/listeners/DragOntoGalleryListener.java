package com.logictreeit.android.fracturedphoto.listeners;

import android.app.Activity;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;

import com.logictreeit.android.fracturedphoto.custom_ui.HorizontalListView;

//************** THIS CLASS IS NOT BEING USED **********************//
@SuppressWarnings("deprecation")
public class DragOntoGalleryListener implements OnDragListener {

	private HorizontalListView pv;

	//private Activity act;

	public DragOntoGalleryListener(Activity act, HorizontalListView pv) {
		this.pv = pv;
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			// ////Toast.makeText(act, "HELLO", 1).show();
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			break;
		case DragEvent.ACTION_DRAG_EXITED:
			break;
		// drag shadow has been released,the drag point is within the
		// bounding box of the View
		case DragEvent.ACTION_DROP:
			//.onTileDrop(getDraggedTileID(), getDroppedPositionOnScreen(event));
			break;
		// the drag and drop operation has concluded.
		case DragEvent.ACTION_DRAG_ENDED:
			break;
		default:
			break;
		}
		return true;
	}

	
}
