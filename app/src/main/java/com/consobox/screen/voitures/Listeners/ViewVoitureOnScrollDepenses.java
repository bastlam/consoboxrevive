package com.consobox.screen.voitures.Listeners;

import android.os.Handler;
import android.os.Message;
import android.widget.AbsListView;

/* JADX INFO: loaded from: classes.dex */
public class ViewVoitureOnScrollDepenses implements AbsListView.OnScrollListener {
    private BackDepense mView;
    private int oldVisibleItem = -1;
    private Handler mHandler = new Handler() { // from class: com.consobox.screen.voitures.Listeners.ViewVoitureOnScrollDepenses.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == -1) {
                ViewVoitureOnScrollDepenses.this.mView.initDepenses(true);
            } else {
                ViewVoitureOnScrollDepenses.this.mView.setDepensePinnedText(msg.what);
            }
        }
    };
    public boolean isEnable = false;

    public interface BackDepense {
        void initDepenses(boolean z);

        void setDepensePinnedText(int i);
    }

    public ViewVoitureOnScrollDepenses(BackDepense context) {
        this.mView = context;
    }

    @Override // android.widget.AbsListView.OnScrollListener
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (this.oldVisibleItem != firstVisibleItem) {
            this.oldVisibleItem = firstVisibleItem;
            this.mHandler.sendEmptyMessage(firstVisibleItem);
        }
        if (firstVisibleItem + visibleItemCount >= totalItemCount - 1 && this.isEnable) {
            this.mHandler.sendEmptyMessage(-1);
        }
    }

    @Override // android.widget.AbsListView.OnScrollListener
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}
