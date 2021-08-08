package com.mwiacek.polskie.przepisy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

public class ScrollerWebViewMain extends WebView {

	ScrollerWebViewMovable sv=null;
	
	public ScrollerWebViewMain(Context context) { 
		super( context);
	}
 
	public ScrollerWebViewMain(Context context, AttributeSet attrs) {
		super( context, attrs );
	}
 
	public ScrollerWebViewMain(Context context, AttributeSet attrs, int defStyle) {
		super( context, attrs, defStyle );
	}
	
	/*@Override
	public void invokeZoomPicker ()
	{
		
	}
	
	@Override
	public boolean zoomIn ()
	{
		return super.zoomIn();
	}
	
	@Override
	public boolean zoomOut()
	{
		return super.zoomOut();
	}*/

	MotionEvent ev;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction()==MotionEvent.ACTION_DOWN) {
			ev = event;
		}
        return super.onTouchEvent(event);
    }

	@Override
	protected void onScrollChanged (int l, int t, int oldl, int oldt)
	{
		super.onScrollChanged(l,t,oldl,oldt);
		if (sv!=null) {
			sv.Showed=true;
			//loadUrl("javascript:Android.r(window.pageYOffset);");
//			sv.y=t*(this.getHeight()-66)/(this.getContentHeight()-this.getHeight());
			sv.y=(int)((this.getHeight()-sv.myBitmap.getHeight())*computeVerticalScrollOffset ()/GetMax());
			sv.SetTimer();
		}		
	}
	
	int GetMax() {
		return computeVerticalScrollRange()-computeVerticalScrollExtent ();
	}
}
