package com.viewpagerindicator;

import android.content.Context;
import com.consobox.R;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

/* JADX INFO: loaded from: classes.dex */
public class TabPageIndicator extends HorizontalScrollView implements PageIndicator {
    private LayoutInflater mInflater;
    private ViewPager.OnPageChangeListener mListener;
    int mMaxTabWidth;
    private int mSelectedTabIndex;
    private View.OnClickListener mTabClickListener;
    private LinearLayout mTabLayout;
    Runnable mTabSelector;
    private ViewPager mViewPager;

    public TabPageIndicator(Context context) {
        this(context, null);
    }

    public TabPageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTabClickListener = new View.OnClickListener() { // from class: com.viewpagerindicator.TabPageIndicator.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                TabView tabView = (TabView) view;
                TabPageIndicator.this.mViewPager.setCurrentItem(tabView.getIndex());
            }
        };
        setHorizontalScrollBarEnabled(false);
        this.mInflater = LayoutInflater.from(context);
        this.mTabLayout = new LinearLayout(getContext());
        addView(this.mTabLayout, new ViewGroup.LayoutParams(-2, -1));
    }

    @Override // android.widget.HorizontalScrollView, android.widget.FrameLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        boolean lockedExpanded = widthMode == 1073741824;
        setFillViewport(lockedExpanded);
        int childCount = this.mTabLayout.getChildCount();
        if (childCount > 1 && (widthMode == 1073741824 || widthMode == Integer.MIN_VALUE)) {
            if (childCount > 2) {
                this.mMaxTabWidth = (int) (View.MeasureSpec.getSize(widthMeasureSpec) * 0.4f);
            } else {
                this.mMaxTabWidth = View.MeasureSpec.getSize(widthMeasureSpec) / 2;
            }
        } else {
            this.mMaxTabWidth = -1;
        }
        int oldWidth = getMeasuredWidth();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int newWidth = getMeasuredWidth();
        if (lockedExpanded && oldWidth != newWidth) {
            setCurrentItem(this.mSelectedTabIndex);
        }
    }

    private void animateToTab(int position) {
        final View tabView = this.mTabLayout.getChildAt(position);
        if (this.mTabSelector != null) {
            removeCallbacks(this.mTabSelector);
        }
        this.mTabSelector = new Runnable() { // from class: com.viewpagerindicator.TabPageIndicator.2
            @Override // java.lang.Runnable
            public void run() {
                int scrollPos = tabView.getLeft() - ((TabPageIndicator.this.getWidth() - tabView.getWidth()) / 2);
                TabPageIndicator.this.smoothScrollTo(scrollPos, 0);
                TabPageIndicator.this.mTabSelector = null;
            }
        };
        post(this.mTabSelector);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mTabSelector != null) {
            post(this.mTabSelector);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mTabSelector != null) {
            removeCallbacks(this.mTabSelector);
        }
    }

    private void addTab(String text, int index) {
        TabView tabView = (TabView) this.mInflater.inflate(R.layout.vpi__tab, (ViewGroup) null);
        tabView.init(this, text, index);
        tabView.setFocusable(true);
        tabView.setOnClickListener(this.mTabClickListener);
        this.mTabLayout.addView(tabView, new LinearLayout.LayoutParams(0, -1, 1.0f));
    }

    @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
    public void onPageScrollStateChanged(int arg0) {
        if (this.mListener != null) {
            this.mListener.onPageScrollStateChanged(arg0);
        }
    }

    @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if (this.mListener != null) {
            this.mListener.onPageScrolled(arg0, arg1, arg2);
        }
    }

    @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
    public void onPageSelected(int arg0) {
        setCurrentItem(arg0);
        if (this.mListener != null) {
            this.mListener.onPageSelected(arg0);
        }
    }

    @Override // com.viewpagerindicator.PageIndicator
    public void setViewPager(ViewPager view) {
        PagerAdapter adapter = view.getAdapter();
        if (adapter == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        if (!(adapter instanceof TitleProvider)) {
            throw new IllegalStateException("ViewPager adapter must implement TitleProvider to be used with TitlePageIndicator.");
        }
        this.mViewPager = view;
        view.setOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.viewpagerindicator.PageIndicator
    public void notifyDataSetChanged() {
        this.mTabLayout.removeAllViews();
        TitleProvider titleProvider = (TitleProvider) this.mViewPager.getAdapter();
        int count = ((PagerAdapter) titleProvider).getCount();
        for (int i = 0; i < count; i++) {
            addTab(titleProvider.getTitle(i), i);
        }
        if (this.mSelectedTabIndex > count) {
            this.mSelectedTabIndex = count - 1;
        }
        setCurrentItem(this.mSelectedTabIndex);
        requestLayout();
    }

    @Override // com.viewpagerindicator.PageIndicator
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override // com.viewpagerindicator.PageIndicator
    public void setCurrentItem(int item) {
        if (this.mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        this.mSelectedTabIndex = item;
        int tabCount = this.mTabLayout.getChildCount();
        int i = 0;
        while (i < tabCount) {
            View child = this.mTabLayout.getChildAt(i);
            boolean isSelected = i == item;
            child.setSelected(isSelected);
            if (isSelected) {
                animateToTab(item);
            }
            i++;
        }
    }

    @Override // com.viewpagerindicator.PageIndicator
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.mListener = listener;
    }

    public static class TabView extends LinearLayout {
        private int mIndex;
        private TabPageIndicator mParent;

        public TabView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public void init(TabPageIndicator parent, String text, int index) {
            this.mParent = parent;
            this.mIndex = index;
            TextView textView = (TextView) findViewById(android.R.id.text1);
            textView.setText(text);
        }

        @Override // android.widget.LinearLayout, android.view.View
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (this.mParent.mMaxTabWidth > 0 && getMeasuredWidth() > this.mParent.mMaxTabWidth) {
                super.onMeasure(View.MeasureSpec.makeMeasureSpec(this.mParent.mMaxTabWidth, 1073741824), heightMeasureSpec);
            }
        }

        public int getIndex() {
            return this.mIndex;
        }
    }
}
