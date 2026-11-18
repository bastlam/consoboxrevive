package com.consobox.screen.fiches;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/* JADX INFO: loaded from: classes.dex */
public class FicheView extends Activity {
    @Override // android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(this);
        addContentView(linearLayout, new LinearLayout.LayoutParams(-1, -1));
        ScrollView sv = new ScrollView(this);
        TextView text = new TextView(this);
        linearLayout.addView(sv);
        sv.addView(text);
        String styledText = getIntent().getExtras().getString("text");
        text.setText(Html.fromHtml(styledText), TextView.BufferType.SPANNABLE);
    }
}
