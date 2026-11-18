package com.consobox.screen.voitures.Listeners;

import android.os.Handler;
import android.os.Message;
import android.widget.AbsListView;

/* JADX INFO: loaded from: classes.dex */
public class ViewVoitureOnScrollPlein implements AbsListView.OnScrollListener {
    private Handler mHandler = new Handler() { // from class: com.consobox.screen.voitures.Listeners.ViewVoitureOnScrollPlein.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            ViewVoitureOnScrollPlein.this.mView.initConsoList(true, msg.what);
        }
    };
    private BackConso mView;

    public interface BackConso {
        void initConsoList(boolean z, int i);
    }

    public ViewVoitureOnScrollPlein(BackConso context) {
        this.mView = context;
    }

    @Override // android.widget.AbsListView.OnScrollListener
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
            this.mHandler.sendEmptyMessage(totalItemCount);
        }
    }

    @Override // android.widget.AbsListView.OnScrollListener
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}
