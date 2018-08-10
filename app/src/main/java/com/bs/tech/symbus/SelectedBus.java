package com.bs.tech.symbus;

/**
 * Created by bhumika on 28/3/18.
 */

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class SelectedBus extends AppCompatActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String bno = "";
    private Bus thisBus = null;
    private DatabaseReference mRef;
    private DatabaseReference mStopsRef;
    private DatabaseReference mRoutesRef;
    private LatLng destn = null, src = null;
    protected ArrayList<LatLng> path = new ArrayList<LatLng>();
    private String TAG = "Selected Bus ---> ";
    private ArrayList<Stop> stops = new ArrayList<Stop>();
    private TextView statusTv;
    protected Polyline polyline = null;
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    protected ArrayList<LatLng> usualRoute = new ArrayList<LatLng>();
    private int mapType;
    private TextView statusTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "In onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_bus);

        // get the intent & intent extras
        Intent i = getIntent();
        bno += i.getStringExtra("bus_no");
        mapType = i.getIntExtra("mapType", 0);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get the status textviews to update
        statusTv = findViewById(R.id.statusTv);
        statusTitle = findViewById(R.id.statusTitle);

        //get the buses from DB
        getBuses();
    }

    private void setUsualRoute() {
        Log.d(TAG, "In setUsualRoute()");
        String toWhere = "";

        // choose the route
        if (thisBus.getDestn().equalsIgnoreCase("SIT")) {
            toWhere += "toSIT";
        } else {
            toWhere += "toSBRoad";
        }
        Log.d(TAG, "Chosen route is: " + toWhere);

        // Get route end points from DB
        mRoutesRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://symbus-jwt.firebaseio.com/")
                .child("Routes").child(toWhere);
        mRoutesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Route mRoute = dataSnapshot.getValue(Route.class);
                Log.d(TAG, "Got the route: " + mRoute);
                // get lat-langs for usual route
                new RouteDirections(SelectedBus.this)
                        .execute(mRoute.getFromLat(), mRoute.getFromLong(),
                                mRoute.getToLat(), mRoute.getToLong(), usualRoute, "false");
                // get lat-langs to destination from current position
                new RouteDirections(SelectedBus.this)
                        .execute(src.latitude, src.longitude,
                                destn.latitude, destn.longitude, path, "true");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "In onMapReady()");
        mMap = googleMap;
        // configure map UI settings
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        // set map type
        switch (mapType)
        {
            case 0:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case 1:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case 2:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }
    }

    private void getBuses()
    {
        Log.d(TAG, "In getBuses()");
        mRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://symbus-jwt.firebaseio.com/")
                .child("Buses");
        ChildEventListener childEventListener= new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Log.d(TAG, "Bus added");
                Bus x = dataSnapshot.getValue(Bus.class);
                // if this is the selected bus
                if(bno.equalsIgnoreCase(x.getBusNo()))
                {
                    //set current bus
                    thisBus= x;
                    // display bus status
                    statusTitle.setText(""+thisBus.getBusNo()+"\nTo: "+thisBus.getDestn());
                    // position the camera
                    CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(x.getLattitude(), x.getLongitude()));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
                    mMap.moveCamera(center);
                    mMap.animateCamera(zoom);

                    // make marker bitmaps
                    int height = 80;
                    int width = 80;
                    // bus marker bitmap
                    BitmapDrawable bitmapdraw=(BitmapDrawable)getResources()
                            .getDrawable(R.drawable.icon_bus);
                    Bitmap b=bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                    //set & plot bus marker
                    src= new LatLng(thisBus.getLattitude(), thisBus.getLongitude());
                    markers.add(mMap.addMarker(new MarkerOptions()
                            .position(src)
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                            .title("" + thisBus.getBusNo())));
                    // destination marker bitmap
                    BitmapDrawable bitmapDraw1= (BitmapDrawable) getResources()
                            .getDrawable(R.drawable.icon_siu);
                    Bitmap b1=bitmapDraw1.getBitmap();
                    Bitmap destnMarker = Bitmap.createScaledBitmap(b1, width, height, false);
                    //set & plot destination marker
                    destn= new LatLng(thisBus.getDestnLat(), thisBus.getDestnLong());
                    markers.add(mMap.addMarker(new MarkerOptions()
                            .position(destn)
                            .icon(BitmapDescriptorFactory.fromBitmap(destnMarker))
                            .title(thisBus.getDestn())));
                    setUsualRoute();
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                Log.d(TAG, "Bus edited");
                Bus x= dataSnapshot.getValue(Bus.class);
                // make marker bitmap
                int height = 80;
                int width = 80;
                //if selected bus was edited
                if(thisBus.getBusNo().equals(x.getBusNo()))
                {
                    thisBus= x;
                    // if bus is still enroute
                    if(thisBus.getIsRunning().equalsIgnoreCase("true"))
                    {
                        statusTitle.setText(""+thisBus.getBusNo()+"\nTo: "+thisBus.getDestn());
                        // find markers to update
                        for(int i=0; i<markers.size(); ++i)
                        {
                            Marker m= markers.get(i);
                            //update bus marker
                            if(m.getTitle().equalsIgnoreCase(x.getBusNo()))
                            {
                                // bus marker bitmap
                                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources()
                                        .getDrawable(R.drawable.icon_bus);
                                Bitmap b=bitmapdraw.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                                // set & plot bus marker
                                src= new LatLng(thisBus.getLattitude(), thisBus.getLongitude());
                                markers.get(i).remove();
                                markers.set(i, mMap.addMarker(new MarkerOptions()
                                        .position(src)
                                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                                        .title("" + thisBus.getBusNo())));
                                // position camera
                                mMap.moveCamera(CameraUpdateFactory
                                        .newLatLng(new LatLng(x.getLattitude(), x.getLongitude())));
                            }
                            //update destination marker
                            if(m.getTitle().equalsIgnoreCase(x.getDestn()))
                            {
                                // destination marker bitmap
                                BitmapDrawable bitmapDraw1= (BitmapDrawable) getResources()
                                        .getDrawable(R.drawable.icon_siu);
                                Bitmap b1=bitmapDraw1.getBitmap();
                                Bitmap destnMarker = Bitmap.createScaledBitmap(b1, width, height, false);
                                // set & plot destination marker
                                destn= new LatLng(thisBus.getDestnLat(), thisBus.getDestnLong());
                                markers.get(i).remove();
                                markers.set(i, mMap.addMarker(new MarkerOptions()
                                        .position(destn)
                                        .icon(BitmapDescriptorFactory.fromBitmap(destnMarker))
                                        .title(thisBus.getDestn())));
                            }
                        }
                        setUsualRoute();
                    }
                    // if bus completed journey
                    else
                    {
                        finish();
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mRef.addChildEventListener(childEventListener);
    }

    private void getStops()
    {
        Log.d(TAG, "In getStops()");
        mStopsRef= FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://symbus-jwt.firebaseio.com/")
                .child("Stops");
        ChildEventListener mStopsListener= new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Log.d(TAG, "Stop added");
                Stop x= dataSnapshot.getValue(Stop.class);
                stops.add(x);
                LatLng cur= new LatLng(x.getLattitude(), x.getLongitude());
                // This bus-stop is not yet crossed
                if((thisBus.getNextStop()<=x.getStopNo() &&
                        thisBus.getDestn().equalsIgnoreCase("SIT")) ||
                        (thisBus.getNextStop()>=x.getStopNo() &&
                                thisBus.getDestn().equalsIgnoreCase("SB Road")))
                {
                    int height = 40;
                    int width = 40;
                    // bus-stop marker bitmap
                    BitmapDrawable bitmapdraw=(BitmapDrawable)getResources()
                            .getDrawable(R.drawable.icon_bus_stop);
                    Bitmap b=bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                    //plot bus-stop marker
                    markers.add(mMap.addMarker(new MarkerOptions()
                            .position(cur)
                            .title(x.getName())
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))));
                    Log.d(TAG, ""+x.getName());
                }
                // This was the last bus-stop crossed
                else if((x.getStopNo()==thisBus.getNextStop()+1 &&
                        thisBus.getDestn().equalsIgnoreCase("SB Road")) ||
                        (x.getStopNo()==thisBus.getNextStop()-1 &&
                                thisBus.getDestn().equalsIgnoreCase("SIT")))
                {
                    //remove the marker & update status
                    for(int j=0; j<markers.size(); ++j)
                    {
                        Marker m= markers.get(j);
                        if(m.getTitle().equalsIgnoreCase(x.getName()))
                            markers.get(j).remove();
                    }
                    statusTv.setText(("Crossed "+x.getName()).toUpperCase());
                }
                // This bus-stop was crossed much earlier
                else
                {
                    for(int j=0; j<markers.size(); ++j)
                    {
                        Marker m= markers.get(j);
                        if(m.getTitle().equalsIgnoreCase(x.getName()))
                            markers.get(j).remove();
                    }
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                Log.d(TAG, "Stop changed");
                Stop x= dataSnapshot.getValue(Stop.class);
                Log.d(TAG, x.getName());
                for(int i=0; i<stops.size(); ++i)
                {
                    Stop bs= stops.get(i);
                    Log.d(TAG, bs.getName());
                    if(bs.getName().equalsIgnoreCase(x.getName()))
                    {
                        stops.set(i, x);
                        LatLng cur= new LatLng(x.getLattitude(), x.getLongitude());
                        // This bus-stop is not yet crossed
                        if((thisBus.getNextStop()<=x.getStopNo() &&
                                thisBus.getDestn().equalsIgnoreCase("SIT")) ||
                                (thisBus.getNextStop()>=x.getStopNo() &&
                                        thisBus.getDestn().equalsIgnoreCase("SB Road")))
                        {
                            for(int j=0; j<markers.size(); ++j)
                            {
                                Marker m= markers.get(j);
                                if(m.getTitle().equalsIgnoreCase(x.getName()))
                                {
                                    int height = 40;
                                    int width = 40;
                                    BitmapDrawable bitmapdraw=(BitmapDrawable)getResources()
                                            .getDrawable(R.drawable.icon_bus_stop);
                                    Bitmap b=bitmapdraw.getBitmap();
                                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                                    markers.get(j).remove();
                                    markers.set(j, mMap.addMarker(new MarkerOptions()
                                           .position(cur)
                                           .title(x.getName())
                                           .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))));
                                    Log.d(TAG, ""+x.getName());

                                }
                            }
                        }
                        // This was the last bus-stop crossed
                        else if((x.getStopNo()==thisBus.getNextStop()+1 &&
                                thisBus.getDestn().equalsIgnoreCase("SB Road")) ||
                                (x.getStopNo()==thisBus.getNextStop()-1 &&
                                        thisBus.getDestn().equalsIgnoreCase("SIT")))
                        {
                            //remove marker & update status
                            for(int j=0; j<markers.size(); ++j)
                            {
                                Marker m= markers.get(j);
                                if(m.getTitle().equalsIgnoreCase(x.getName()))
                                    markers.get(j).remove();
                            }
                            statusTv.setText(("Crossed "+x.getName()).toUpperCase());
                        }
                        // This bus-stop was crossed much earlier
                        else
                        {
                            for(int j=0; j<markers.size(); ++j)
                            {
                                Marker m= markers.get(j);
                                if(m.getTitle().equalsIgnoreCase(x.getName()))
                                    markers.get(j).remove();
                            }
                        }
                    }
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "Stop removed");
                Stop x= dataSnapshot.getValue(Stop.class);
                for(int i=0; i<stops.size(); ++i)
                {
                    Stop bs= stops.get(i);
                    if(bs.getName().equalsIgnoreCase(x.getName()))
                    {
                        stops.remove(x);
                        for(int j=0; j<markers.size(); ++j)
                        {
                            Marker m = markers.get(j);
                            if (m.getTitle().equalsIgnoreCase(x.getName()))
                            {
                                markers.get(j).remove();
                                Log.d(TAG, "" + x.getName());

                            }
                        }
                    }
                }
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mStopsRef.addChildEventListener(mStopsListener);
    }

    public void plotRoute()
    {
        Log.d(TAG, "In plotRoute()");
        if(usualRoute.containsAll(path))
        {
            //only then plot stops
            Log.d(TAG, "Routes match, usual route is: "+usualRoute);
            statusTv.setText("BUS DEPARTED FROM SOURCE");
            getStops();
        }
        else
        {
            Log.d(TAG, "Routes do not match, usual route is: "+usualRoute);
            statusTv.setText("BUS TOOK A NEW ROUTE");
            // remove stale markers
            if(markers.size()>2)
            {
                for(int i= 2; i<markers.size(); i++)
                {
                    markers.get(i).remove();
                }
            }
        }
        //Draw the polyline
        PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
        polyline= mMap.addPolyline(opts);
    }
}
