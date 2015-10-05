package com.scottlogic.dmiley.battleship.gridview;

import android.view.GestureDetector;
import android.view.MotionEvent;

// Class to listen to motion event on custom view components.
public class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        return true;
    }
}
