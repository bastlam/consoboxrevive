package com.consobox.screen.voitures.Listeners;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import com.consobox.data.Utils;
import com.consobox.screen.graph.Graphs;
import com.consobox.screen.voitures.StatsGeneral;
import java.io.FileNotFoundException;
import java.io.IOException;

/* JADX INFO: loaded from: classes.dex */
public class StatsListListener implements AdapterView.OnItemClickListener {
    private Activity parent;

    public StatsListListener(Activity parent) {
        this.parent = parent;
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        if (position == 0) {
            Intent intent = new Intent(this.parent, (Class<?>) StatsGeneral.class);
            intent.putExtra("voiture", this.parent.getIntent().getIntExtra("voitureid", 0));
            this.parent.startActivity(intent);
            return;
        }
        if (position == 1) {
            Intent intent2 = new Intent(this.parent, (Class<?>) Graphs.class);
            intent2.putExtra("voiture", this.parent.getIntent().getIntExtra("voitureid", 0));
            this.parent.startActivity(intent2);
            return;
        }
        if (position == 2) {
            try {
                Utils.exportConsoCSV(this.parent, this.parent.getIntent().getIntExtra("voitureid", 0), true);
            } catch (FileNotFoundException e3) {
                e3.printStackTrace();
            } catch (IOException e4) {
                e4.printStackTrace();
            }
        }
    }
}
