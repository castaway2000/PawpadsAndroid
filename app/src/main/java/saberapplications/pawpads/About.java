package saberapplications.pawpads;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by blaze on 2/25/2016.
 */
public class About extends AppCompatActivity implements View.OnClickListener {
    TextView riverbreak, blazecollie;
    Intent browserIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_view);
        setTitle("PawPads | About");
        blazecollie = (TextView) findViewById(R.id.tvBlazeFA);
        riverbreak = (TextView) findViewById(R.id.tvRiverbreakFA);

        blazecollie.setOnClickListener(this);
        riverbreak.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvBlazeFA:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(blazecollie.getText().toString()));
                startActivity(browserIntent);
                break;

            case R.id.tvRiverbreakFA:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(riverbreak.getText().toString()));
                startActivity(browserIntent);
                break;
        }
    }
}
