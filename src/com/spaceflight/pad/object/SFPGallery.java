package com.spaceflight.pad.object;

import java.io.File;
import java.util.LinkedList;

import com.spaceflight.pad.SFPApplication;
import com.spaceflight.pad.activity.FolderBrowser;
import com.spaceflight.pad.filebrowser.FileUtil;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Scroller;

public class SFPGallery extends AdapterView<Adapter> {
    private static final String TAG = "lilong";
	private static final int SNAP_VELOCITY = 1000;
	private static final int INVALID_SCREEN = -1;
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = TOUCH_STATE_REST + 1;
	private final static int TOUCH_STATE_DISPATCH = TOUCH_STATE_SCROLLING + 1;

	private LinkedList<View> mLoadedViews;
	private int mCurrentBufferIndex;
	private int mCurrentAdapterIndex;
	private int mSideBuffer = 1;
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private int mTouchState = TOUCH_STATE_REST;
	private float mLastMotionX;
	private int mTouchSlop;
	private int mMaximumVelocity;
	private int mCurrentScreen;
	private int mNextScreen = INVALID_SCREEN;
	private boolean mFirstLayout = true;
	private ViewSwitchListener mViewSwitchListener;
	private Adapter mAdapter;
	private int mLastScrollDirection;
	private AdapterDataSetObserver mDataSetObserver;
	private int mLastOrientation = -1;
	
	private boolean beginScale;
	private GestureDetector mGestureDetector;
	private final ScaleGestureDetector mScaleDetector;

	private OnGlobalLayoutListener orientationChangeListener = new OnGlobalLayoutListener() {

		@Override
		public void onGlobalLayout() {
			getViewTreeObserver().removeGlobalOnLayoutListener(
					orientationChangeListener);
			setSelection(mCurrentAdapterIndex);
		}
	};

	/**
	 * Receives call backs when a new {@link View} has been scrolled to.
	 */
	public static interface ViewSwitchListener {

		/**
		 * This method is called when a new View has been scrolled to.
		 * 
		 * @param view
		 *            the {@link View} currently in focus.
		 * @param position
		 *            The position in the adapter of the {@link View} currently in focus.
		 */
		void onSwitched(View view, int position);

	}

	public SFPGallery(Context context) {
		super(context);
		mScaleDetector = new ScaleGestureDetector(context, new MyScaleListener());
		mGestureDetector = new GestureDetector(context,new OnDoubleClick());
		
//		mSideBuffer = 3;
		mSideBuffer = 1;
		init();
	}

	public SFPGallery(Context context, int sideBuffer) {
		super(context);
		mScaleDetector = new ScaleGestureDetector(context, new MyScaleListener());
		mGestureDetector = new GestureDetector(context,new OnDoubleClick());
		
//		mSideBuffer = sideBuffer;
		mSideBuffer = 1;
		init();
	}

