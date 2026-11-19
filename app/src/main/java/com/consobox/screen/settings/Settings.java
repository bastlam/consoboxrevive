package com.consobox.screen.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.widget.Toast;
import com.consobox.R;
import com.consobox.data.BackupManager;
import com.consobox.drive.DriveBackup;
import com.consobox.drive.DriveClient;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.ApiException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Settings extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private static final int REQ_EXPORT = 1;
    private static final int REQ_IMPORT = 2;
    private static final int REQ_DRIVE_AUTH = 3;

    /** What to do once the Google authorization is obtained. */
    private static final int ACTION_CONNECT = 0;
    private static final int ACTION_BACKUP = 1;
    private static final int ACTION_RESTORE = 2;
    private static final int ACTION_ENABLE_AUTO = 3;

    Preference devise;
    SharedPreferences mPref;

    private Preference mDriveAccount;
    private CheckBoxPreference mDriveAuto;
    private Preference mDriveBackupNow;
    private Preference mDriveRestore;

    private int mPendingAction = ACTION_CONNECT;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final Handler mMain = new Handler(Looper.getMainLooper());
    private ProgressDialog mProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mPendingAction = savedInstanceState.getInt("pendingAction", ACTION_CONNECT);
        }
        setPreferenceScreen(initContent());
        refreshDrivePrefs();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pendingAction", mPendingAction);
    }

    @Override
    protected void onDestroy() {
        hideProgress();
        mExecutor.shutdown();
        super.onDestroy();
    }

    private PreferenceScreen initContent() {
        getPreferenceManager().setSharedPreferencesName("consobox");
        this.mPref = getPreferenceManager().getSharedPreferences();
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

        // ---- Currency
        PreferenceCategory revision = new PreferenceCategory(this);
        revision.setTitle(getResources().getString(R.string.gestionrevision));
        root.addPreference(revision);
        this.devise = new Preference(this);
        this.devise.setTitle(getResources().getString(R.string.devise));
        this.devise.setSummary(getResources().getStringArray(R.array.devise)[this.mPref.getInt("devise", 0)]);
        this.devise.setOnPreferenceClickListener(preference -> {
            CharSequence[] items = getResources().getStringArray(R.array.devise);
            new AlertDialog.Builder(Settings.this)
                    .setTitle(getResources().getString(R.string.choixdevise))
                    .setItems(items, (dialog, item) -> {
                        mPref.edit().putInt("devise", item).commit();
                        devise.setSummary(getResources().getStringArray(R.array.devise)[mPref.getInt("devise", 0)]);
                        dialog.cancel();
                    })
                    .show();
            return true;
        });
        revision.addPreference(this.devise);

        // ---- Local backup (Storage Access Framework: no storage permission needed)
        PreferenceCategory local = new PreferenceCategory(this);
        local.setTitle(R.string.local_backup_category);
        root.addPreference(local);
        Preference export = new Preference(this);
        export.setTitle(R.string.export_file);
        export.setSummary(R.string.export_file_summary);
        export.setOnPreferenceClickListener(p -> {
            Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("application/octet-stream");
            i.putExtra(Intent.EXTRA_TITLE, "consobox-" + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()) + ".bak");
            startActivityForResult(i, REQ_EXPORT);
            return true;
        });
        local.addPreference(export);
        Preference importPref = new Preference(this);
        importPref.setTitle(R.string.import_file);
        importPref.setSummary(R.string.import_file_summary);
        importPref.setOnPreferenceClickListener(p -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(i, REQ_IMPORT);
            return true;
        });
        local.addPreference(importPref);

        // ---- Google Drive backup
        PreferenceCategory drive = new PreferenceCategory(this);
        drive.setTitle(R.string.drive_category);
        root.addPreference(drive);
        mDriveAccount = new Preference(this);
        mDriveAccount.setTitle(R.string.drive_account);
        mDriveAccount.setOnPreferenceClickListener(p -> {
            if (DriveBackup.getEmail(this) == null) {
                startDriveAction(ACTION_CONNECT);
            } else {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.drive_disconnect_confirm)
                        .setPositiveButton(R.string.yes, (d, w) -> {
                            DriveBackup.disconnect(this);
                            refreshDrivePrefs();
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
            return true;
        });
        drive.addPreference(mDriveAccount);
        mDriveAuto = new CheckBoxPreference(this);
        mDriveAuto.setPersistent(false);
        mDriveAuto.setTitle(R.string.drive_auto);
        mDriveAuto.setOnPreferenceChangeListener((p, value) -> {
            if ((Boolean) value) {
                if (DriveBackup.getEmail(this) == null) {
                    startDriveAction(ACTION_ENABLE_AUTO);
                    return false;
                }
                DriveBackup.setAutoEnabled(this, true);
            } else {
                DriveBackup.setAutoEnabled(this, false);
            }
            refreshDrivePrefs();
            return true;
        });
        drive.addPreference(mDriveAuto);
        mDriveBackupNow = new Preference(this);
        mDriveBackupNow.setTitle(R.string.drive_backup_now);
        mDriveBackupNow.setOnPreferenceClickListener(p -> {
            startDriveAction(ACTION_BACKUP);
            return true;
        });
        drive.addPreference(mDriveBackupNow);
        mDriveRestore = new Preference(this);
        mDriveRestore.setTitle(R.string.drive_restore);
        mDriveRestore.setSummary(R.string.drive_restore_summary);
        mDriveRestore.setOnPreferenceClickListener(p -> {
            startDriveAction(ACTION_RESTORE);
            return true;
        });
        drive.addPreference(mDriveRestore);

        // ---- About
        PreferenceCategory about = new PreferenceCategory(this);
        about.setTitle(getResources().getString(R.string.aproposde));
        root.addPreference(about);
        Preference version = new Preference(this);
        version.setTitle(getResources().getString(R.string.versionlogiciel));
        try {
            version.setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e2) {
            e2.printStackTrace();
        }
        version.setEnabled(false);
        about.addPreference(version);
        return root;
    }

    private void refreshDrivePrefs() {
        String email = DriveBackup.getEmail(this);
        boolean connected = email != null;
        mDriveAccount.setSummary(connected ? getString(R.string.drive_account_connected, email) : getString(R.string.drive_account_none));
        mDriveAuto.setChecked(connected && DriveBackup.isAutoEnabled(this));
        long last = DriveBackup.getLastBackup(this);
        String lastTxt = last == 0 ? getString(R.string.drive_never)
                : getString(R.string.drive_last_backup, DateFormat.getDateFormat(this).format(new Date(last))
                + " " + DateFormat.getTimeFormat(this).format(new Date(last)));
        mDriveAuto.setSummary(getString(R.string.drive_auto_summary) + "\n" + lastTxt);
        mDriveBackupNow.setSummary(lastTxt);
    }

    // ------------------------------------------------------------------ Google Drive

    private void startDriveAction(int action) {
        mPendingAction = action;
        showProgress();
        DriveBackup.authorize(this)
                .addOnSuccessListener(this, result -> {
                    if (result.hasResolution()) {
                        hideProgress();
                        try {
                            startIntentSenderForResult(result.getPendingIntent().getIntentSender(), REQ_DRIVE_AUTH, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            toast(getString(R.string.drive_auth_failed));
                        }
                    } else {
                        onDriveToken(result.getAccessToken());
                    }
                })
                .addOnFailureListener(this, e -> {
                    hideProgress();
                    toast(getString(R.string.drive_error, e.getLocalizedMessage()));
                });
    }

    private void onDriveToken(String token) {
        if (token == null) {
            hideProgress();
            toast(getString(R.string.drive_auth_failed));
            return;
        }
        final int action = mPendingAction;
        showProgress();
        mExecutor.execute(() -> {
            try {
                DriveClient client = new DriveClient(this, token);
                if (DriveBackup.getEmail(this) == null) {
                    String email = client.getUserEmail();
                    DriveBackup.onConnected(this, email != null ? email : "?");
                }
                if (action == ACTION_ENABLE_AUTO) {
                    DriveBackup.setAutoEnabled(this, true);
                }
                if (action == ACTION_BACKUP || (action == ACTION_ENABLE_AUTO && DriveBackup.getLastBackup(this) == 0)) {
                    DriveBackup.backupNow(this, token);
                    mMain.post(() -> toast(getString(R.string.drive_backup_ok)));
                }
                if (action == ACTION_RESTORE) {
                    List<DriveClient.DriveFile> files = client.listBackups();
                    mMain.post(() -> {
                        hideProgress();
                        chooseDriveBackup(token, files);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                mMain.post(() -> toast(getString(R.string.drive_error, e.getLocalizedMessage())));
            } finally {
                mMain.post(() -> {
                    if (action != ACTION_RESTORE) {
                        hideProgress();
                    }
                    refreshDrivePrefs();
                });
            }
        });
    }

    private void chooseDriveBackup(String token, List<DriveClient.DriveFile> files) {
        if (files.isEmpty()) {
            toast(getString(R.string.drive_no_backup));
            return;
        }
        CharSequence[] names = new CharSequence[files.size()];
        for (int i = 0; i < files.size(); i++) {
            DriveClient.DriveFile f = files.get(i);
            names[i] = f.name + (f.size > 0 ? "  (" + (f.size / 1024) + " Ko)" : "");
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.drive_restore)
                .setItems(names, (d, which) -> confirmRestore(() -> {
                    showProgress();
                    mExecutor.execute(() -> {
                        try (InputStream in = new DriveClient(this, token).download(files.get(which).id)) {
                            BackupManager.restore(this, in);
                            mMain.post(() -> toast(getString(R.string.restaurerok)));
                        } catch (BackupManager.InvalidBackupException e) {
                            mMain.post(() -> toast(getString(R.string.invalid_backup)));
                        } catch (Exception e) {
                            e.printStackTrace();
                            mMain.post(() -> toast(getString(R.string.drive_error, e.getLocalizedMessage())));
                        } finally {
                            mMain.post(this::hideProgress);
                        }
                    });
                }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ------------------------------------------------------------------ results

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DRIVE_AUTH) {
            if (resultCode != Activity.RESULT_OK || data == null) {
                toast(getString(R.string.drive_auth_failed));
                return;
            }
            try {
                AuthorizationResult r = Identity.getAuthorizationClient(this).getAuthorizationResultFromIntent(data);
                onDriveToken(r.getAccessToken());
            } catch (ApiException e) {
                toast(getString(R.string.drive_auth_failed) + " (" + e.getStatusCode() + ")");
            }
            return;
        }
        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        final Uri uri = data.getData();
        if (requestCode == REQ_EXPORT) {
            showProgress();
            mExecutor.execute(() -> {
                try (OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
                    BackupManager.export(this, out);
                    mMain.post(() -> toast(getString(R.string.export_ok)));
                } catch (Exception e) {
                    e.printStackTrace();
                    mMain.post(() -> toast(getString(R.string.saveimpossible)));
                } finally {
                    mMain.post(this::hideProgress);
                }
            });
        } else if (requestCode == REQ_IMPORT) {
            confirmRestore(() -> {
                showProgress();
                mExecutor.execute(() -> {
                    try (InputStream in = getContentResolver().openInputStream(uri)) {
                        BackupManager.restore(this, in);
                        mMain.post(() -> toast(getString(R.string.restaurerok)));
                    } catch (BackupManager.InvalidBackupException e) {
                        mMain.post(() -> toast(getString(R.string.invalid_backup)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        mMain.post(() -> toast(getString(R.string.restaurerimpossible)));
                    } finally {
                        mMain.post(this::hideProgress);
                    }
                });
            });
        }
    }

    private void confirmRestore(Runnable action) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.restaurer)
                .setMessage(R.string.import_confirm)
                .setPositiveButton(R.string.yes, (d, w) -> action.run())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showProgress() {
        if (isFinishing()) {
            return;
        }
        if (mProgress == null) {
            mProgress = new ProgressDialog(this);
            mProgress.setMessage(getString(R.string.drive_in_progress));
            mProgress.setCancelable(false);
        }
        if (!mProgress.isShowing()) {
            mProgress.show();
        }
    }

    private void hideProgress() {
        if (mProgress != null && mProgress.isShowing()) {
            try {
                mProgress.dismiss();
            } catch (Exception ignored) {
            }
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary(newValue.toString());
        return true;
    }
}
