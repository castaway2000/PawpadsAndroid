package saberapplications.pawpads.ui.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.ActivitySettingsBinding;
import saberapplications.pawpads.databinding.BindableBoolean;
import saberapplications.pawpads.databinding.BindableString;
import saberapplications.pawpads.service.UserLocationService;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.ui.login.LoginActivity;

/**
 * Created by blaze on 3/24/2016.
 */
public class PrefrenceActivity extends BaseActivity {
    private SharedPreferences preferences;

    ActivitySettingsBinding binding;

    public BindableString range=new BindableString();
    public BindableString unit=new BindableString();
    public BindableString accuracy=new BindableString();
    public BindableBoolean pushes=new BindableBoolean();
    public BindableBoolean popups=new BindableBoolean();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_settings);
        binding.setActivity(this);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setTitle(null);
        TextView  textView= (TextView) findViewById(R.id.toolbar_title);
        textView.setText(R.string.settings);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        preferences = PreferenceManager.getDefaultSharedPreferences(PrefrenceActivity.this);


        accuracy.set( preferences.getString("accuracy", "medium"));

        unit.set(preferences.getString(C.MEASURE_UNIT, "KM"));

        range.set(String.valueOf(preferences.getInt("range", 60)));

        range.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                SharedPreferences.Editor editor=preferences.edit();
                int r;
                if(range.get().equals("")){
                    r=0;
                }else {
                    r=Integer.parseInt(range.get());
                }

                editor.putInt(C.RANGE,r);
                if (unit.equals("MI")) {
                    int rangeM = (int) Math.floor(r * 1.60934);
                    editor.putInt(C.RANGE_KM, rangeM);
                    Util.RANGE=rangeM;
                } else {
                    editor.putInt(C.RANGE_KM, r);
                    Util.RANGE=r;
                }
                editor.apply();

            }
        });
        binding.etRange.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && range.get().equals("")){
                    range.set("0");
                }
            }
        });
        pushes.set(preferences.getBoolean(C.PUSH, true));

        pushes.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean(C.PUSH,pushes.get());
                editor.apply();
                Util.PUSH_NOTIFICIATIONS=pushes.get();
            }
        });

        popups.set( preferences.getBoolean(C.ALERT, true));

        popups.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                SharedPreferences.Editor editor=preferences.edit();
                editor.putBoolean(C.ALERT,popups.get());
                editor.apply();
                Util.IM_ALERT=popups.get();
            }
        });

        binding.units.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rbKM:
                        unit.set("KM");
                        break;
                    case R.id.rbMI:
                        unit.set("MI");
                        break;
                }
                SharedPreferences.Editor editor=preferences.edit();
                editor.putString(C.MEASURE_UNIT,unit.get());
                editor.apply();
                Util.UNIT_OF_MEASURE=unit.get();
            }
        });

        binding.accuracy.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rbLow:
                        accuracy.set(C.ACCURACY_LOW);
                        break;
                    case R.id.rbMedium:
                        accuracy.set(C.ACCURACY_MEDIUM);
                        break;
                    case R.id.rbHigh:
                        accuracy.set(C.ACCURACY_HIGH);
                        break;
                }
                SharedPreferences.Editor editor=preferences.edit();
                editor.putString(C.ACCURACY,accuracy.get());
                editor.apply();
                Util.ACCURACY=accuracy.get();
            }
        });


        TextView appVersion = (TextView) findViewById(R.id.app_version);
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersion.setText(getString(R.string.version) + String.format("%s(%d)", pInfo.versionName, pInfo.versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }


    public void deleteUserProfile() {
        DeleteAccountConfirmationDialog dialog = new DeleteAccountConfirmationDialog();
        dialog.show(getSupportFragmentManager(),"dialog");
        dialog.setCallback(new DeleteAccountConfirmationDialog.Callback() {
            @Override
            public void onDelete() {
                final ProgressDialog progressDialog=new ProgressDialog(PrefrenceActivity.this,R.style.AppAlertDialogTheme);
                progressDialog.setMessage(getString(R.string.deleting_profile));
                progressDialog.show();
                progressDialog.setCancelable(false);
                QBUsers.deleteUser(preferences.getInt(C.QB_USERID, -1), new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        progressDialog.dismiss();
                        Toast.makeText(PrefrenceActivity.this, R.string.profile_was_removed, Toast.LENGTH_LONG).show();
                        Intent myIntent1 = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(myIntent1);
                        finish();
                        LocalBroadcastManager.getInstance(PrefrenceActivity.this).sendBroadcast(new Intent(C.CLOSE_ALL_APP_ACTIVITIES));
                        SharedPreferences.Editor editor=preferences.edit();
                        editor.clear();
                        editor.apply();
                        UserLocationService.stop();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        progressDialog.dismiss();
                        Util.onError(e, PrefrenceActivity.this);
                    }

                });
        }});

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
