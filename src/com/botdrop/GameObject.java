package com.botdrop;

import android.graphics.Bitmap;

public class GameObject {
	
	/**
	 * dirY = DIR_UP should decrement the y coordinate
	 * 		(moving sprite towards top of screen)
	 * 
	 * dirY = DIR_DOWN should increment the y coordinate
	 * 		(moving sprite towards bottom of screen)
	 */
	public static final int DIR_DOWN = 1;
	public static final int DIR_UP = -1;
	public int directionY = DIR_DOWN;
	
	public static final int DIR_LEFT = -1;
	public static final int DIR_RIGHT = 1;
	public int directionX = DIR_RIGHT;
	
	public int dx, dy;
	
	public float velocity = 1.0f;
	
	// Internally, the x & y coords are stored such that
	// their registration point is in the center of the bitmap.
	// The getters return points registered in upper-left.
	public float x = 100f, y, lastX, lastY;
	
	public Bitmap bitmap;
	
	public GameObject(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	public void setY(int pos) {
		this.y = pos - bitmap.getHeight() / 2;
	}
	
	public void setX(int pos) {
		this.x = pos - bitmap.getWidth() / 2;
	}
	
	public void switchXDirection() {
		if (this.directionX == GameObject.DIR_LEFT) {
			this.directionX = GameObject.DIR_RIGHT;
		} else {
			this.directionX = GameObject.DIR_LEFT;
		}
	}
	
	public void switchYDirection() {
		if (this.directionY == GameObject.DIR_UP) {
			this.directionY = GameObject.DIR_DOWN;
		} else {
			this.directionY = GameObject.DIR_UP;
		}
	}

	
	@Override
	public String toString() {
		return "( " + this.x + ", " + this.y + ")";
	}
}
