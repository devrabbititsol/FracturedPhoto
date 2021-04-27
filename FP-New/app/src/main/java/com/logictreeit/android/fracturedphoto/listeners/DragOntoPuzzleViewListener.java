package com.logictreeit.android.fracturedphoto.listeners;

import android.app.Activity;
import android.graphics.Point;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import com.logictreeit.android.fracturedphoto.adapters.ShatteredPuzzleGalleryAdapter;
import com.logictreeit.android.fracturedphoto.custom_ui.ShatteredPuzzleView;

public class DragOntoPuzzleViewListener implements OnDragListener {

	private ShatteredPuzzleView puzzleView;

	public DragOntoPuzzleViewListener(Activity act, ShatteredPuzzleView puzzleView) {
		this.puzzleView = puzzleView;
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				break;
			// drag shadow has been released,the drag point is within the
			// bounding box of the View
			case DragEvent.ACTION_DROP:
				puzzleView.onTileDropFromGallery(getDraggedTileID(), getDroppedPositionOnScreen(event));
				break;
			// the drag and drop operation has concluded.
			case DragEvent.ACTION_DRAG_ENDED:
				break;
			case DragEvent.ACTION_DRAG_LOCATION:
				break;
			default:
				break;
		}
		return true;
	}

	private Point getDroppedPositionOnScreen(DragEvent event) {
		Point p = new Point();
		p.set((int) event.getX(), (int) event.getY());
		return p;
	}

	private int getDraggedTileID() {
		return ShatteredPuzzleGalleryAdapter.draggingTileID;
	}
}
