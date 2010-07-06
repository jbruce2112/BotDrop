package com.botdrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import java.util.*;

public class BotDropView extends SurfaceView implements SurfaceHolder.Callback {	
	
	private BotDropThread thread;
	private List<GameObject> gameObjects = new ArrayList<GameObject>();
	private GameObject mPlayerGameObject;
	private static boolean isPlayerTouched = false;
	
	private static long lastTouchedTimer;
	
	// num of pixels that trigger a change in veloctiy
	public static final int POS_DELTA_THRESHOLD = 75;
	
	// num millis that pixel delta must occur in to trigger velocity change
	public static final int VELOCITY_DELTA_THRESHOLD = 750;	
	
	// max time to allow pixel difference to modify velocity
	public static final int TIMER_MAX_MILLIS = 1500;
	
	// max pixels allowed to move per frame
	public static final int SPEED_LIMIT = 20;
	
	public BotDropView(Context c) {
		super(c);
		// register with SurfaceHolder to receive callbacks
		getHolder().addCallback(this);
		setUpGameObjects();
		
		thread = new BotDropThread(getHolder(), this);
		setFocusable(true);		
	}
	
	public void setUpGameObjects() {
		final Bitmap botBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.android_bot);
		mPlayerGameObject = new GameObject(botBitmap);
		gameObjects.add(mPlayerGameObject);
	}
	
	class BotDropThread extends Thread {
		
		public SurfaceHolder surfaceHolder;
		private BotDropView botDropView;
		private boolean run = false;
		
		public BotDropThread(SurfaceHolder holder, BotDropView view) {
			surfaceHolder = holder;
			botDropView = view;
		}
		
		public void setRunning(boolean r) {
			run = r;
		}
		
		@Override
		public void run() {
			Canvas canvas;
			while (run) {
				canvas = null;
				canvas = surfaceHolder.lockCanvas(null);
				try {
					updatePosition();					
					synchronized (surfaceHolder) {
						botDropView.onDraw(canvas);
					}
				}
				finally {
					if (canvas != null) {
						// display to screen and reset state
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		GameObject o = mPlayerGameObject;
		
		synchronized (thread.surfaceHolder) {

			if (e.getAction() == MotionEvent.ACTION_UP) {
				
				o.dx = (int) Math.abs(o.x - o.lastX);
				o.dy = (int) Math.abs(o.y - o.lastY);
				
				if (System.currentTimeMillis() - lastTouchedTimer > TIMER_MAX_MILLIS) {
					lastTouchedTimer = System.currentTimeMillis();	//reset
				}
				else if (o.velocity < SPEED_LIMIT) {
					if (o.dx / POS_DELTA_THRESHOLD >= 1) {
						o.velocity += o.dx / (POS_DELTA_THRESHOLD * 3.5f);
					} else if (o.dy / POS_DELTA_THRESHOLD >= 1) {
						o.velocity += o.dy / (POS_DELTA_THRESHOLD * 3.5f);
					}
				}
				
				//check for direction change
				
				if (o.lastY > o.y) {
					Log.i("", "SHOULD GO UP NOW");
					o.directionY = GameObject.DIR_UP;
				} else {
					o.directionY = GameObject.DIR_DOWN;
				}
				
				if (o.lastX > o.x) {
					o.directionX = GameObject.DIR_LEFT;
				} else {
					o.directionX = GameObject.DIR_RIGHT;
				}
				isPlayerTouched = false;
			}
			else if (e.getAction() == MotionEvent.ACTION_DOWN) {				
				o.lastX = o.x;
				o.lastY = o.y;
				isPlayerTouched = true;
			}
			
			checkScreenBorders(o);
			o.setX((int) e.getX());
			o.setY((int) e.getY());
			
			return true;	//bs
		}
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		for (GameObject sprite : gameObjects) {
			
			canvas.drawColor(Color.BLACK);
			if (sprite != null) {
				Paint p = new Paint();
				p.setColor(Color.WHITE);
				canvas.drawText("("+sprite.directionX + ", " +  sprite.directionY +")", 
								50, 50, p);
			}
			canvas.drawBitmap(sprite.bitmap, 
							  sprite.x, 
							  sprite.y, null);
		}
	}
	
	public void updatePosition() {
		for (GameObject sprite : gameObjects) {
			
			checkScreenBorders(sprite);
			
			// DIRECITON
			if (!isPlayerTouched) {
				sprite.x += sprite.velocity * sprite.directionX;
				sprite.y += sprite.velocity * sprite.directionY;
			}
		}
	}
	
	public void checkScreenBorders(GameObject o) {
		if (o.x < 0 || 
			o.x + o.bitmap.getWidth() > this.getWidth()) {
			o.switchXDirection();
		}
		
		if (o.y < 0 || 
			o.y + o.bitmap.getHeight() > this.getHeight()) {
			o.switchYDirection();
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		thread.setRunning(true);
		thread.start();
		
	}
	
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// simply copied from sample application LunarLander:
	    // we have to tell thread to shut down & wait for it to finish, or else
	    // it might touch the Surface after we return and explode
	    boolean retry = true;
	    thread.setRunning(false);
	    while (retry) {
	        try {
	            thread.join();
	            retry = false;
	        } catch (InterruptedException e) {
	            // we will try it again and again...
	        }
	    }
	}
}
