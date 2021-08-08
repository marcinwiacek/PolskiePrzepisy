package com.mwiacek.polskie.przepisy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

public class ScrollerTextViewMain extends TextView {

	ScrollerTextViewMovable sv=null;
	
	public ScrollerTextViewMain(Context context) { 
		super( context);
	}
 
	public ScrollerTextViewMain(Context context, AttributeSet attrs) {
		super( context, attrs );
	}
 
	public ScrollerTextViewMain(Context context, AttributeSet attrs, int defStyle) {
		super( context, attrs, defStyle );
	}
	
	MotionEvent ev;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction()==MotionEvent.ACTION_DOWN) {	
			ev = event;
		}
        return super.onTouchEvent(event);
    }

	@Override
	public void computeScroll () {
		super.computeScroll();
		if (sv!=null && GetMax()!=0) {				
			sv.Showed=true;
			sv.y=(int)((sv.getHeight()-sv.myBitmap.getHeight())*computeVerticalScrollOffset ()/GetMax());
			sv.SetTimer();
		}		
	}
	
	int GetMax() {
		return computeVerticalScrollRange();//-computeVerticalScrollExtent ();
	}
}
