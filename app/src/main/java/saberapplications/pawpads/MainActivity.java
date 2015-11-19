package saberapplications.pawpads;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView listView;
    UserLocalStore userLocalStore;
    UserData ud = new UserData(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        userLocalStore = new UserLocalStore(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_profileID:
                Intent i = new Intent(MainActivity.this, profileEditPage.class);
                startActivity(i);
                return true;

            case R.id.action_logout:
                //TODO: set logout functionality
                userLocalStore = new UserLocalStore(this);
                userLocalStore.clearUserData();
                userLocalStore.setUserLoggedIn(false);
                startActivity(new Intent(this, Login.class));
                finish();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUserData();
        if(authenticate()) {
            //TODO: run main event
            listView.setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            performClickAction(position);
                        }
                    }
            );
        }
        else {
            startActivity(new Intent(MainActivity.this, Login.class));
            finish();
        }
    }

    private boolean authenticate(){
        return userLocalStore.getUserLoggedIn();
    }

    public void setUserData() {
        ud.getUserData();
    }

    public void setListView(UserList userList){
        final ListAdapter listAdapter = new CustomAdapter(this, ud.user, ud.upics, ud.descr, ud.geol);
        listView.setAdapter(listAdapter);

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public void performClickAction(int position) {
        ud.getUserData();
        final CustomAdapter ca = new CustomAdapter(this, ud.user, ud.upics, ud.descr, ud.geol);

        Intent i = new Intent(MainActivity.this, profilepage.class);
        i.putExtra("value", ca.descrip[position]);
        i.putExtra("image", ca.pics[position]);
        i.putExtra("username", ca.user[position]);
        i.putExtra("location", ca.geoloc[position]);
        startActivity(i);
    }

    @Override
    public void onRefresh() {
        setUserData();
    }
}

