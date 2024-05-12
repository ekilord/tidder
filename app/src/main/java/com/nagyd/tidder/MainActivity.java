package com.nagyd.tidder;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nagyd.tidder.databinding.ActivityMainBinding;
import com.nagyd.tidder.databinding.NavHeaderMainBinding;
import com.nagyd.tidder.firebase.Auth;

import com.nagyd.tidder.firebase.Database;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {
    private static final int NOTIFICATION_ID = 1;
    private static final int REQUEST_CODE_NOTIFICATION_PERMISSION = 101;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Auth.init();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_feed, R.id.nav_login, R.id.nav_sublist, R.id.nav_profile)
                .setOpenableLayout(drawer)
                .build();



        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                logout();
            } else {
                NavigationUI.onNavDestinationSelected(item, navController);
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        updateUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();

        this.recreate();
        overridePendingTransition(0, 0);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(R.id.nav_feed);

        Toast.makeText(this, "You have been logged out", Toast.LENGTH_SHORT).show();
    }

    public void updateUI() {
        FirebaseUser currentUser = Auth.mAuth.getCurrentUser();

        Menu menu = binding.navView.getMenu();
        MenuItem loginItem = menu.findItem(R.id.nav_login);
        //MenuItem sublistItem = menu.findItem(R.id.nav_sublist);
        MenuItem profileItem = menu.findItem(R.id.nav_profile);
        MenuItem logoutItem = menu.findItem(R.id.nav_logout);


        NavHeaderMainBinding headerBinding = NavHeaderMainBinding.bind(binding.navView.getHeaderView(0));
        TextView headerUsernameField = headerBinding.navHeaderUsername;
        TextView headerEmailField = headerBinding.navHeaderEmail;

        if (currentUser != null) {

            Database db = new Database();

            db.getUserViaEmail(currentUser.getEmail()).thenAccept(user -> {
                headerUsernameField.setText(user.username);
                headerEmailField.setText(user.email);
            });

            loginItem.setVisible(false);
            //sublistItem.setVisible(true);
            profileItem.setVisible(true);
            logoutItem.setVisible(true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermission();
            }

        } else {
            headerUsernameField.setText("Guest");
            headerEmailField.setText("");

            loginItem.setVisible(true);
            //sublistItem.setVisible(false);
            profileItem.setVisible(false);
            logoutItem.setVisible(false);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private NotificationChannel createNotificationChannel() {
        String channelId = "welcome_channel";

        String channelName = "Welcome Channel";

        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);

        channel.setDescription("Channel used to welcome users");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        return channel;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotification() {
        NotificationChannel channel = createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel.getId())
                .setContentTitle("tidder")
                .setContentText("Welcome to the tidder community!")
                .setSmallIcon(R.drawable.icon_user)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE));

        Notification notification = builder.build();

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestNotificationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_CODE_NOTIFICATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showNotification();
            }
        }
    }

}