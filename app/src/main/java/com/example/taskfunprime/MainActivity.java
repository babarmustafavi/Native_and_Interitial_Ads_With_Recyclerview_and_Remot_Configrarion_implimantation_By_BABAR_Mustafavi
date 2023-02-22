package com.example.taskfunprime;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskfunprime.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    DatabaseReference dbreference;
    StorageReference storageReference;
    String taskdata = null;
    private ActivityMainBinding binding;
    private MainAdapter adapter;
    private List<Main> mainList;
    // Remote Config keys
    private static final String LOADING_PHRASE_CONFIG_KEY = "loading_phrase";
    private static final String WELCOME_MESSAGE_KEY = "welcome_message";
    private static final String WELCOME_MESSAGE_CAPS_KEY = "welcome_message_caps";
    FirebaseRemoteConfig mFirebaseRemoteConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dbreference = FirebaseDatabase.getInstance().getReference().child("UserInformation");
        storageReference = FirebaseStorage.getInstance().getReference();
        mainList = new ArrayList<>();
        //simple remot configration demo
         mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        //add data from drawble to arrylist
        mainList.add(new Main(R.drawable.image1));
        mainList.add(new Main(R.drawable.image2));
        mainList.add(new Main(R.drawable.image3));
        mainList.add(new Main(R.drawable.image4));
        mainList.add(new Main(R.drawable.image5));
        mainList.add(new Main(R.drawable.image6));
        mainList.add(new Main(R.drawable.image7));
        mainList.add(new Main(R.drawable.image8));
        mainList.add(new Main(R.drawable.image9));
        mainList.add(new Main(R.drawable.image10));


        //set layoutmanger and adapter
        binding.rvMain.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        binding.rvMain.setLayoutManager(gridLayoutManager);
        //  binding.rvMain.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainAdapter(MainActivity.this, mainList);
        binding.rvMain.setAdapter(adapter);
        //adapter item click lisner
        adapter.setOnItemClickListener(new MainAdapter.setdat() {
            @Override
            public void onItemClickListener(View view, int position, String name) {
                savedata(name);
            }
        });
        binding.imagename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               loadingdata();
            }
        });

    }
    public void loadingdata(){
        binding.imagename.setText(mFirebaseRemoteConfig.getString(LOADING_PHRASE_CONFIG_KEY)+"setDataComplete");
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    boolean updated = task.getResult();
                    Log.d(TAG, "Config params updated: " + updated);
                    //Toast.makeText(MainActivity.this, "Fetch and activate succeeded", Toast.LENGTH_SHORT).show();
                    String value = mFirebaseRemoteConfig.getString("key");
                } else {
                    Toast.makeText(MainActivity.this, "Fetch failed",
                            Toast.LENGTH_SHORT).show();
                }
                displayWelcomeMessage();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    private void displayWelcomeMessage() {
        // [START get_config_values]
        String welcomeMessage = mFirebaseRemoteConfig.getString(WELCOME_MESSAGE_KEY);
        // [END get_config_values]
        if (mFirebaseRemoteConfig.getBoolean(WELCOME_MESSAGE_CAPS_KEY)) {
            binding.imagename.setAllCaps(true);
        } else {
            binding.imagename.setAllCaps(false);
        }
        binding.imagename.setText(welcomeMessage+"setMC");
        Toast.makeText(this, "welcome_message="+welcomeMessage, Toast.LENGTH_SHORT).show();
    }
    public void setremotconfig(String data) {
        binding.imagename.setText(data);
    }

    //save data on firebase like update
    public void savedata(String name) {
        final StorageReference uploader = storageReference.child("userid");
        final Map<String, Object> map = new HashMap<>();
        map.put("ImageName", name);
        dbreference.child("userid").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // boolean ok=g. updateData(nameput,emailput,paassput);
                if (snapshot.exists())
                    dbreference.child("userid").updateChildren(map);
                else
                    dbreference.child("userid").setValue(map);

                setdata();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
//set data with textview
    public void setdata() {
        if (isNetworkAvailable()) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            DatabaseReference uDatabaseReference = FirebaseDatabase.getInstance().getReference().child("UserInformation");

            uDatabaseReference.child("userid").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String ImageName = dataSnapshot.child("ImageName").getValue().toString();
                    if (ImageName != "") {
                        binding.imagen.setText("Selected Image" + ImageName);
                    } else {
                        binding.imagen.setText("Image Name");
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        } else {
            Toast.makeText(this, "please data on and try agian", Toast.LENGTH_SHORT).show();

        }
    }

    //internet chacking
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}