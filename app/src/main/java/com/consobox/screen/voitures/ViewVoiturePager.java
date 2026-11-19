package com.consobox.screen.voitures;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import com.consobox.R;
import com.consobox.data.Message;
import com.consobox.data.Utils;
import com.consobox.models.Voiture;
import com.consobox.screen.voitures.viewvoiture.OnDataChangeListener;
import com.consobox.screen.voitures.viewvoiture.OnVisible;
import com.consobox.screen.voitures.viewvoiture.ViewVoitureConso;
import com.consobox.screen.voitures.viewvoiture.ViewVoitureContent;
import com.consobox.screen.voitures.viewvoiture.ViewVoitureDepense;
import com.consobox.screen.voitures.viewvoiture.ViewVoitureStats;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class ViewVoiturePager extends Activity implements OnDataChangeListener {
    private TabPageIndicator mTitleIndicator;
    private ViewVoitureConso mViewVoitureConso;
    private ViewVoitureContent mViewVoitureContent;
    private ViewVoitureDepense mViewVoitureDepense;
    private Voiture mVoiture;

    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_viewvoiture);
        this.mVoiture = Utils.getVoiture(this, getIntent().getExtras().getInt("voitureid"));
        setTitle(this.mVoiture.nom);
        MyPagerAdapter adapter = new MyPagerAdapter(this);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        this.mTitleIndicator = (TabPageIndicator) findViewById(R.id.titles);
        this.mTitleIndicator.setViewPager(viewPager);
        try {
            if (getIntent().getAction().equals("revision")) {
                NotificationManager notifManager = (NotificationManager) getSystemService("notification");
                notifManager.cancel(getIntent().getIntExtra("voitureid", 0));
            }
        } catch (NullPointerException e) {
        }
        Message.viewVoiture = this;
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        this.mVoiture.updatsValues(this);
        this.mViewVoitureConso.initConsoList(false, 0);
        this.mViewVoitureDepense.initDepenses(false);
        this.mViewVoitureContent.init();
    }

    @Override // android.app.Activity
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.viewvoiture, menu);
        return true;
    }

    @Override // android.app.Activity
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.essence) {
            Message.EssenceBox(this, this.mViewVoitureConso, "0");
            return true;
        }
        return true;
    }

    @Override // android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        refresh();
    }

    private class MyPagerAdapter extends PagerAdapter implements TitleProvider {
        private ArrayList<LinearLayout> views = new ArrayList<>();

        public MyPagerAdapter(Context context) {
            DisplayMetrics dm = new DisplayMetrics();
            ViewVoiturePager.this.getWindowManager().getDefaultDisplay().getMetrics(dm);
            ViewVoiturePager.this.mViewVoitureConso = new ViewVoitureConso(context, ViewVoiturePager.this.mVoiture, ViewVoiturePager.this);
            ViewVoiturePager.this.mViewVoitureDepense = new ViewVoitureDepense(context, ViewVoiturePager.this.mVoiture);
            ViewVoiturePager.this.mViewVoitureContent = new ViewVoitureContent(context, ViewVoiturePager.this.mVoiture, ViewVoiturePager.this);
            this.views.add(ViewVoiturePager.this.mViewVoitureContent);
            this.views.add(ViewVoiturePager.this.mViewVoitureConso);
            this.views.add(ViewVoiturePager.this.mViewVoitureDepense);
            this.views.add(new ViewVoitureStats(context, ViewVoiturePager.this));
        }

        @Override // androidx.viewpager.widget.PagerAdapter
        public void destroyItem(View view, int arg1, Object object) {
            ((ViewPager) view).removeView((LinearLayout) object);
        }

        @Override // androidx.viewpager.widget.PagerAdapter
        public void finishUpdate(View arg0) {
        }

        @Override // androidx.viewpager.widget.PagerAdapter
        public int getCount() {
            return this.views.size();
        }

        /* JADX WARN: Multi-variable type inference failed */
        @Override // androidx.viewpager.widget.PagerAdapter
        public Object instantiateItem(View view, int position) {
            LinearLayout linearLayout = this.views.get(position);
            ((OnVisible) linearLayout).onVisible();
            ((ViewPager) view).addView(linearLayout);
            return linearLayout;
        }

        @Override // androidx.viewpager.widget.PagerAdapter
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override // androidx.viewpager.widget.PagerAdapter
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override // androidx.viewpager.widget.PagerAdapter
        public Parcelable saveState() {
            return null;
        }

        @Override // androidx.viewpager.widget.PagerAdapter
        public void startUpdate(View arg0) {
        }

        @Override // com.viewpagerindicator.TitleProvider
        public String getTitle(int position) {
            switch (position) {
                case 0:
                    return "Principal";
                case 1:
                    return "Consos";
                case 2:
                    return "Dépenses";
                case 3:
                    return "Stats";
                default:
                    return "Autre";
            }
        }
    }

    @Override // com.consobox.screen.voitures.viewvoiture.OnDataChangeListener
    public void OnDataChange() {
        refresh();
    }
}
