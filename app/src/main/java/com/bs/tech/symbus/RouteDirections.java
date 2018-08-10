package com.bs.tech.symbus;

/**
 * Created by bhumika on 12/4/18.
 */

import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import java.util.ArrayList;
import java.util.List;

public class RouteDirections extends AsyncTask<Object, Void, Object>
{
    private String TAG="RouteDirections --->";
    private ArrayList<LatLng> pathArr= new ArrayList<LatLng>();
    double fromLat, fromLong, toLat, toLong;
    private SelectedBus activity;
    private boolean shouldPlot= false;

    RouteDirections(SelectedBus activity) { this.activity= activity; }

    @Override
    protected Object doInBackground(Object... objects)
    {
        fromLat= (double) objects[0];
        fromLong= (double) objects[1];
        toLat= (double) objects[2];
        toLong= (double) objects[3];
        pathArr= (ArrayList<LatLng>) objects[4];
        String str= (String) objects[5];
        if(str.equalsIgnoreCase("true"))
            shouldPlot= true;
        setRoute(fromLat, fromLong,
                toLat, toLong, pathArr);
        return pathArr;
    }

    @Override
    protected void onPostExecute(Object o)
    {
        super.onPostExecute(o);
        if(activity.polyline!=null)
            activity.polyline.remove();
        if(activity.usualRoute.size()>0 && activity.path.size()>0 && shouldPlot)
            activity.plotRoute();
    }

    private void setRoute(double fromLat, double fromLong,
                          double toLat, double toLong,
                          ArrayList<LatLng> pathArr)
    {
        Log.d(TAG, "In setRoute()");
        //remove old coords
        if (pathArr.size() > 0)
            pathArr.clear();
        //Execute Directions API request
        GeoApiContext context = new GeoApiContext
                .Builder()
                .apiKey("AIzaSyD7dHf4RKSAMqv0qL96tvENY5OX44qOmWg")
                .build();
        DirectionsApiRequest req = DirectionsApi
                .getDirections(context,
                        "" + fromLat + " " + fromLong,
                        "" + toLat + " " + toLong);
        try
        {
            DirectionsResult res = req.await();
            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0)
            {
                DirectionsRoute route = res.routes[0];
                if (route.legs != null)
                {
                    for (int i = 0; i < route.legs.length; i++)
                    {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null)
                        {
                            for (int j = 0; j < leg.steps.length; j++)
                            {
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length > 0)
                                {
                                    for (int k = 0; k < step.steps.length; k++)
                                    {
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null)
                                        {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1)
                                            {
                                                pathArr.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null)
                                    {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords)
                                        {
                                            pathArr.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG, ex.getLocalizedMessage());
        }

    }
}
