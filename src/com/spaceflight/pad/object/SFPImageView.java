package com.spaceflight.pad.object;

import com.spaceflight.pad.activity.ImageBrowser;

import android.content.Context;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class SFPImageView extends ImageView {
	protected final static int NONE = 1;
	protected final static int ZOOM = NONE + 1;
	protected final static int DRAG = ZOOM + 1;
    
    private float mLastMotionX;
    private float mLastMotionY;
    
    private float beforeLenght;
    private float afterLenght;
    protected int mode = NONE;
    private float zoomValue = 1.0f;
    private int transValueX = 0;
    private int transValueY = 0;
    
	public SFPImageView(Context context) {
		super(context);
	}
	
	public SFPImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		Log.d("lilong","MyImageView : onTouchEvent()");
		final int action = ev.getAction();
		final float currentX = ev.getX();
		final float currentY = ev.getY();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN :{
			if(zoomValue > 1.0f){
				mode = DRAG;
			}
			mLastMotionX = currentX;
			mLastMotionY = currentY;
			break;
		}
		case MotionEvent.ACTION_MOVE :{
			if (mode == DRAG){
				transValueX += Math.round(currentX - mLastMotionX);
				transValueY += Math.round(currentY - mLastMotionY);
				setTranslationX(transValueX);
				setTranslationY(transValueY);
			}
			
			if(ev.getPointerCount() >= 2){
	        	if(mode != ZOOM){
	        		beforeLenght = spacing(ev);
	        		if(beforeLenght > 32){
	        			mode = ZOOM;
	        		}
	        	}
	        	
	        	if (mode == ZOOM){
					afterLenght = spacing(ev);
					float gapLenght = afterLenght - beforeLenght;                     
					if(gapLenght > 0){ 
                    	zoomValue += 0.05f;
                    	if(zoomValue >= 1.5f){
                    		zoomValue = 1.5f;
                    	}
                    }else{  
                    	zoomValue -= 0.05f;
                    	transValueX = (int)transValueX >> 2;
                    	transValueY = (int)transValueY >> 2;
                    	if(zoomValue <= 1.0f){
                    		zoomValue = 1.0f;
                    		setTranslationX(0);
            				setTranslationY(0);
                    	}
                    	setTranslationX(transValueX);
        				setTranslationY(transValueY);
                    	
                    } 
                    setScaleX(zoomValue);
					setScaleY(zoomValue);
                    beforeLenght = afterLenght; 
				}
	    	}
			mLastMotionX = currentX;
			mLastMotionY = currentY;
			break;
		}
		case MotionEvent.ACTION_UP :{
			if(mode == DRAG){
				int maxXValue = (int) ((ImageBrowser.mMetric.widthPixels * getScaleX() - ImageBrowser.mMetric.widthPixels)/2);
				int maxYValue = (int) ((ImageBrowser.mMetric.heightPixels * getScaleY() - ImageBrowser.mMetric.heightPixels)/2);
				
				if(transValueX > maxXValue){
					transValueX = maxXValue;
				}else if(transValueX < - maxXValue){
					transValueX = - maxXValue;
				}
				setTranslationX(transValueX);
				
				if(transValueY > maxYValue){
					transValueY = maxYValue;
				}else if(transValueY < - maxYValue){
					transValueY = -maxYValue;
				}
				setTranslationY(transValueY);
			}
			mode = NONE;
			break;
		}
		}
		return true;
	}
	
	
}
