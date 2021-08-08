package com.mwiacek.polskie.przepisy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ScrollerWebViewMovable extends View {
	
	Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scroll1);
	Boolean Touched=false,Showed=false;
	int y=0;
	ScrollerWebViewMain webview;
	CountDownTimer ct=null;
	
	public void SetTimer() {
		if (ct!=null) {
			ct.cancel();
		}
		ct= new CountDownTimer(2000, 2000) {

		     public void onTick(long millisUntilFinished) {
		     }

		     public void onFinish() {
		    	 if (Showed || !Touched) {
		    		 Showed=false;
		    		 invalidate();
		    	 }
		     }
		  }.start();
		
	}
	public ScrollerWebViewMovable(Context context) { 
		super( context);
	}
 
	public ScrollerWebViewMovable(Context context, AttributeSet attrs) {
		super( context, attrs );
	}
 
	public ScrollerWebViewMovable(Context context, AttributeSet attrs, int defStyle) {
		super( context, attrs, defStyle );
	}
	
	@Override
	public void onDraw(Canvas canvas) {
        	//super.onDraw(canvas);

        	if (Showed) {
        		canvas.drawBitmap(myBitmap, 0, y, null);
        	}
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {

    	if (!Showed) return false;
    	
    	if (event.getAction() == MotionEvent.ACTION_DOWN) {
    		if ((int)event.getY()>=y && (int)event.getY()<=y+myBitmap.getHeight()) {
    			Touched = true;
    			return true;
    		}
    	} 

    	if (event.getAction() == MotionEvent.ACTION_MOVE) {
    		if (Touched) {
    			y=(int)event.getY();
    			if (y+myBitmap.getHeight()>webview.getHeight()) y=webview.getHeight()-myBitmap.getHeight();
    			if (y-myBitmap.getHeight()<0) y=0;
    			//this.invalidate();
    			webview.scrollTo(0, (int)((y*webview.GetMax()/(this.getHeight()-myBitmap.getHeight()))));
 //   			webview.loadUrl("javascript:window.scrollTo(0, "+((int)(y*webviewheight/(this.getHeight()-66)))+");");
    			return true;
    		}
    	} 
    	if (event.getAction() == MotionEvent.ACTION_UP) {
    		if (Touched) {
    			Touched=false;
    			SetTimer();
    			return true;
    		}
    	}
    	
    	return false;
    }
}
