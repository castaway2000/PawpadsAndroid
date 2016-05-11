package saberapplications.pawpads.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;

/**
 * Created by blaze on 2/25/2016.
 */
public class AboutActivity extends AppCompatActivity implements View.OnClickListener {
    TextView riverbreak, blazecollie, blazecollieTwitter, feedback, versionNum;
    Intent browserIntent;
    String versionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_view);
        setTitle("PawPads | About");
        blazecollie = (TextView) findViewById(R.id.tvBlazeFA);
        blazecollieTwitter = (TextView) findViewById(R.id.tvBlazeTwitter);
        riverbreak = (TextView) findViewById(R.id.tvRiverbreakFA);
        versionNum = (TextView) findViewById(R.id.tvVersionNumber);

        feedback = (TextView) findViewById(R.id.tvFeedback);
        feedback.setText(Html.fromHtml("<a href=\"mailto:szablya@gmail.com\">Send Feedback</a>"));
        feedback.setMovementMethod(LinkMovementMethod.getInstance());
        versionNum.setText(Util.APP_VERSION);
        blazecollie.setOnClickListener(this);
        blazecollieTwitter.setOnClickListener(this);
        riverbreak.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvBlazeFA:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(blazecollie.getText().toString()));
                startActivity(browserIntent);
                break;
            case R.id.tvBlazeTwitter:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(blazecollieTwitter.getText().toString()));
                startActivity(browserIntent);
                break;
            case R.id.tvRiverbreakFA:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(riverbreak.getText().toString()));
                startActivity(browserIntent);
                break;
        }
    }
}
