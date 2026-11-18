package com.consobox.lib.popup;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import com.consobox.R;

/* JADX INFO: loaded from: classes.dex */
public class QuickAction extends PopupWindows {
    public static final int ANIM_AUTO = 5;
    public static final int ANIM_GROW_FROM_CENTER = 3;
    public static final int ANIM_GROW_FROM_LEFT = 1;
    public static final int ANIM_GROW_FROM_RIGHT = 2;
    public static final int ANIM_REFLECT = 4;
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private int mAnimStyle;
    private ImageView mArrowDown;
    private ImageView mArrowUp;
    private LayoutInflater mInflater;
    private int mOrientation;
    private ScrollView mScroller;
    private ViewGroup mTrack;
    private int rootWidth;

    public QuickAction(Context context) {
        this(context, 1);
    }

    public QuickAction(Context context, int orientation) {
        super(context);
        this.rootWidth = 0;
        this.mOrientation = orientation;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        if (this.mOrientation == 0) {
            setRootViewId(R.layout.popup_horizontal);
        } else {
            setRootViewId(R.layout.popup_vertical);
        }
        this.mAnimStyle = 5;
    }

    public void setRootViewId(int id) {
        this.mRootView = (ViewGroup) this.mInflater.inflate(id, (ViewGroup) null);
        this.mTrack = (ViewGroup) this.mRootView.findViewById(R.id.tracks);
        this.mArrowDown = (ImageView) this.mRootView.findViewById(R.id.arrow_down);
        this.mArrowUp = (ImageView) this.mRootView.findViewById(R.id.arrow_up);
        this.mScroller = (ScrollView) this.mRootView.findViewById(R.id.scroller);
        this.mRootView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
        setContentView(this.mRootView);
    }

    public void setAnimStyle(int mAnimStyle) {
        this.mAnimStyle = mAnimStyle;
    }

    public void setView(View aView) {
        this.mTrack.addView(aView, 0);
    }

    public void show(View anchor) {
        int xPos;
        int arrowPos;
        int yPos;
        preShow();
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());
        this.mRootView.measure(-2, -2);
        int rootHeight = this.mRootView.getMeasuredHeight();
        if (this.rootWidth == 0) {
            this.rootWidth = this.mRootView.getMeasuredWidth();
        }
        int screenWidth = this.mWindowManager.getDefaultDisplay().getWidth();
        int screenHeight = this.mWindowManager.getDefaultDisplay().getHeight();
        if (anchorRect.left + this.rootWidth > screenWidth) {
            xPos = anchorRect.left - (this.rootWidth - anchor.getWidth());
            if (xPos < 0) {
                xPos = 0;
            }
            arrowPos = anchorRect.centerX() - xPos;
        } else {
            if (anchor.getWidth() > this.rootWidth) {
                xPos = anchorRect.centerX() - (this.rootWidth / 2);
            } else {
                xPos = anchorRect.left;
            }
            arrowPos = anchorRect.centerX() - xPos;
        }
        int dyTop = anchorRect.top;
        int dyBottom = screenHeight - anchorRect.bottom;
        boolean onTop = dyTop > dyBottom;
        if (onTop) {
            if (rootHeight > dyTop) {
                yPos = 15;
                ViewGroup.LayoutParams l = this.mScroller.getLayoutParams();
                l.height = dyTop - anchor.getHeight();
            } else {
                yPos = anchorRect.top - rootHeight;
            }
        } else {
            yPos = anchorRect.bottom;
            if (rootHeight > dyBottom) {
                ViewGroup.LayoutParams l2 = this.mScroller.getLayoutParams();
                l2.height = dyBottom;
            }
        }
        showArrow(onTop ? R.id.arrow_down : R.id.arrow_up, arrowPos);
        setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
        this.mWindow.showAtLocation(anchor, 0, xPos, yPos);
    }

    private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
        this.mWindow.setAnimationStyle(2131427336);
    }

    private void showArrow(int whichArrow, int requestedX) {
        View showArrow = whichArrow == R.id.arrow_up ? this.mArrowUp : this.mArrowDown;
        View hideArrow = whichArrow == R.id.arrow_up ? this.mArrowDown : this.mArrowUp;
        int arrowWidth = this.mArrowUp.getMeasuredWidth();
        showArrow.setVisibility(0);
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow.getLayoutParams();
        param.leftMargin = requestedX - (arrowWidth / 2);
        hideArrow.setVisibility(4);
    }
}
