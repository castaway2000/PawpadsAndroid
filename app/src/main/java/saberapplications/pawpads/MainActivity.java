package saberapplications.pawpads;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView listView;

    int i =0;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        setUserData();
        i=1;

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        performClickAction(position);
                    }
                }
        );
    }

    public void setUserData() {

        i=2;
        UserData ud = new UserData(this);
        ud.getUserData();
        final ListAdapter listAdapter = new CustomAdapter(this, ud.user, ud.upics, ud.descr, ud.geol);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(listAdapter);
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }



    public void performClickAction(int position) {

        i=3;
        UserData u = new UserData(this);


        u.getUserData();
        final CustomAdapter ca = new CustomAdapter(this, u.user, u.upics, u.descr, u.geol);

        Intent i = new Intent(MainActivity.this, profilepage.class);
        i.putExtra("value", ca.descrip[position]);
        i.putExtra("image", ca.pics[position]);
        i.putExtra("username", ca.profile[position]);
        i.putExtra("location", ca.geoloc[position]);
        startActivity(i);
    }
    @Override
    public void onRefresh() {
        setUserData();
    }
}