	public SFPGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScaleDetector = new ScaleGestureDetector(context, new MyScaleListener());
		mGestureDetector = new GestureDetector(context,new OnDoubleClick());
		
//		TypedArray styledAttrs = context.obtainStyledAttributes(attrs,R.styleable.ViewFlow);
//		mSideBuffer = styledAttrs.getInt(R.styleable.ViewFlow_sidebuffer, 3);
		mSideBuffer = 1;
		init();
	}

	private void init() {
		mLoadedViews = new LinkedList<View>();
		mScroller = new Scroller(getContext());
		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		mTouchSlop = 12;
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		if (newConfig.orientation != mLastOrientation) {
			mLastOrientation = newConfig.orientation;
			getViewTreeObserver().addOnGlobalLayoutListener(orientationChangeListener);
		}
	}

	public int getViewsCount() {
		return mAdapter.getCount();
	}
	
	public void clearBuf(){
		if(mLoadedViews != null){
			mLoadedViews.clear();
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY && !isInEditMode()) {
			throw new IllegalStateException(
					"ViewFlow can only be used in EXACTLY mode.");
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY && !isInEditMode()) {
			throw new IllegalStateException(
					"ViewFlow can only be used in EXACTLY mode.");
		}

		// The children are given the same width and height as the workspace
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		if (mFirstLayout) {
			mScroller.startScroll(0, 0, mCurrentScreen * width, 0, 0);
			mFirstLayout = false;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth,
						child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}

	private SFPImageView mCurrentAdapterView;
	
	@SuppressWarnings("static-access")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (getChildCount() == 0)
			return false;
		//lilong modify begin
		mGestureDetector.onTouchEvent(ev);
		if(mTouchState != TOUCH_STATE_SCROLLING){
			mScaleDetector.onTouchEvent(ev);
			if(beginScale){
				mCurrentAdapterView = (SFPImageView) getChildAt(mCurrentScreen);
				if(mCurrentAdapterView != null){
					mCurrentAdapterView.dispatchTouchEvent(ev);
					mTouchState = TOUCH_STATE_DISPATCH;
				}
				return true;
			}
			
			if(mCurrentAdapterView != null && mCurrentAdapterView.getScaleX() > 1.0f){
				mCurrentAdapterView.dispatchTouchEvent(ev);
				mTouchState = TOUCH_STATE_DISPATCH;
				return true;
			}
		}
		//lilong modify end
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float currentX = ev.getX();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// Remember where the motion event started
			mLastMotionX = currentX;
            
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
			break;

		case MotionEvent.ACTION_MOVE:
			
			final int xDiff = (int) Math.abs(currentX - mLastMotionX);

			boolean xMoved = xDiff > mTouchSlop;

			if(xMoved){
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// Scroll to follow the motion event
				final int deltaX = (int) (mLastMotionX - currentX);

				final int scrollX = getScrollX();
				if (deltaX < 0) {
					if (scrollX > 0) {
						scrollBy(Math.max(-scrollX, deltaX), 0);
					}
				} else if (deltaX > 0) {
					final int availableToScroll = getChildAt(
							getChildCount() - 1).getRight()- scrollX - getWidth();
					if (availableToScroll > 0) {
						scrollBy(Math.min(availableToScroll, deltaX), 0);
					}
				}
			}
			mLastMotionX = currentX;
			break;

		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityX = (int) velocityTracker.getXVelocity();

				if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
					// Fling hard enough to move left
					snapToScreen(mCurrentScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY
						&& mCurrentScreen < getChildCount() - 1) {
					// Fling hard enough to move right
					snapToScreen(mCurrentScreen + 1);
				} else {
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}else{
				FileInfo currentFile = (FileInfo) getAdapter().getItem(mCurrentAdapterIndex);
				if(currentFile != null && currentFile.MIMEType != null && currentFile.MIMEType.startsWith(FileInfo.MIME_VIDEO)){
					if(FolderBrowser.mBackgroundPlayer != null){
						if(FolderBrowser.mBackgroundPlayer.mPlayerThread != null){
							if(FolderBrowser.mBackgroundPlayer.mPlayerThread.isPlaying()){
								FolderBrowser.mBackgroundPlayer.mPlayerThread.pausePlayMusic();
							}
						}
					}
					openFile(currentFile.Path);
				}
			}

			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			snapToDestination();
			mTouchState = TOUCH_STATE_REST;
		}
		return true;
	}
	
	private class OnDoubleClick extends GestureDetector.SimpleOnGestureListener{
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			SFPImageView currentView = (SFPImageView) getChildAt(mCurrentScreen);
			if(currentView != null){
				if(currentView.getScaleX() > 1.0f){
					currentView.setTranslationX(0);
					currentView.setTranslationY(0);
					currentView.setScaleX(1.0f);
					currentView.setScaleY(1.0f);
					mTouchState = TOUCH_STATE_REST;
				}
			}
			return false;   
		}
		
		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			return super.onDoubleTapEvent(e);
		}
	}
	
	private void openFile(String path) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		File file = new File(path);
		String type = FileUtil.getMimeTypeOfFile(file.getName());
		intent.setDataAndType(Uri.fromFile(file), type);
		SFPApplication.mContext.startActivity(intent);
	}
	
	private class MyScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{

	    @Override
		public boolean onScale(ScaleGestureDetector detector) {
			return super.onScale(detector);
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			beginScale = true;
			return super.onScaleBegin(detector);
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			beginScale = false;
			super.onScaleEnd(detector);
		}
	}

	@Override
	protected void onScrollChanged(int h, int v, int oldh, int oldv) {
		super.onScrollChanged(h, v, oldh, oldv);
	}

	private void snapToDestination() {
		final int screenWidth = getWidth();
		final int whichScreen = (getScrollX() + (screenWidth / 2))
				/ screenWidth;

		snapToScreen(whichScreen);
	}

	private void snapToScreen(int whichScreen) {
		mLastScrollDirection = whichScreen - mCurrentScreen;
		if (!mScroller.isFinished())
			return;

		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		mNextScreen = whichScreen;
		
		final int newX = whichScreen * getWidth();
		final int delta = newX - getScrollX();
		mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta));
		invalidate();
	}
	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		} else if (mNextScreen != INVALID_SCREEN) {
			Log.d(TAG, "computeScroll(2)");
			mCurrentScreen = Math.max(0,
					Math.min(mNextScreen, getChildCount() - 1));
			mNextScreen = INVALID_SCREEN;
			postViewSwitched(mLastScrollDirection);
		}
	}

	/**
	 * Scroll to the {@link View} in the view buffer specified by the index.
	 * 
	 * @param indexInBuffer
	 *            Index of the view in the view buffer.
	 */
	private void setVisibleView(int indexInBuffer, boolean uiThread) {
		mCurrentScreen = Math.max(0,
				Math.min(indexInBuffer, getChildCount() - 1));
		int dx = (mCurrentScreen * getWidth()) - mScroller.getCurrX();
		mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), dx,
				0, 0);
		if(dx == 0)
			onScrollChanged(mScroller.getCurrX() + dx, mScroller.getCurrY(), mScroller.getCurrX() + dx, mScroller.getCurrY());
		if (uiThread)
			invalidate();
		else
			postInvalidate();
	}

	/**
	 * Set the listener that will receive notifications every time the {code
	 * ViewFlow} scrolls.
	 * 
	 * @param l
	 *            the scroll listener
	 */
	public void setOnViewSwitchListener(ViewSwitchListener l) {
		mViewSwitchListener = l;
	}

	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}

	@Override
	public void setAdapter(Adapter adapter) {
		setAdapter(adapter, 0);
	}
	
	public void setAdapter(Adapter adapter, int initialPosition) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}

		mAdapter = adapter;

		if (mAdapter != null) {
			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);

		}
		if (mAdapter == null || mAdapter.getCount() == 0)
			return;
		
		setSelection(initialPosition);		
	}
	
	@Override
	public View getSelectedView() {
		return (mCurrentBufferIndex < mLoadedViews.size() ? mLoadedViews
				.get(mCurrentBufferIndex) : null);
	}

    @Override
    public int getSelectedItemPosition() {
        return mCurrentAdapterIndex;
    }

	@Override
	public void setSelection(int position) {
		Log.d("lilong", "setSelection()");
		mNextScreen = INVALID_SCREEN;
		mScroller.forceFinished(true);
		if (mAdapter == null)
			return;
		
		position = Math.max(position, 0);
		position =  Math.min(position, mAdapter.getCount()-1);

		while (!mLoadedViews.isEmpty()) {
			detachViewFromParent(mLoadedViews.remove());
		}

		View currentView = makeAndAddView(position, true);
		mLoadedViews.addLast(currentView);
		
		for(int offset = 1; mSideBuffer - offset >= 0; offset++) {
			int leftIndex = position - offset;
			int rightIndex = position + offset;
			if(leftIndex >= 0)
				mLoadedViews.addFirst(makeAndAddView(leftIndex, false));
			if(rightIndex < mAdapter.getCount())
				mLoadedViews.addLast(makeAndAddView(rightIndex, true));
		}

		mCurrentBufferIndex = mLoadedViews.indexOf(currentView);
		mCurrentAdapterIndex = position;

		requestLayout();
		setVisibleView(mCurrentBufferIndex, false);
		if (mViewSwitchListener != null) {
			mViewSwitchListener
					.onSwitched(mLoadedViews.get(mCurrentBufferIndex),mCurrentAdapterIndex);
		}
	}

	private void resetFocus() {
		mLoadedViews.clear();
		removeAllViewsInLayout();

		for (int i = Math.max(0, mCurrentAdapterIndex - mSideBuffer); i < Math
				.min(mAdapter.getCount(), mCurrentAdapterIndex + mSideBuffer
						+ 1); i++) {
			mLoadedViews.addLast(makeAndAddView(i, true));
			if (i == mCurrentAdapterIndex)
				mCurrentBufferIndex = mLoadedViews.size() - 1;
		}
		requestLayout();
	}

	private void postViewSwitched(int direction) {
		if (direction == 0)
			return;

		if (direction > 0) { // to the right
			mCurrentAdapterIndex++;
			mCurrentBufferIndex++;


			// Remove view outside buffer range
			if (mCurrentAdapterIndex > mSideBuffer) {
				detachViewFromParent(mLoadedViews.removeFirst());
				// removeView(recycleView);
				mCurrentBufferIndex--;
			}

			// Add new view to buffer
			int newBufferIndex = mCurrentAdapterIndex + mSideBuffer;
			if (newBufferIndex < mAdapter.getCount()){
				mLoadedViews.addLast(makeAndAddView(newBufferIndex, true));
			}

		} else { // to the left
			mCurrentAdapterIndex--;
			mCurrentBufferIndex--;

			// Remove view outside buffer range
			if (mAdapter.getCount() - 1 - mCurrentAdapterIndex > mSideBuffer) {
				detachViewFromParent(mLoadedViews.removeLast());
			}

			// Add new view to buffer
			int newBufferIndex = mCurrentAdapterIndex - mSideBuffer;
			if (newBufferIndex > -1) {
				mLoadedViews.addFirst(makeAndAddView(newBufferIndex, false));
				mCurrentBufferIndex++;
			}

		}

		requestLayout();
		setVisibleView(mCurrentBufferIndex, true);
		if (mViewSwitchListener != null) {
			mViewSwitchListener.onSwitched(mLoadedViews.get(mCurrentBufferIndex),
							mCurrentAdapterIndex);
		}
	}

	private View setupChild(View child, boolean addToEnd, boolean recycle) {
		ViewGroup.LayoutParams p = (ViewGroup.LayoutParams) child.getLayoutParams();
		if (p == null) {
			p = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT, 0);
		}
		if (recycle)
			attachViewToParent(child, (addToEnd ? -1 : 0), p);
		else
			addViewInLayout(child, (addToEnd ? -1 : 0), p, true);
		return child;
	}

	private View makeAndAddView(int position, boolean addToEnd) {
		View view = mAdapter.getView(position, null, this);
		return setupChild(view, addToEnd,false);
	}

	class AdapterDataSetObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			Log.i(TAG, "AdapterDataSetObserver : onChanged()");
			View v = getChildAt(mCurrentBufferIndex);
			if (v != null) {
				for (int index = 0; index < mAdapter.getCount(); index++) {
					if (v.equals(mAdapter.getItem(index))) {
						mCurrentAdapterIndex = index;
						break;
					}
				}
			}
			resetFocus();
		}

		@Override
		public void onInvalidated() {
			// Not yet implemented!
		}
	}
}
