package com.amenity.amenity;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;
import android.widget.ArrayAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class InputActivity extends AppCompatActivity {
    Double latitude;
    Double longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.markerdetails_layout);
        latitude = getIntent().getDoubleExtra("latitude",0.0);
        longitude = getIntent().getDoubleExtra("longitude",0.0);

        final EditText message = (EditText) findViewById(R.id.message);
        final EditText phone = (EditText) findViewById(R.id.phone);
        //final EditText qty = (EditText) findViewById(R.id.qty);
        //Spinner mySpinner = (Spinner) findViewById(R.id.spinner);
       // ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
         //       R.array.resources, android.R.layout.simple_spinner_item);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //mySpinner.setAdapter(adapter);
        //final String resource = mySpinner.getSelectedItem().toString();
        final ArrayList<String> resources=new ArrayList<>();
        final ArrayList<Integer> need=new ArrayList<>(0);
        final ArrayList<Integer> recv=new ArrayList<>(0);





        final CheckBox chkwater=(CheckBox)findViewById(R.id.cb1);
        final CheckBox chkblanket=(CheckBox)findViewById(R.id.cb2);
        final CheckBox chkfood=(CheckBox)findViewById(R.id.cb3);
        final CheckBox chkmedicines=(CheckBox)findViewById(R.id.cb4);
        final CheckBox chktoiletries=(CheckBox)findViewById(R.id.cb5);
        final EditText qtywater=(EditText)findViewById(R.id.qtywater);
        final EditText qtyblanket=(EditText)findViewById(R.id.qtyblanket);
        final EditText qtyfood=(EditText)findViewById(R.id.qtyfood);
        final EditText qtymedicines=(EditText)findViewById(R.id.qtymedicines);
        final EditText qtytoiletries=(EditText)findViewById(R.id.qtytoiletries);
        Button share = (Button) findViewById(R.id.sharebutton);

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phone.getEditableText().toString().length() != 10) {
                    Toast.makeText(getApplicationContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();

                    /*if(chkwater.isChecked()){
                        Toast.makeText(getApplicationContext(),"Hello",Toast.LENGTH_SHORT).show();
                    }*/
                }
                if(((chkwater.isChecked())&&(TextUtils.isEmpty(qtywater.getText().toString())))||((chkblanket.isChecked())&&(qtyblanket.getText().toString().matches("")))||((chkfood.isChecked())&&(qtyfood.getText().toString().matches("")))||((chkmedicines.isChecked())&&(qtymedicines.getText().toString().matches("")))||((chktoiletries.isChecked())&&(qtytoiletries.getText().toString().matches("")))) {
                    Toast.makeText(getApplicationContext(), "Enter quantity for checked item", Toast.LENGTH_SHORT).show();
                }
                else {

                    if(chkwater.isChecked()){
                        resources.add("Water");
                        need.add(Integer.parseInt(qtywater.getText().toString()));
                    }

                    if(chkblanket.isChecked()) {
                        resources.add("Blankets");
                        need.add(Integer.parseInt(qtyblanket.getText().toString()));
                    }
                    if(chkfood.isChecked()) {
                        resources.add("Food");
                        need.add(Integer.parseInt(qtyfood.getText().toString()));
                    }
                    if(chkmedicines.isChecked()) {
                        resources.add("Medicines");
                        need.add(Integer.parseInt(qtymedicines.getText().toString()));
                    }
                    if(chktoiletries.isChecked()) {
                        resources.add("Toiletries");
                        need.add(Integer.parseInt(qtytoiletries.getText().toString()));
                    }
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    /*intent.putExtra("message", message.getText().toString());
                    intent.putStringArrayListExtra("resources",resources);
                    intent.putIntegerArrayListExtra("need",need);
                    intent.putIntegerArrayListExtra("recv",recv);

                    */
                    //intent.putExtra("userName",userName.getText().toString());
                    /*intent.putExtra("phone", phone.getText().toString());
                    intent.putExtra("resource", resource);*/
                    //intent.putExtra("quantity", qty.getText().toString());
                    /*HashMap<String,Integer> hashMap = new HashMap<String, Integer>();
                    HashMap<String,Integer> reached = new HashMap<String, Integer>();
                    hashMap.put(resource, Integer.parseInt(qty.getEditableText().toString()));
                    reached.put(resource, 0);*/
                    //ArrayList<String> resources = new ArrayList<String>();
                    //resources.add(resource);
                    //ArrayList<Integer> need = new ArrayList<Integer>();
                    //need.add(Integer.parseInt(qty.getEditableText().toString()));
                    //ArrayList<Integer> recv = new ArrayList<Integer>();
                    //recv.add(0);
                    Date date = Calendar.getInstance().getTime();
                    AmenityMarker amenityMarker = new AmenityMarker(latitude,longitude,resources,need,recv,message.getEditableText().toString(), date, FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),phone.getEditableText().toString());
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("markers").push();
                    ref.setValue(amenityMarker);
                    String k = ref.getKey();
                    FirebaseDatabase.getInstance().getReference("markers/"+k+"/pid").setValue(k);
                    Log.v("InputActivity",ref.getKey().toString());
                    //Toast.makeText(InputActivity.this,ref.getKey() , Toast.LENGTH_SHORT).show();
                    //setResult(2, intent);
                    //finish();//finishing activity
                    //Intent intent1 = new Intent();
                    startActivity(intent);
                }
            }
        });

        Button smsButton = (Button) findViewById(R.id.sendViaSMS);
        smsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (phone.getEditableText().toString().length() != 10) {
                    Toast.makeText(getApplicationContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();

                    /*if(chkwater.isChecked()){
                        Toast.makeText(getApplicationContext(),"Hello",Toast.LENGTH_SHORT).show();
                    }*/
                }
                if (((chkwater.isChecked()) && (TextUtils.isEmpty(qtywater.getText().toString()))) || ((chkblanket.isChecked()) && (qtyblanket.getText().toString().matches(""))) || ((chkfood.isChecked()) && (qtyfood.getText().toString().matches(""))) || ((chkmedicines.isChecked()) && (qtymedicines.getText().toString().matches(""))) || ((chktoiletries.isChecked()) && (qtytoiletries.getText().toString().matches("")))) {
                    Toast.makeText(getApplicationContext(), "Enter quantity for checked item", Toast.LENGTH_SHORT).show();
                } else {

                    if (chkwater.isChecked()) {
                        resources.add("Water");
                        need.add(Integer.parseInt(qtywater.getText().toString()));
                    }

                    if (chkblanket.isChecked()) {
                        resources.add("Blankets");
                        need.add(Integer.parseInt(qtyblanket.getText().toString()));
                    }
                    if (chkfood.isChecked()) {
                        resources.add("Food");
                        need.add(Integer.parseInt(qtyfood.getText().toString()));
                    }
                    if (chkmedicines.isChecked()) {
                        resources.add("Medicines");
                        need.add(Integer.parseInt(qtymedicines.getText().toString()));
                    }
                    if (chktoiletries.isChecked()) {
                        resources.add("Toiletries");
                        need.add(Integer.parseInt(qtytoiletries.getText().toString()));
                    }
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    /*intent.putExtra("message", message.getText().toString());
                    intent.putStringArrayListExtra("resources",resources);
                    intent.putIntegerArrayListExtra("need",need);
                    intent.putIntegerArrayListExtra("recv",recv);

                    */
                    //intent.putExtra("userName",userName.getText().toString());
                    /*intent.putExtra("phone", phone.getText().toString());
                    intent.putExtra("resource", resource);*/
                    //intent.putExtra("quantity", qty.getText().toString());
                    /*HashMap<String,Integer> hashMap = new HashMap<String, Integer>();
                    HashMap<String,Integer> reached = new HashMap<String, Integer>();
                    hashMap.put(resource, Integer.parseInt(qty.getEditableText().toString()));
                    reached.put(resource, 0);*/
                    //ArrayList<String> resources = new ArrayList<String>();
                    //resources.add(resource);
                    //ArrayList<Integer> need = new ArrayList<Integer>();
                    //need.add(Integer.parseInt(qty.getEditableText().toString()));
                    //ArrayList<Integer> recv = new ArrayList<Integer>();
                    //recv.add(0);
                    Date date = Calendar.getInstance().getTime();
                    AmenityMarker amenityMarker = new AmenityMarker(latitude, longitude, resources, need, recv, message.getEditableText().toString(), date, FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), phone.getEditableText().toString());
                    //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("markers").push();
                    //ref.setValue(amenityMarker);
                    //String k = ref.getKey();
                    //FirebaseDatabase.getInstance().getReference("markers/"+k+"/pid").setValue(k);
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage("+18329253858",null,latitude.toString() + "," + longitude.toString() + "," + amenityMarker.message + "," + amenityMarker.phone,null,null);

                    //startActivity(intent);
                }
            }
        });
    }

}
