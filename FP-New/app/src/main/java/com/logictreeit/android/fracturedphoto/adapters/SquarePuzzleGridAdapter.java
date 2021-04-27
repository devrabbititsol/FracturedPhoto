package com.logictreeit.android.fracturedphoto.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dran.fracturedphoto.R;
import com.logictreeit.android.fracturedphoto.activities.PlaySquarePuzzleActivity;
import com.logictreeit.android.fracturedphoto.custom_ui.RotateLayout;
import com.logictreeit.android.fracturedphoto.db.FracturePhotoDB;
import com.logictreeit.android.fracturedphoto.helpers.ImageDragShadowBuilder;
import com.logictreeit.android.fracturedphoto.models.SquareTile;
import com.logictreeit.android.fracturedphoto.utils.Utils;

public class SquarePuzzleGridAdapter extends BaseAdapter implements OnTouchListener, OnDragListener {

    public RotateLayout congratsParentLayout;
    private PlaySquarePuzzleActivity activity;
    private HashMap<View, Integer> hashMap = new HashMap<View, Integer>();
    private Integer touchedItemPosition;
    private Integer dropedItemPosition;
    private ArrayList<SquareTile> tilesList;
    private boolean isCreatePuzzleMode;
    private String pzlRowId;
    private Dialog congratsDialog;
    
    public SquarePuzzleGridAdapter(PlaySquarePuzzleActivity act, ArrayList<SquareTile> tilesList, boolean isCreatePuzzleMode, String pzlRowId, RotateLayout congratsParentLayout) {
        this.activity = act;
        this.tilesList = tilesList;
        this.isCreatePuzzleMode = isCreatePuzzleMode;
        this.pzlRowId = pzlRowId;
        this.congratsParentLayout = congratsParentLayout;
    }
    
    @Override
    public int getCount() {
        return tilesList.size();
    }
    
    @Override
    public Object getItem(int pos) {
        return tilesList.get(pos);
    }
    
    @Override
    public long getItemId(int pos) {
        return pos;
    }
    
    @Override
    public View getView(int pos, View v, ViewGroup parent) {
        ImageView img = new ImageView(activity);
        
        SquareTile tile = tilesList.get(pos);
        tile.setCurrentPosition(pos);
        img.setLayoutParams(new GridView.LayoutParams(tilesList.get(pos).getWidth(), tilesList.get(pos).getHeight()));
        img.setPadding(1, 1, 1, 1);
        img.setImageBitmap(tile.getBitmap());
        int rotation = tile.getRotation();
        img.setRotation(rotation * 90);
        
        img.setOnTouchListener(this);
        img.setOnDragListener(this);
        hashMap.put(img, pos);
        
        return img;
    }
    
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        activity.setGridTouched(true);
        SquareTile tile = null;
        ClipData data = ClipData.newPlainText("", "");
        
        if (hashMap.containsKey(v)) {
            touchedItemPosition = hashMap.get(v);
            activity.setTouchedItemPos(touchedItemPosition);
            tile = tilesList.get(touchedItemPosition);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.startDrag(data, ImageDragShadowBuilder.fromBitmap(activity, tile.getBitmap(), (int) v.getRotation()), v, 0);
                v.setVisibility(View.INVISIBLE);
                break;
            case MotionEvent.ACTION_MOVE:
                v.setVisibility(View.VISIBLE);
                break;
            case MotionEvent.ACTION_CANCEL:
                v.setVisibility(View.VISIBLE);
                break;
            case MotionEvent.ACTION_UP:
                v.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        return true;
    }
    
    @Override
    public boolean onDrag(View v, DragEvent event) {
        dropedItemPosition = hashMap.get(v);
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                break;
            case DragEvent.ACTION_DROP:
                swapItemPositions(dropedItemPosition, touchedItemPosition);
                activity.setTouchedItemPos(dropedItemPosition);
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                break;
            default:
                break;
        }
        return true;
    }
    
    void swapItemPositions(int current, int target) {
        SquareTile tempBitmap = tilesList.get(current);
        tilesList.set(current, tilesList.get(target));
        tilesList.set(target, tempBitmap);
        notifyDataSetChanged();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPuzzleSolved()) {
                    showPuzzleSolvedDialogBox();
                    Utils.doPlaySound(activity, R.raw.congratulations_audio);
                }
            }
        }, 200);
    }
    
    public boolean isPuzzleSolved() {
        for (SquareTile tile : tilesList) {
            if (tile.getCurrentPosition() != tile.getTileOriginalPosition() || tile.getRotation() != 0) {
                return false;
            }
        }
        return true;
    }
    
    public void showPuzzleSolvedDialogBox() {
        /*Builder alertDialogBox = new Builder(activity);
		alertDialogBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (!isCreatePuzzleMode && pzlRowId != null){
					FracturePhotoDB db = new FracturePhotoDB(activity.getApplicationContext());
					db.openDB();
					db.closeDB();
				}
			}
		});
		alertDialogBox.setTitle("Congratulations!");
		alertDialogBox.setMessage("You Solved It");
		alertDialogBox.setCancelable(false);
		alertDialogBox.show();*/
    
        congratsDialog = new Dialog(activity);
        congratsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        congratsDialog.setCancelable(false);
        congratsDialog.setCanceledOnTouchOutside(false);
        congratsDialog.setContentView(R.layout.congratulations_alert);
        congratsParentLayout = (RotateLayout) congratsDialog.findViewById(R.id.congratsParentLayout);
        congratsDialog.show();
        congratsParentLayout.setAngle(PlaySquarePuzzleActivity.rotateTo);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (congratsDialog != null && congratsDialog.isShowing()) {
                    congratsDialog.dismiss();
                }
            }
        }, 9000);
        startCongratsTimer();
    }
    
    Timer congratsTimer;
    TimerTask congratsTimerTask;
    final Handler congratsHandler = new Handler();
    
    public void startCongratsTimer() {
        congratsTimer = new Timer();
        congratsBlinkingCount = 0;
        initializeCongratsTimerTask();
        congratsTimer.schedule(congratsTimerTask, 100, 500); //
    }
    
    public void stopCongratsTimerTask() {
        if (congratsTimer != null) {
            congratsTimer.cancel();
            congratsTimer = null;
            congratsBlinkingCount = 0;
        }
        if (congratsDialog != null && congratsDialog.isShowing()) {
            congratsDialog.setCancelable(true);
            congratsDialog.setCanceledOnTouchOutside(true);
        }
    }
    
    int congratsBlinkingCount = 0;
    
    public void initializeCongratsTimerTask() {
        
        congratsTimerTask = new TimerTask() {
            public void run() {
                congratsHandler.post(new Runnable() {
                    public void run() {
                        if (congratsBlinkingCount > 7) {
                            stopCongratsTimerTask();
                            return;
                        }
                        if (congratsDialog != null && congratsDialog.isShowing()) {
                            RotateLayout parent = (RotateLayout) congratsDialog.findViewById(R.id.congratsParentLayout);
                            TextView label = (TextView) congratsDialog.findViewById(R.id.congratsLable);
                            
                            if (congratsBlinkingCount % 2 == 1) {
                                label.setTextColor(Color.BLACK);
                                parent.setBackgroundResource(R.drawable.bg_black_border_of_congrats);
                            } else {
                                label.setTextColor(Color.RED);
                                parent.setBackgroundResource(R.drawable.bg_red_border_of_congrats);
                            }
                            ++congratsBlinkingCount;
                        } else {
                            stopCongratsTimerTask();
                        }
                    }
                });
            }
        };
    }
    
}
