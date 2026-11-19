package com.consobox.data;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import com.consobox.R;
import com.consobox.screen.voitures.ViewVoiturePager;
import java.util.Calendar;

/* JADX INFO: loaded from: classes.dex */
public class Message {
    public static Dialog dialogInProgress;
    public static int sVoitureId = 0;
    public static ViewVoiturePager viewVoiture;

    public static void OKMessage(CharSequence string, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(string).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.consobox.data.Message.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void CustomMessage(CharSequence string, CharSequence OkMessage, CharSequence CancelMessage, Context context, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(string).setCancelable(true).setPositiveButton(OkMessage, listener);
        if (CancelMessage != null) {
            builder.setNegativeButton(CancelMessage, listener);
        }
        AlertDialog alert = builder.create();
        dialogInProgress = alert;
        alert.show();
    }

    public static Dialog ErrorMessage(CharSequence string, Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_errormessage);
        dialog.setTitle(context.getResources().getString(R.string.erreur));
        TextView text = (TextView) dialog.findViewById(R.id.ErrorMessage_text);
        text.setText(string);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
        return dialog;
    }

    public static void YesNoMessage(CharSequence string, Context context, View.OnClickListener listener) {
        Dialog dialog = new Dialog(context);
        dialogInProgress = dialog;
        dialog.setContentView(R.layout.dialog_yesnodelete);
        dialog.findViewById(R.id.YesNoDetele_yes).setOnClickListener(listener);
        dialog.findViewById(R.id.YesNoDetele_no).setOnClickListener(listener);
        dialog.setTitle(string);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    public static void EssenceBox(Context context, View.OnClickListener onClick, String prix) {
        Calendar cal = Calendar.getInstance();
        String date = String.valueOf(cal.get(5) < 10 ? "0" : "") + cal.get(5) + "/" + (cal.get(2) + 1 < 10 ? "0" : "") + (cal.get(2) + 1) + "/" + cal.get(1);
        EssenceBox(context, onClick, date, prix);
    }

    /* JADX WARN: Type inference failed for: r2v0, types: [com.consobox.data.Message$2] */
    public static void EssenceBox(Context context, View.OnClickListener onClick, String date, String prix) {
        Dialog dialog = new Dialog(context);
        dialogInProgress = dialog;
        dialog.setContentView(R.layout.dialog_ajoutplein);
        ((TextView) dialog.findViewById(R.id.essencebox_km)).setSelectAllOnFocus(true);
        ((TextView) dialog.findViewById(R.id.essencebox_litre)).setSelectAllOnFocus(true);
        ((TextView) dialog.findViewById(R.id.essencebox_prix)).setText(prix);
        ((TextView) dialog.findViewById(R.id.essencebox_date)).setText(date);
        ((ImageButton) dialog.findViewById(R.id.essencebox_datebutton)).setOnClickListener(new View.OnClickListener() { // from class: com.consobox.data.Message.2
            private Context context;

            public View.OnClickListener setContext(Context c) {
                this.context = c;
                return this;
            }

            @Override // android.view.View.OnClickListener
            public void onClick(View arg0) {
                DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() { // from class: com.consobox.data.Message.2.1
                    @Override // android.app.DatePickerDialog.OnDateSetListener
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        int month = monthOfYear + 1;
                        String txt = String.valueOf(dayOfMonth < 10 ? "0" : "") + String.valueOf(dayOfMonth) + "/" + (month < 10 ? "0" : "") + String.valueOf(month) + "/" + String.valueOf(year);
                        ((TextView) Message.dialogInProgress.findViewById(R.id.essencebox_date)).setText(txt);
                    }
                };
                String[] time = ((String) ((TextView) Message.dialogInProgress.findViewById(R.id.essencebox_date)).getText()).split("/");
                new DatePickerDialog(this.context, mDateSetListener, Integer.valueOf(time[2]).intValue(), Integer.valueOf(time[1]).intValue() - 1, Integer.valueOf(time[0]).intValue()).show();
            }
        }.setContext(context));
        dialog.setTitle(context.getResources().getString(R.string.ajouterplein));
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        ((Button) dialog.findViewById(R.id.essencebox_ok)).setOnClickListener(onClick);
        ((Button) dialog.findViewById(R.id.essencebox_annuler)).setOnClickListener(onClick);
        dialog.show();
    }

    public static void ChangeDoubleDialog(String title, View.OnClickListener listener, Context context, double defaultValue) {
        Dialog dialog = new Dialog(context);
        dialogInProgress = dialog;
        dialog.setContentView(R.layout.dialog_yesnoedit);
        ((EditText) dialog.findViewById(R.id.value)).setSelectAllOnFocus(true);
        ((EditText) dialog.findViewById(R.id.value)).setText(String.valueOf(defaultValue));
        dialog.setTitle(title);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        ((Button) dialog.findViewById(R.id.ok)).setOnClickListener(listener);
        ((Button) dialog.findViewById(R.id.annuler)).setOnClickListener(listener);
        dialog.show();
    }

}
