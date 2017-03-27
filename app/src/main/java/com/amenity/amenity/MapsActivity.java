package com.amenity.amenity;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.mediation.customevent.CustomEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    public DatabaseReference databaseReference;
    public DatabaseReference markerReference;
    public ArrayList<AmenityMarker> amenityMarkers;
    public ArrayList<AmenityMarker> searchResultMarkers;
    public ArrayList<AmenityMarker> helpers;
    public SearchView searchView;
    public Button button;
    public Button currentLocation;
    public MarkerOptions locationMarkerOptions;
    public Marker locationMarker;
    final int resultcode = 7000;
    private GoogleApiClient mGoogleApiClient;
    public Boolean mLocationPermissionGranted;
    public final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 77;
    LatLng myLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            Intent intent = new Intent(getApplicationContext(), GoogleSignInActivity.class);
            startActivity(intent);
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                Toast.makeText(MapsActivity.this, "GoogleAPIClient Connection Failed!", Toast.LENGTH_SHORT).show();
                                Log.v("ConnextionFailed",connectionResult.toString());
                            }
                        } /* OnConnectionFailedListener */)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
        amenityMarkers = new ArrayList<AmenityMarker>();
        searchResultMarkers = new ArrayList<AmenityMarker>();
        helpers = new ArrayList<AmenityMarker>();
        searchView = (SearchView) findViewById(R.id.searchText);
        searchView.setBackgroundColor(Color.WHITE);
        currentLocation = (Button) findViewById(R.id.currentLocation);
        currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
                if(myLocation!=null) {
                    changePositionOfLocationMarker(myLocation);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                }else{
                    Toast.makeText(MapsActivity.this, "Location error!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        locationMarkerOptions = new MarkerOptions().position(new LatLng(20.5937,78.9629)).title("Your Location").draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        //addCurrentLocationMarker();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Toast.makeText(MapsActivity.this, "You want to Search for "+query, Toast.LENGTH_SHORT).show();
                searchResultMarkers.clear();
                int found = 0;
                if(query.length()<=1) return true;
                String nq = Character.toUpperCase(query.charAt(0)) + String.valueOf(query.subSequence(1,query.length())).toLowerCase();
                for (int i = 0; i < amenityMarkers.size(); i++) {
                    AmenityMarker curr = amenityMarkers.get(i);
                    if (curr.resources.contains(nq)) {
                        found = 1;
                        searchResultMarkers.add(curr);
                    }
                }
                if (found == 0) {
                    Toast.makeText(MapsActivity.this, "Not found!", Toast.LENGTH_SHORT).show();
                }
                hideKeyboard();
                updateUIWithSearchResult();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //Toast.makeText(MapsActivity.this, "You pressed close", Toast.LENGTH_SHORT).show();
                hideKeyboard();
                clearMap();
                updateUI();
                return true;
            }
        });
        button = (Button) findViewById(R.id.toInput);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, InputActivity.class);
                intent.putExtra("latitude",locationMarker.getPosition().latitude);
                intent.putExtra("longitude",locationMarker.getPosition().longitude);
                startActivity(intent);
            }
        });
        button = (Button) findViewById(R.id.available);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, ShareActivity.class);
                intent.putExtra("latitude",locationMarker.getPosition().latitude);
                intent.putExtra("longitude",locationMarker.getPosition().longitude);
                startActivity(intent);
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("name");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Toast.makeText(MapsActivity.this, dataSnapshot.getValue(String.class), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        HashMap<String, Integer> hashMap = new HashMap<String, Integer>();
        hashMap.put("Water", 10);
        hashMap.put("Blankets", 5);
        hashMap.put("Food", 6);
        Date date = Calendar.getInstance().getTime();
        //amenityMarkers.add(new AmenityMarker(46, 12, hashMap, "Awesome", date, "IDK", "9841735072"));
        //addCurrentLocationMarker();
        updateUI();
        //FirebaseDatabase.getInstance().getReference("/markers").push().setValue(new AmenityMarker(67,54,hashMap,"Plz Help",date,"ashwini","9841735072"));
        //FirebaseDatabase.getInstance().getReference("/markers").push().setValue(new AmenityMarker(87,77,hashMap,"I need some help rn",date,"varun","9841735072"));
        /*DatabaseReference markerRef = FirebaseDatabase.getInstance().getReference("markers/");
        markerRef.push().setValue(new AmenityMarker(25,85,hashMap,"This is cool",date,"Varun","9841735072"));
        FirebaseDatabase.getInstance().getReference("markers/"+markerRef.getKey()).setValue(new Pair<String,String>("pid",markerRef.getKey()));*/
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                AmenityMarker marker = dataSnapshot.getValue(AmenityMarker.class);
                //Toast.makeText(MapsActivity.this, "Received Marker "+marker.userName, Toast.LENGTH_SHORT).show();
                if (marker != null) {
                    amenityMarkers.add(marker);
                    updateUI();
                } else {
                    Log.v("FirebaseRead", "null was Read");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        markerReference = FirebaseDatabase.getInstance().getReference("markers/");
        markerReference.addChildEventListener(childEventListener);

        ChildEventListener childEventListener1 = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                AmenityMarker marker = dataSnapshot.getValue(AmenityMarker.class);
                //Toast.makeText(MapsActivity.this, "Received Marker "+marker.userName, Toast.LENGTH_SHORT).show();
                if (marker != null) {
                    helpers.add(marker);
                    updateUI();
                } else {
                    Log.v("FirebaseRead", "null was Read");
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        DatabaseReference helperReference = FirebaseDatabase.getInstance().getReference("helpers/");
        helperReference.addChildEventListener(childEventListener1);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addCurrentLocationMarker();
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                //Toast.makeText(MapsActivity.this, "Dragging started at "+marker.getPosition().latitude+" "+marker.getPosition().longitude, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                //Toast.makeText(MapsActivity.this, "Dragging started at "+marker.getPosition().latitude+" "+marker.getPosition().longitude, Toast.LENGTH_SHORT).show();
                marker.setPosition(new LatLng(marker.getPosition().latitude,marker.getPosition().longitude));
            }
        });
        updateUI();
        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }


    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void clearMap() {
        try {
            mMap.clear();
        } catch (Exception ex) {
            Log.v("GoogleMaps", "Map not loaded");
        }
    }

    public void updateUI() {
        try {
            //mMap.clear();
            for (int i = 0; i < amenityMarkers.size(); i++) {
                AmenityMarker currentMarker = amenityMarkers.get(i);
                LatLng pos = new LatLng(currentMarker.latitude, currentMarker.longitude);
                //Change
                mMap.addMarker(new MarkerOptions().position(pos).title(currentMarker.message)).setTag(currentMarker.pid);
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            }

            for(int i=0;i< helpers.size();i++){
                AmenityMarker currentMarkers = helpers.get(i);
                LatLng pos = new LatLng(currentMarkers.latitude,currentMarkers.longitude);
                mMap.addMarker(new MarkerOptions().position(pos).title(currentMarkers.message).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).setTag(currentMarkers.pid);
            }
        } catch (Exception ex) {
            Log.v("GoogleMaps", "Map not loaded");
        }
    }

    public void updateUIWithSearchResult() {
        try {
            mMap.clear();
            for (int i = 0; i < searchResultMarkers.size(); i++) {
                AmenityMarker currentMarker = searchResultMarkers.get(i);
                LatLng pos = new LatLng(currentMarker.latitude, currentMarker.longitude);
                mMap.addMarker(new MarkerOptions().position(pos).title(currentMarker.message)).setTag(currentMarker.pid);
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            }
        } catch (Exception ex) {
            Log.v("GoogleMaps", "Map not loaded");
        }
    }

    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Boolean mLocationPermissionGranted = true;
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            myLocation = new LatLng(location.getLatitude(),location.getLongitude());
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        // A step later in the tutorial adds the code to get the device location.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    public void addCurrentLocationMarker(){
        locationMarker = mMap.addMarker(locationMarkerOptions);
    }

    public void changePositionOfLocationMarker(LatLng location){
        locationMarker.setPosition(location);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.v("PID",(String) marker.getTag());
        Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
        //Add Extras
        intent.putExtra("pid",marker.getTag().toString());
        startActivity(intent);
    }
}
