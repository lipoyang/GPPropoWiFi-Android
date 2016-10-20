/*
 * Copyright (C) 2014 Bizan Nishimura (@lipoyang)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lipoyang.gppropowifi;

//import java.util.HashMap;
import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
//import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.content.res.Resources;
import android.view.MotionEvent;

public class PropoView extends View {
	// screen size of the original design
	private final float W_SCREEN = 1184;
	private final float H_SCREEN = 720;
	// Bluetooth button size
	private float W_BT_BUTTON = 240;
	private float H_BT_BUTTON = 106;
	// Bluetooth button base point (left-top) 
	private float X_BT_BUTTON = W_SCREEN/2 - W_BT_BUTTON/2;
	private float Y_BT_BUTTON = 54;
	// Setting button size
	private float W_SET_BUTTON = 150;
	private float H_SET_BUTTON = 150;
	// Setting button base point (left-top)
	private float X_SET_BUTTON = W_SCREEN/2 - W_SET_BUTTON/2;
	private float Y_SET_BUTTON = 530;
	// F<->B bar radius and length of movement (half)
	private float R_FB_BAR = 42;
	private float L_FB_BAR = 173; //(range/2 - radius/2)
	// F<->B bar neutral point
	private float X_FB_BAR = 296;
	private float Y_FB_BAR = 377;
	// L<->R bar radius and length of movement (half)
	private float R_LR_BAR = 42;
	private float L_LR_BAR = 173; //(range/2 - radius/2)
	// L<->R bar neutral point
	private float X_LR_BAR = 888;
	private float Y_LR_BAR = 377;
	// margin of bar touch range
	private float MARGIN_BAR = 50;
	
	// touch points
    //private SparseArray<Point> points =new SparseArray<Point>();
    
    // ID of touch point
	private int fbID = -1;	// touch on F<->B bar
	private int lrID = -1;	// touch on L<->R bar
	private int btID = -1;	// touch on Bluetooth button
	private int setID = -1;	// touch on Setting button
    
    // position of F<->B bar, L<->R bar
    private float fb_y;
    private float lr_x;

    // Bluetooth state
    private WiFiStatus btState = WiFiStatus.DISCONNECTED;
    
    // bitmap objects
	private Bitmap imgBar, imgDisconnected, imgConnecting, imgConnected, imgSetting;
	private Paint paint;
	
	// event listener
	private PropoListener parent;
	
	// constructors
	public PropoView(Context context, AttributeSet attrs ) {
		super(context,attrs);
		init();
	}
	public PropoView(Context context) {
		super(context);
		init();
	}
	void init(){
		Resources res = this.getContext().getResources();
		imgDisconnected = BitmapFactory.decodeResource(res, R.drawable.disconnected);
		imgConnecting   = BitmapFactory.decodeResource(res, R.drawable.connecting);
		imgConnected    = BitmapFactory.decodeResource(res, R.drawable.connected);
		imgBar          = BitmapFactory.decodeResource(res, R.drawable.bar);
		imgSetting     = BitmapFactory.decodeResource(res, R.drawable.setting);

		paint = new Paint();
	}
	
	// set main activity
	public void setParent(PropoListener listener, Activity activity) {
		
		parent = listener;
		
		// get screen information
		WindowManager wm = (WindowManager)activity.getBaseContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);
		
		// scale factor
		float xScale = displayMetrics.widthPixels / W_SCREEN;
		float yScale = displayMetrics.heightPixels / H_SCREEN;
		
		// resize bitmap images
		Bitmap img1 = imgDisconnected;
		Bitmap img2 = imgConnecting;
		Bitmap img3 = imgConnected;
		Bitmap img4 = imgBar;
		Bitmap img5 = imgSetting;
		Matrix matrix = new Matrix();
		float rsz_ratio_w = 84.0f / img4.getWidth()  * xScale;
		float rsz_ratio_h = 84.0f / img4.getHeight() * yScale;
		matrix.postScale( rsz_ratio_w, rsz_ratio_h );
		imgDisconnected = Bitmap.createBitmap(img1, 0, 0, img1.getWidth(), img1.getHeight(), matrix, true);
		imgConnecting   = Bitmap.createBitmap(img2, 0, 0, img2.getWidth(), img2.getHeight(), matrix, true);
		imgConnected    = Bitmap.createBitmap(img3, 0, 0, img3.getWidth(), img3.getHeight(), matrix, true);
		imgBar          = Bitmap.createBitmap(img4, 0, 0, img4.getWidth(), img4.getHeight(), matrix, true);
		imgSetting     = Bitmap.createBitmap(img5, 0, 0, img5.getWidth(), img5.getHeight(), matrix, true);
		
		// Bluetooth button size
		W_BT_BUTTON = imgDisconnected.getWidth();
		H_BT_BUTTON = imgDisconnected.getHeight();
		// Bluetooth button base point (left-top) 
		X_BT_BUTTON = displayMetrics.widthPixels/2 - W_BT_BUTTON/2;
		Y_BT_BUTTON *= yScale;
		// Setting button size
		W_SET_BUTTON = imgSetting.getWidth();
		H_SET_BUTTON = imgSetting.getHeight();
		// Setting button base point (left-top)
		X_SET_BUTTON = displayMetrics.widthPixels/2 - W_SET_BUTTON/2;
		Y_SET_BUTTON *= yScale;
		// F<->B bar radius and length of movement (half)
		R_FB_BAR *= xScale;
		L_FB_BAR *= yScale; //(range/2 - radius/2)
		// F<->B bar neutral point
		X_FB_BAR *= xScale;
		Y_FB_BAR *= yScale;
		// L<->R bar radius and length of movement (half)
		R_LR_BAR *= yScale;
		L_LR_BAR *= xScale; //(range/2 - radius/2)
		// L<->R bar neutral point
		X_LR_BAR *= xScale;
		Y_LR_BAR *= yScale;
		// margin of bar touch range
		MARGIN_BAR *= xScale;
		
		// initial position of sticks
	    fb_y = Y_FB_BAR;
	    lr_x = X_LR_BAR;
	}
	
	// set Bluetooth State
	public void setBtStatus(WiFiStatus state)
	{
		btState = state;
		invalidate();	// redraw
	}
	
	@Override
	protected void onDraw(Canvas c) {
		super.onDraw(c);
		
		// draw bitmap objects
		
		// Bluetooth button
		switch(btState){
		case CONNECTING:
			c.drawBitmap(imgConnecting,X_BT_BUTTON,Y_BT_BUTTON,paint);
			break;
		case CONNECTED:
			c.drawBitmap(imgConnected,X_BT_BUTTON,Y_BT_BUTTON,paint);
			break;
		case DISCONNECTED:
		default:
			c.drawBitmap(imgDisconnected,X_BT_BUTTON,Y_BT_BUTTON,paint);
			break;
		}
		// Setting Button
		c.drawBitmap(imgSetting,X_SET_BUTTON,Y_SET_BUTTON,paint);

		// F<->B bar
        c.drawBitmap(imgBar,X_FB_BAR - R_FB_BAR, fb_y     - R_FB_BAR, paint);
		// L<->R bar
        c.drawBitmap(imgBar,lr_x     - R_FB_BAR, Y_LR_BAR - R_FB_BAR, paint);
	}
	
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	
    	// get touch informations
        int action = event.getAction();
        // int index = (action & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
        int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        int eventID = event.getPointerId(index);
        int touchCount = event.getPointerCount();
 
        switch ( action & MotionEvent.ACTION_MASK ) {
 
        // (1) on touch-down
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
        	
        	// get the touch point
            int tx =(int)event.getX(index);
            int ty =(int)event.getY(index);
            Point posTouch = new Point(tx, ty);
            
			// (1.1) touch F<->B bar?
			if(fbID == -1) {
				if( (tx > (X_FB_BAR - R_FB_BAR - MARGIN_BAR)) &&
					(tx < (X_FB_BAR + R_FB_BAR + MARGIN_BAR)) &&
					(ty > (Y_FB_BAR - L_FB_BAR - R_FB_BAR - MARGIN_BAR*2)) &&
					(ty < (Y_FB_BAR + L_FB_BAR + R_FB_BAR + MARGIN_BAR*2)))
				{
					fbID = eventID;

					// message to the main activity
					//   F<->B value (-1.0 ... +1.0)
					fb_y = ty;
					if(fb_y < Y_FB_BAR - L_FB_BAR) fb_y = Y_FB_BAR - L_FB_BAR;
					if(fb_y > Y_FB_BAR + L_FB_BAR) fb_y = Y_FB_BAR + L_FB_BAR;
					float fb = -(fb_y - Y_FB_BAR) / L_FB_BAR;
					parent.onTouchFbStick(fb);
				}
			}
			// (1.2) touch L<->R bar?
			if(lrID == -1) {
				if( (tx > (X_LR_BAR - L_LR_BAR - R_LR_BAR - MARGIN_BAR*2)) &&
					(tx < (X_LR_BAR + L_LR_BAR + R_LR_BAR + MARGIN_BAR*2)) &&
					(ty > (Y_LR_BAR - R_LR_BAR - MARGIN_BAR)) &&
					(ty < (Y_LR_BAR + R_LR_BAR + MARGIN_BAR)))
				{
					lrID = eventID;

					// message to the main activity
					//   L<->R value (-1.0 ... +1.0)
					lr_x = tx;
					if(lr_x < X_LR_BAR - L_LR_BAR) lr_x = X_LR_BAR - L_LR_BAR;
					if(lr_x > X_LR_BAR + L_LR_BAR) lr_x = X_LR_BAR + L_LR_BAR;
					float lr = (lr_x - X_LR_BAR) / L_LR_BAR;
					parent.onTouchLrStick(lr);
				}
			}
			// (1.3) touch Bluetooth button?
			if(btID == -1){
				if( (tx >= (X_BT_BUTTON )) &&
					(tx <= (X_BT_BUTTON + W_BT_BUTTON)) &&
					(ty >= (Y_BT_BUTTON )) &&
					(ty <= (Y_BT_BUTTON + H_BT_BUTTON)))
				{
					btID = eventID;
				}
			}
			// (1.4) touch Setting button?
			if(setID == -1){
				if( (tx >= (X_SET_BUTTON )) &&
						(tx <= (X_SET_BUTTON + W_SET_BUTTON)) &&
						(ty >= (Y_SET_BUTTON )) &&
						(ty <= (Y_SET_BUTTON + H_SET_BUTTON)))
				{
					setID = eventID;
				}
			}
            break;
        
        // (2) on touch-move
        case MotionEvent.ACTION_MOVE:
 
            for(index = 0; index < touchCount; index++) {
 
            	// get the touch point
                eventID = event.getPointerId(index);
                tx =(int)event.getX(index);
                ty =(int)event.getY(index);
                posTouch = new Point(tx, ty);
 
				// (2.1) move F<->B bar?
				if(eventID == fbID)
				{
					if( (tx > (X_FB_BAR - R_FB_BAR - MARGIN_BAR)) &&
						(tx < (X_FB_BAR + R_FB_BAR + MARGIN_BAR)) &&
						(ty > (Y_FB_BAR - L_FB_BAR - R_FB_BAR - MARGIN_BAR*2)) &&
						(ty < (Y_FB_BAR + L_FB_BAR + R_FB_BAR + MARGIN_BAR*2)))
					{
						fb_y = ty;
						if(fb_y < Y_FB_BAR - L_FB_BAR) fb_y = Y_FB_BAR - L_FB_BAR;
						if(fb_y > Y_FB_BAR + L_FB_BAR) fb_y = Y_FB_BAR + L_FB_BAR;
					}else{
						fbID = -1;
						fb_y = Y_FB_BAR;
					}
					// message to the main activity
					//   F<->B value (-1.0 ... +1.0)
					float fb = -(fb_y - Y_FB_BAR) / L_FB_BAR;
					parent.onTouchFbStick(fb);
				}
				// (2.2) move L<->R bar
				else if(eventID == lrID)
				{
					if( (tx > (X_LR_BAR - L_LR_BAR - R_LR_BAR - MARGIN_BAR*2)) &&
						(tx < (X_LR_BAR + L_LR_BAR + R_LR_BAR + MARGIN_BAR*2)) &&
						(ty > (Y_LR_BAR - R_LR_BAR - MARGIN_BAR)) &&
						(ty < (Y_LR_BAR + R_LR_BAR + MARGIN_BAR)))
					{
						lr_x = tx;
						if(lr_x < X_LR_BAR - L_LR_BAR) lr_x = X_LR_BAR - L_LR_BAR;
						if(lr_x > X_LR_BAR + L_LR_BAR) lr_x = X_LR_BAR + L_LR_BAR;
					}else{
						lrID = -1;
						lr_x = X_LR_BAR;
					}
					// message to the main activity
					//   L<->R value (-1.0 ... +1.0)
					float lr = (lr_x - X_LR_BAR) / L_LR_BAR;
					parent.onTouchLrStick(lr);
				}
            }
            break;
 
        // (3) on touch-up
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
 
			// (3.1) leave F<->B bar?
			if(eventID == fbID) {
				fb_y = Y_FB_BAR;
				fbID = -1;

				// message to the main activity
				//   L<->R value (-1.0 ... +1.0)
				float fb = -(fb_y - Y_FB_BAR) / L_FB_BAR;
				parent.onTouchFbStick(fb);
			}
			// (3.2) leave L<->R bar?
			else if(eventID == lrID) {
				lr_x = X_LR_BAR;
				lrID = -1;

				// message to the main activity
				//   L<->R value (-1.0 ... +1.0)
				float lr = (lr_x - X_LR_BAR) / L_LR_BAR;
				parent.onTouchLrStick(lr);
			}
			// (3.3) leave Bluetooth button?
			else if(eventID == btID){
				btID = -1;

				// message to the main activity
				parent.onTouchBtButton();
			}
			// (3.4) leave Setting button?
			else if(eventID == setID){
				setID = -1;

				// message to the main activity
				parent.onTouchSetButton();
			}
            break;
        }
        invalidate(); // redraw
        return true;
    }
}
