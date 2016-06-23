package tu.tracking.system.gestures;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class OnTargetTouchListener implements OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnTargetTouchListener(Context ctx){
        gestureDetector = new GestureDetector(ctx, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        private static final long DOUBLE_PRESS_INTERVAL = 1000;
        private boolean mHasDoubleClicked = false;
        private long lastPressTime = 0;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight(e1);
                        } else {
                            onSwipeLeft(e1);
                        }
                    }
                    result = true;
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                }
                result = true;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }

//        @Override
//        public boolean onSingleTapUp(MotionEvent e) {
//            long pressTime = System.currentTimeMillis();
//            final MotionEvent fe = e;
//
//            // If double click...
//            if (pressTime - lastPressTime <= DOUBLE_PRESS_INTERVAL) {
//                onDoubleTapEvent(e);
//                mHasDoubleClicked = true;
//            }
//            else {     // If not double click....
//                mHasDoubleClicked = false;
//                Handler myHandler = new Handler() {
//                    public void handleMessage(Message m) {
//                        if (!mHasDoubleClicked) {
//                            onClick(fe);
//                        }
//                    }
//                };
//                Message m = new Message();
//                myHandler.sendMessageDelayed(m, DOUBLE_PRESS_INTERVAL);
//            }
//            // record the last time the menu button was pressed.
//            lastPressTime = pressTime;
//            return true;
//        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            onDoubleTapEv(e);
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            onClick(e);
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            onLongPressEvent(e);
            super.onLongPress(e);
        }
    }

    public abstract void onClick(MotionEvent e);

    public abstract void onDoubleTapEv(MotionEvent e);

    public abstract void onLongPressEvent(MotionEvent e);

    public abstract void onSwipeRight(MotionEvent e);

    public abstract void onSwipeLeft(MotionEvent e);

    public abstract void onSwipeTop();

    public abstract void onSwipeBottom();
}