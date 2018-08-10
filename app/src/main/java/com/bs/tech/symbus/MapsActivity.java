package com.bs.tech.symbus;

/**
 * Created by bhumika on 28/3/18.
 */

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener
{
    private ArrayList<Bus> buses;
    private ArrayList<Marker> markers;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private String TAG = "MapsActivity ---> ";
    int count=0;
    private GoogleMap mMap;
    FloatingActionButton floatingActionButton, mapActionButton,
            ttActionButton, logoutActionButton;
    private int mapType;
    private int noSit=0, noSBRoad=0;
    TextView statusDetails;
    private android.widget.Toast mToast= null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        buses = new ArrayList<Bus>();
        markers = new ArrayList<Marker>();
        statusDetails= findViewById(R.id.statusDetails);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        handleThemes();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap)
    {
        mMap= googleMap;
        mMap.setOnInfoWindowClickListener(this);
        // configure map UI settings
        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapsActivity.this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this,
                    "Please provide location permissions", Toast.LENGTH_SHORT)
                    .show();
        }
        else
            mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);

        // Get the buses from DB
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReferenceFromUrl("https://symbus-jwt.firebaseio.com/")
                .child("Buses");
        ChildEventListener childEventListener = new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                MarkerOptions options = new MarkerOptions();
                Bus x = dataSnapshot.getValue(Bus.class);
                buses.add(x);
                if (x.getIsRunning().equalsIgnoreCase("true"))
                {
                    if(x.getDestn().equalsIgnoreCase("SIT"))
                    {
                        ++noSit;
                    }
                    else
                    {
                        ++noSBRoad;
                    }
                    statusDetails.setText("SB Road to SIT: "+noSit+"\nSIT to SB Road: "+
                            noSBRoad);
                    int height = 80;
                    int width = 80;
                    BitmapDrawable bitmapdraw=(BitmapDrawable)getResources()
                            .getDrawable(R.drawable.icon_bus);
                    Bitmap b=bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                    options.position(new LatLng(x.getLattitude(), x.getLongitude()))
                                .title(x.getBusNo())
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                .snippet("To "+x.getDestn());
                    markers.add(mMap.addMarker(options));
                }
                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(
                        x.getLattitude(), x.getLongitude()));
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
                mMap.moveCamera(center);
                mMap.animateCamera(zoom);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                int height = 80;
                int width = 80;
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources()
                        .getDrawable(R.drawable.icon_bus);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                MarkerOptions options= new MarkerOptions();
                Bus x= dataSnapshot.getValue(Bus.class);
                for (int i = 0; i < buses.size(); i++)
                {
                    //found the bus
                    if(buses.get(i).getBusNo().equals(x.getBusNo()))
                    {
                        // If isRunning status was changed
                        if((!x.getIsRunning().equalsIgnoreCase(buses.get(i).getIsRunning())))
                        {
                            //not running to running
                            if (x.getIsRunning().equalsIgnoreCase("true"))
                            {
                                if(x.getDestn().equalsIgnoreCase("SIT"))
                                {
                                    ++noSit;
                                }
                                else
                                {
                                    ++noSBRoad;
                                }
                                statusDetails.setText("SB Road to SIT: "+noSit+"\nSIT to SB Road: "+
                                        noSBRoad);
                                buses.set(i, x);
                                options.position(new LatLng(x.getLattitude(), x.getLongitude()))
                                        .title(x.getBusNo())
                                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                        .snippet("To "+x.getDestn());
                                markers.get(i).remove();
                                markers.set(i, mMap.addMarker(options));
                            }
                            //running to not running
                            else if (x.getIsRunning().equalsIgnoreCase("false"))
                            {
                                if(x.getDestn().equalsIgnoreCase("SIT"))
                                {
                                    --noSit;
                                }
                                else
                                {
                                    --noSBRoad;
                                }
                                buses.set(i, x);
                                statusDetails.setText("SB Road to SIT: "+noSit+"\nSIT to SB Road: "+
                                        noSBRoad);
                                if (mToast!=null)
                                    mToast.cancel();
                                mToast= Toast.makeText(MapsActivity.this,
                                        markers.get(i).getTitle()+" completed journey",
                                        Toast.LENGTH_SHORT);
                                mToast.show();
                                markers.get(i).remove();
                            }
                        }
                        // Some other field of bus got updated
                        else
                        {
                            buses.set(i, x);
                            options.position(new LatLng(x.getLattitude(), x.getLongitude()))
                                    .title(x.getBusNo())
                                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                    .snippet("To "+x.getDestn());
                            markers.get(i).remove();
                            markers.set(i, mMap.addMarker(options));
                        }
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLng(new LatLng(x.getLattitude(), x.getLongitude())));
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                Bus b= dataSnapshot.getValue(Bus.class);
                for(int i = 0; i < buses.size(); i++)
                {
                    Bus x= buses.get(i);
                    if(x.getBusNo().equals(b.getBusNo()))
                    {
                        markers.get(i).remove();
                        buses.remove(x);
                    }
                    if(x.getDestn().equalsIgnoreCase("SIT"))
                    {
                        --noSit;
                    }
                    else
                    {
                        --noSBRoad;
                    }
                    statusDetails.setText("SB Road to SIT: "+noSit+"\nSIT to SB Road: "+
                            noSBRoad);
                }
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mRef.addChildEventListener(childEventListener);
    }

    @Override
    public void onInfoWindowClick(Marker marker)
    {
        Intent i= new Intent(MapsActivity.this, SelectedBus.class);
        String bno= marker.getTitle();
        Log.d(TAG, "Marker id= "+bno);
        i.putExtra("bus_no", bno);
        i.putExtra("mapType", mapType);
        startActivity(i);
    }

    private void handleThemes()
    {
        floatingActionButton = findViewById(R.id.mainBtn);
        mapActionButton = findViewById(R.id.MapType);
        ttActionButton = findViewById(R.id.Timetable);
        logoutActionButton = findViewById(R.id.LogoutBtn);
        ttActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, TimeTableActivity.class);
                startActivity(intent);
            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(count==0)
                {
                    mapActionButton.setVisibility(View.VISIBLE);
                    ttActionButton.setVisibility(View.VISIBLE);
                    logoutActionButton.setVisibility(View.VISIBLE);
                    count=1;
                }
                else
                {
                    mapActionButton.setVisibility(View.INVISIBLE);
                    ttActionButton.setVisibility(View.INVISIBLE);
                    logoutActionButton.setVisibility(View.INVISIBLE);
                    count=0;
                }
            }
        });
        logoutActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i= new Intent(MapsActivity.this, LoginActivity.class);
                i.putExtra("sign out", 1);
                startActivity(i);
                finish();
            }
        });
        mapActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder=new AlertDialog.Builder(MapsActivity.this);
                RelativeLayout parent= findViewById(R.id.parent);
                final View mView=getLayoutInflater().inflate(R.layout.map_choose, parent,false);
                mBuilder.setView(mView);
                final AlertDialog dialog=mBuilder.create();
                dialog.show();
                ImageButton defaultBtn = (ImageButton)mView.findViewById(R.id.default_view);
                defaultBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        mapType=0;
                        dialog.dismiss();
                    }
                });
                ImageButton terrainBtn = (ImageButton)mView.findViewById(R.id.terrain);
                terrainBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        mapType=1;
                        dialog.dismiss();
                    }
                });
                ImageButton hybridBtn = (ImageButton)mView.findViewById(R.id.hybrid);
                hybridBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        mapType=2;
                        dialog.dismiss();
                    }
                });
            }
        });
    }

}
