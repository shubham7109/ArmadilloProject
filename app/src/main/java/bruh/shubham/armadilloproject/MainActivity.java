package bruh.shubham.armadilloproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.PictureInPictureParams;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bruh.shubham.armadilloproject.Models.CustomGridLayoutManager;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements ApplicationAdapter.ItemClickListener {

    private static final int READ_EXTERNAL_STORAGE = 123;
    private static int SCREEN_HEIGHT;
    private PackageManager packageManager = null;
    private List<ApplicationInfo> applist = null;
    private ApplicationAdapter listadaptor = null;
    private Context context;
    private RecyclerView recyclerView;
    private ConstraintLayout layout;
    private Realm realm;

    @Override public void onPause() {
        super.onPause();
    }

    @Override public void onResume() {
        hideSystemUI();
        overridePendingTransition(R.anim.fadeout, R.anim.fadein);
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpActivity();
        new LoadApplications().execute();
    }

    @Override public void onBackPressed() {
        // Does nothing ... yet :)
    }

    private void setUpActivity(){
        hideSystemUI();
        askPermissions();
        setUpViews();
        setUpVariables();
    }
    /**
     * Creates and returns the default realm instance
     * @return default realm instance
     */
    public Realm getRealm(){
        if(realm == null){
            realm = Realm.getDefaultInstance();
        }
        return realm;
    }

    private void askPermissions(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpWallpaper(true);
                } else {
                    Snackbar.make(getWindow().getDecorView().getRootView(),"Read storage permission is needed to set Wallpaper.",Snackbar.LENGTH_LONG).setAction("enable", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    }).show();

                    setUpWallpaper(false);
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void setUpVariables(){
        context = this;
        packageManager = getPackageManager();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        SCREEN_HEIGHT = displayMetrics.heightPixels;
    }

    private void setUpViews(){
        recyclerView = (RecyclerView) findViewById(R.id.list);
        layout = findViewById(R.id.layout);
    }

    private void setUpWallpaper(boolean permission){
        if(permission)
            layout.setBackground(WallpaperManager.getInstance(this).getDrawable());
        else  layout.setBackgroundResource(R.drawable.gradient);
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
     // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

        private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
            ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();
            for (ApplicationInfo info : list) {
                try {
                    if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {
                        applist.add(info);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return applist;
        }

    @Override
    public void onItemClick(View view, int position) {
        ApplicationInfo app = applist.get(position);
        try {
            Intent intent = packageManager
                    .getLaunchIntentForPackage(app.packageName);

            if (null != intent) {
                startActivity(intent);
                overridePendingTransition(R.anim.fadeout, R.anim.fadein);
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadApplications extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {
                applist = checkForLaunchIntent(packageManager.getInstalledApplications
                        (PackageManager.GET_META_DATA));

                return null;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
            }

            int saveY =0;

            @Override
            protected void onPostExecute(Void result) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                linearLayoutManager.setReverseLayout(true);
                linearLayoutManager.offsetChildrenVertical(100);
                recyclerView.setLayoutManager(linearLayoutManager);
                Collections.sort(applist, new Comparator<ApplicationInfo>() {
                    @Override
                    public int compare(ApplicationInfo lhs, ApplicationInfo rhs) {
                        return lhs.loadLabel(context.getPackageManager()).toString().compareTo(rhs.loadLabel(context.getPackageManager()).toString());
                    }
                });
                listadaptor = new ApplicationAdapter(context, applist);
                listadaptor.setClickListener((ApplicationAdapter.ItemClickListener) context);
                recyclerView.setAdapter(listadaptor);
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        saveY = saveY + dy;
                        if (dy > 0) {
                            Log.e("Scrolling up","(dy,dx) " + dy+ ","+ dx);
                        } else {
                            Log.e("Scrolling down","(dy,dx) " + dy+ ","+ dx);
                        }
                    }

                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);

                        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                            Log.e("Fling","State");
                        } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                            Log.e("Touch","State");
                        } else {
                            Log.e("SAVEY: ",saveY+"");
                            saveY = 0;
                        }
                    }
                });
                super.onPostExecute(result);
            }
        }
    }