package com.amenity.amenity;

import android.*;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class InfoActivity extends AppCompatActivity {
    public RecyclerView.LayoutManager layoutManager;
    public RecyclerView recyclerView;
    public ArrayList<String> resources;
    public ArrayList<Integer> need;
    public ArrayList<Integer> recv;
    public AmenityMarker currDetails;
    public String number;
    public ResourceAdapter resourceAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        String k = getIntent().getStringExtra("pid");
        //Toast.makeText(this, k, Toast.LENGTH_SHORT).show();
        DatabaseReference entryRef = FirebaseDatabase.getInstance().getReference("markers/"+k);
        entryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AmenityMarker amenityMarker = dataSnapshot.getValue(AmenityMarker.class);
                currDetails = amenityMarker;
                if(currDetails!=null) {
                    resources = currDetails.resources;
                    need = currDetails.need;
                    recv = currDetails.recv;
                }
                TextView textView = (TextView) findViewById(R.id.postedText);
                textView.setText("This was posted by "+ currDetails.userName);
                TextView textView1 = (TextView) findViewById(R.id.message);
                textView1.setText(currDetails.message);
                number = currDetails.phone;
                recyclerView = (RecyclerView) findViewById(R.id.itemsRecyclerView);
                layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(layoutManager);
                resourceAdapter = new ResourceAdapter(getApplicationContext(),resources,need,recv);
                recyclerView.setAdapter(resourceAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Button button = (Button) findViewById(R.id.callButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(number!=null) makeCall(number);
            }
        });



        /*resources = getIntent().getStringArrayListExtra("resources");
        need = getIntent().getIntegerArrayListExtra("need");
        recv = getIntent().getIntegerArrayListExtra("recv");*/
        //.add("Water");
        //names.add("Food");
       // need.add(5);
        //need.add(4);
        //recv.add(1);
        //recv.add(2);
        /*resourceAdapter = new ResourceAdapter(getApplicationContext(),resources,need,recv);
        recyclerView.setAdapter(resourceAdapter);*/
    }

    private void makeCall(String number) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", number, null));
            startActivity(intent);
            finish();
        }
    }
}
