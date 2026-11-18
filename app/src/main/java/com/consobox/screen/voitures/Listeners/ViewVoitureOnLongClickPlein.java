package com.consobox.screen.voitures.Listeners;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import com.consobox.R;
import com.consobox.data.Message;
import com.consobox.data.Utils;
import java.util.HashMap;

/* JADX INFO: loaded from: classes.dex */
public class ViewVoitureOnLongClickPlein implements AdapterView.OnItemLongClickListener, DialogInterface.OnClickListener {
    protected Context mContext;
    private int mVoiture;

    public ViewVoitureOnLongClickPlein(Context context, int voiture) {
        this.mContext = context;
        this.mVoiture = voiture;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface arg0, int arg1) {
        if (arg1 == -1) {
            Intent intent = new Intent(Message.viewVoiture, (Class<?>) ViewVoitureModifPlein.class);
            intent.putExtra("voitureid", this.mVoiture);
            intent.putExtra("pleinid", Message.sVoitureId);
            Message.viewVoiture.startActivityForResult(intent, 20);
            return;
        }
        if (arg1 == -2) {
            Message.YesNoMessage("Supprimer ce plein ?", this.mContext, new View.OnClickListener() { // from class: com.consobox.screen.voitures.Listeners.ViewVoitureOnLongClickPlein.1
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    if (v.getId() == R.id.YesNoDetele_yes) {
                        SQLiteDatabase sql = Utils.getDatabase(ViewVoitureOnLongClickPlein.this.mContext);
                        sql.execSQL("UPDATE voiture SET km=(SELECT km FROM voiture WHERE id=" + ViewVoitureOnLongClickPlein.this.mVoiture + ") - (SELECT km FROM conso WHERE id=" + Message.sVoitureId + ") WHERE id=" + ViewVoitureOnLongClickPlein.this.mVoiture);
                        sql.execSQL("DELETE FROM conso WHERE id=" + Message.sVoitureId);
                        sql.execSQL("DELETE FROM depenses WHERE type=0 AND description='" + Message.sVoitureId + "'");
                        Utils.closeDatabase(ViewVoitureOnLongClickPlein.this.mContext, sql);
                        Message.viewVoiture.refresh();
                    }
                    if (Message.dialogInProgress.isShowing()) {
                        Message.dialogInProgress.cancel();
                    }
                }
            });
        }
    }

    @Override // android.widget.AdapterView.OnItemLongClickListener
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, String> hashMap = (HashMap) ((SimpleAdapter) parent.getAdapter()).getItem(position);
        String itemId = hashMap.get("id");
        Message.CustomMessage(this.mContext.getResources().getString(R.string.vouspouvezmodifier), this.mContext.getResources().getString(R.string.modifier), this.mContext.getResources().getString(R.string.supprimer), this.mContext, this);
        Message.sVoitureId = Integer.valueOf(itemId).intValue();
        return true;
    }
}
