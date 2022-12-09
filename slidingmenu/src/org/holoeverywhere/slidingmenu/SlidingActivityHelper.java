
package org.holoeverywhere.slidingmenu;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.app.Activity;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class SlidingActivityHelper {
    static {
        LayoutInflater.remap(SlidingMenuView.class);
    }

    // NOP for execute static code block
    public static void init() {

    }

    private Activity mActivity;

    private boolean mBroadcasting = false;

    private boolean mEnableSlide = true;

    private boolean mOnPostCreateCalled = false;

    private SlidingMenuView mSlidingMenu;

    private View mViewAbove;

    private View mViewBehind;

    /**
     * Instantiates a new SlidingActivityHelper.
     * 
     * @param activity the associated activity
     */
    public SlidingActivityHelper(Activity activity) {
        mActivity = activity;
    }

    /**
     * Finds a view that was identified by the id attribute from the XML that
     * was processed in onCreate(Bundle).
     * 
     * @param id the resource id of the desired view
     * @return The view if found or null otherwise.
     */
    public View findViewById(int id) {
        View v;
        if (mSlidingMenu != null) {
            v = mSlidingMenu.findViewById(id);
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    /**
     * Gets the SlidingMenu associated with this activity.
     * 
     * @return the SlidingMenu associated with this activity.
     */
    public SlidingMenuView getSlidingMenu() {
        return mSlidingMenu;
    }

    /**
     * Sets mSlidingMenu as a newly inflated SlidingMenu. Should be called
     * within the activitiy's onCreate()
     * 
     * @param savedInstanceState the saved instance state (unused)
     */
    public void onCreate(Bundle savedInstanceState) {
        mSlidingMenu = (SlidingMenuView) LayoutInflater.from(mActivity).inflate(
                R.layout.slidingmenumain, null);
    }

    /**
     * On key up.
     * 
     * @param keyCode the key code
     * @param event the event
     * @return true, if successful
     */
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mSlidingMenu.isBehindShowing()) {
            showAbove();
            return true;
        }
        return false;
    }

    /**
     * Further SlidingMenu initialization. Should be called within the
     * activitiy's onPostCreate()
     * 
     * @param savedInstanceState the saved instance state (unused)
     */
    public void onPostCreate(Bundle savedInstanceState) {
        if (mViewBehind == null) {
            throw new IllegalStateException("Both setBehindContentView must be called in onCreate");
        }

        mOnPostCreateCalled = true;

        // get the window background
        TypedArray a = mActivity.getTheme().obtainStyledAttributes(new int[] {
                android.R.attr.windowBackground
        });
        int background = a.getResourceId(0, 0);
        a.recycle();

        if (mEnableSlide) {
            // move everything into the SlidingMenu
            ViewGroup decor = (ViewGroup) mActivity.getWindow().getDecorView();
            ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
            // save ActionBar themes that have transparent assets
            decorChild.setBackgroundResource(background);
            decor.removeView(decorChild);
            mSlidingMenu.setContent(decorChild);
            decor.addView(mSlidingMenu);
        } else {
            if (mViewAbove == null) {
                mViewAbove = new View(mActivity);
            }
            // take the above view out of
            ViewGroup parent = (ViewGroup) mViewAbove.getParent();
            if (parent != null) {
                parent.removeView(mViewAbove);
            }
            // save people from having transparent backgrounds
            if (mViewAbove.getBackground() == null) {
                mViewAbove.setBackgroundResource(background);
            }
            mSlidingMenu.setContent(mViewAbove);
            parent.addView(mSlidingMenu, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
        }
    }

    /**
     * Called to retrieve per-instance state from an activity before being
     * killed so that the state can be restored in onCreate(Bundle) or
     * onRestoreInstanceState(Bundle) (the Bundle populated by this method will
     * be passed to both).
     * 
     * @param outState Bundle in which to place your saved state.
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("menuOpen", mSlidingMenu.isBehindShowing());
    }

    /**
     * Register the above content view.
     * 
     * @param v the above content view to register
     * @param params LayoutParams for that view (unused)
     */
    public void registerAboveContentView(View v, LayoutParams params) {
        if (!mBroadcasting) {
            mViewAbove = v;
        }
    }

    /**
     * Set the behind view content to an explicit view. This view is placed
     * directly into the behind view 's view hierarchy. It can itself be a
     * complex view hierarchy.
     * 
     * @param view The desired content to display.
     * @param layoutParams Layout parameters for the view. (unused)
     */
    public void setBehindContentView(View view, LayoutParams layoutParams) {
        mViewBehind = view;
        mSlidingMenu.setMenu(mViewBehind);
    }

    /**
     * Set the activity content to an explicit view. This view is placed
     * directly into the activity's view hierarchy. It can itself be a complex
     * view hierarchy. When calling this method, the layout parameters of the
     * specified view are ignored. Both the width and the height of the view are
     * set by default to MATCH_PARENT. To use your own layout parameters, invoke
     * setContentView(android.view.View, android.view.ViewGroup.LayoutParams)
     * instead.
     * 
     * @param v The desired content to display.
     */
    public void setContentView(View v) {
        mBroadcasting = true;
        mActivity.setContentView(v);
    }

    /**
     * Controls whether the ActionBar slides along with the above view when the
     * menu is opened, or if it stays in place.
     * 
     * @param slidingActionBarEnabled True if you want the ActionBar to slide
     *            along with the SlidingMenu, false if you want the ActionBar to
     *            stay in place
     */
    public void setSlidingActionBarEnabled(boolean slidingActionBarEnabled) {
        if (mOnPostCreateCalled) {
            throw new IllegalStateException("enableSlidingActionBar must be called in onCreate.");
        }
        mEnableSlide = slidingActionBarEnabled;
    }

    /**
     * Close the SlidingMenu and show the above view.
     */
    public void showAbove() {
        mSlidingMenu.showAbove();
    }

    /**
     * Open the SlidingMenu and show the behind view.
     */
    public void showBehind() {
        mSlidingMenu.showBehind();
    }

    /**
     * Toggle the SlidingMenu. If it is open, it will be closed, and vice versa.
     */
    public void toggle() {
        if (mSlidingMenu.isBehindShowing()) {
            showAbove();
        } else {
            showBehind();
        }
    }

}