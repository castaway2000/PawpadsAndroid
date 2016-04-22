package saberapplications.pawpads.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.QBUsers;

import java.util.List;

import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.ui.login.LoginActivity;

/**
 * Created by blaze on 3/24/2016.
 */
public class PrefrenceActivity extends BaseActivity implements View.OnClickListener {
    private SharedPreferences defaultSharedPreferences;
    Button removeProfile, savebtn;
    EditText dist;
    CheckBox tbMetricBox, tbImBox, tbPushBox;
    RadioGroup rbGroup;
    RadioButton rbHigh, rbMedium, rbLow;
    int range, gRange, accuracy, gAccuracy;
    public String unit, gUnit;
    Boolean alert, push, gAlert, gPush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("PawPads | Settings");
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(PrefrenceActivity.this);
        dist = (EditText) findViewById(R.id.etRange);
        tbMetricBox = (CheckBox) findViewById(R.id.ckMetric);
        tbPushBox = (CheckBox) findViewById(R.id.ckPushNotifications);
        tbImBox = (CheckBox) findViewById(R.id.ckImNotification);
        rbLow = (RadioButton) findViewById(R.id.rbLow);
        rbMedium = (RadioButton) findViewById(R.id.rbMedium);
        rbHigh = (RadioButton) findViewById(R.id.rbHigh);

        //TODO: create GUI for accuracy settings.
        gAccuracy = defaultSharedPreferences.getInt("accuracy", 6);
        gRange = defaultSharedPreferences.getInt("range", 60);
        dist.setText(String.valueOf(gRange));

        gUnit = defaultSharedPreferences.getString("unit", "standard");
        gAlert = defaultSharedPreferences.getBoolean("alert", true);
        gPush = defaultSharedPreferences.getBoolean("push", true);

        //check box funtionality. defaults: standard, true, true;
        if(gUnit.equals("standard")){
            tbMetricBox.setChecked(false);
        } else { tbMetricBox.setChecked(true);}
        if(!gPush){
            tbPushBox.setChecked(false);
        } else {tbPushBox.setChecked(true);}
        if(!gAlert){
            tbImBox.setChecked(false);
        } else { tbImBox.setChecked(true); }
        if(gAccuracy == 1){
            rbLow.setChecked(true);
            rbMedium.setChecked(false);
            rbHigh.setChecked(false);

        }
        else if(gAccuracy == 3){
            rbLow.setChecked(false);
            rbMedium.setChecked(true);
            rbHigh.setChecked(false);
        }
        else{
            rbLow.setChecked(false);
            rbMedium.setChecked(false);
            rbHigh.setChecked(true);
        }


        savebtn = (Button) findViewById(R.id.btProfSave);
        removeProfile = (Button) findViewById(R.id.btRmProfile);
        savebtn.setOnClickListener(this);
        removeProfile.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 buildRemoveAlertDialog();
                                             }
                                         }
        );
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btProfSave:
                if(dist.getText().length() > 0){
                    range = Integer.valueOf(String.valueOf(dist.getText()));
                }else { range = 60; }

                if(tbMetricBox.isChecked()){
                    unit = "metric";
                } else { unit = "standard"; }

                if(tbPushBox.isChecked()){
                   push = true;
                } else { push = false; }

                if (tbImBox.isChecked()){
                    alert = true;
                } else { alert = false; }
                if(rbLow.isChecked()){
                    accuracy = 1;
                }
                else if (rbMedium.isChecked()){
                    accuracy = 3;
                }else{ accuracy = 6; }

                saveSettings(accuracy, range, unit, alert, push);
                break;
        }
    }

    public void saveSettings(int accuracy, int range, String unit, Boolean alert, Boolean push ){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(PrefrenceActivity.this).edit();
        editor.putInt("accuracy", accuracy);
        editor.putInt("range", range);
        editor.putString("unit", unit);
        editor.putBoolean("alert", alert);
        editor.putBoolean("push", push);
        editor.apply();

        Util.ACCURACY = accuracy;
        Util.PUSH_NOTIFICIATIONS = push;
        Util.IM_ALERT = alert;
        Util.UNIT_OF_MEASURE = unit;

    }

    private void buildRemoveAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(PrefrenceActivity.this);
        alertDialog.setTitle("Remove profile");
        alertDialog.setMessage("Enter Password");
        final EditText input = new EditText(PrefrenceActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().isEmpty() && input.getText().toString().equals(defaultSharedPreferences.getString(Util.QB_PASSWORD, ""))) {
                            QBUsers.deleteUser(defaultSharedPreferences.getInt(Util.QB_USERID, -1), new QBEntityCallbackImpl() {

                                @Override
                                public void onSuccess() {
                                    Toast.makeText(PrefrenceActivity.this, "Remove profile successfully", Toast.LENGTH_LONG).show();
                                    Intent myIntent1 = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(myIntent1);
                                    finish();
                                }

                                @Override
                                public void onError(List errors) {
                                    Util.onError(errors, PrefrenceActivity.this);
                                }
                            });
                        } else {
                            Toast.makeText(PrefrenceActivity.this, "Wrong password", Toast.LENGTH_LONG).show();
                        }


                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

}
