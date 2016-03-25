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
    CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("PawPads | Settings");
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(PrefrenceActivity.this);
        dist = (EditText) findViewById(R.id.etRange);
        dist.setText(String.valueOf(Util.RANGE));
        checkBox = (CheckBox) findViewById(R.id.tbMetric);
        if(Util.UNIT_OF_MEASURE == "metric"){
            checkBox.setChecked(true);
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
                if(dist.getText().length() == 0){
                    Util.RANGE = 10;
                }
                else {
                    Util.RANGE = Integer.valueOf(String.valueOf(dist.getText()));
                }
                if(checkBox.isChecked()){
                    Util.UNIT_OF_MEASURE = "metric";
                    checkBox.setChecked(true);
                }
                else {Util.UNIT_OF_MEASURE = "standard";}
                break;
        }
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
