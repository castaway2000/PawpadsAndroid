package saberapplications.pawpads.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import saberapplications.pawpads.R;
import saberapplications.pawpads.databinding.AboutViewBinding;

/**
 * Created by blaze on 2/25/2016.
 */
public class AboutActivity extends AppCompatActivity {

    Intent browserIntent;
    AboutViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=DataBindingUtil.setContentView(this,R.layout.about_view);
        binding.setActivity(this);
        PackageInfo pInfo;

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            binding.setVersion(getString(R.string.version) +" " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {

        }

        binding.tvBlazeFA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.furaffinity.net/user/blaze-collie"));
                startActivity(browserIntent);
            }
        });
        binding.tvBlazeTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/blazecollie"));
                startActivity(browserIntent);

            }
        });

        binding.iconDesign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.furaffinity.net/user/riverbreak"));
                startActivity(browserIntent);
            }
        });

    }

    public void sendFeedback(){
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","szablya@gmail.com", null));
        startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)));
    }

}
