package com.digital.app.bmicalculator.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.digital.app.bmicalculator.R;
import com.digital.app.bmicalculator.network.NearByGym;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class NearByPlaces extends Fragment {

    public static final String TAG = NearByPlaces.class.getSimpleName();
    private static final int LOC_REQ_CODE = 1;

    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    protected RecyclerView mRecyclerView;
    private FusedLocationProviderClient mClient;
    private double mLatitudeCurrent;
    private double mLongitudeCurrent;
    ProgressDialog pd;
    private String mCurrentLocation;
    private List<NearByGym> mNearByGym;

    public NearByPlaces() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        pd = new ProgressDialog(getContext());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_near_by_places, container, false);
        mClient = LocationServices.getFusedLocationProviderClient(getContext());
        mRecyclerView = view.findViewById(R.id.nearby_place_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        mNearByGym = new ArrayList<>();

        pd.setMessage("Loading");
        pd.show();



        getCurrentLocation();

        return view;
    }

    private void getCurrentLocation() {
        mClient = LocationServices.getFusedLocationProviderClient(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mLatitudeCurrent = location.getLatitude();
                mLongitudeCurrent = location.getLongitude();
                mCurrentLocation = mLatitudeCurrent + "," + mLongitudeCurrent;
                getCurrentPlaceItems();
            }
        });


    }

    private void getCurrentPlaceItems() {
        if (isLocationAccessPermitted()) {

                getCurrentPlaceData();

        } else {
            requestLocationAccessPermission();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentPlaceData() {
//        Collection<String> filterType = new ArrayList<>();
//        filterType.add(String.valueOf(Place.TYPE_GYM));
//        PlaceFilter placeFilter = new PlaceFilter(true, filterType);
//
//        Task<PlaceLikelihoodBufferResponse> placeResult =  mPlaceDetectionClient.
//                getCurrentPlace(null);
//        placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
//            @Override
//            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
//                Log.d(TAG, "current location places info");
//                List<Place> placesList = new ArrayList();
//                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
//                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
//                    placesList.add(placeLikelihood.getPlace().freeze());
//                }
//                likelyPlaces.release();
//                pd.hide();
//                NearByAdapter adapter = new NearByAdapter(placesList, getContext(), mLatitudeCurrent, mLongitudeCurrent);
//                mRecyclerView.setAdapter(adapter);
//            }
//        });

        String baseurl = "https://maps.googleapis.com/maps/api/place/search/json?key=AIzaSyAqnwqLxKUu51LSRZNhj9Rt2KyBW0kmeIY" +
                "&radius=10000&sensor=true&type=gym&rankBy=distance&location="
        +mCurrentLocation;

        RequestQueue queue = Volley.newRequestQueue(getContext());
        final StringRequest request = new StringRequest(Request.Method.GET, baseurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("NearBy", response);

                        try {
                            JSONObject gym = new JSONObject(response);
                            JSONArray results = gym.getJSONArray("results");
                            for (int i =0; i < results.length(); i++){
                                JSONObject jsonObject = results.getJSONObject(i);
                                String name= jsonObject.getString("name");
                                String address= jsonObject.getString("vicinity");
                                JSONObject geometry = jsonObject.getJSONObject("geometry");
                                JSONObject location = geometry.getJSONObject("location");
                                String latitude = location.getString("lat");
                                String longitude = location.getString("lng");

                                mNearByGym.add(new NearByGym(name,address, Double.parseDouble(latitude), Double.parseDouble(longitude)));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        NearByAdapter adapter = new NearByAdapter(mNearByGym, getContext(), mLatitudeCurrent, mLongitudeCurrent);
                        mRecyclerView.setAdapter(adapter);
                        pd.hide();


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("NearBy", error.getMessage());
                pd.hide();
            }
        });

        queue.add(request);
    }

    private boolean isLocationAccessPermitted() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOC_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                getCurrentPlaceData();
            }
        }
    }

    private void requestLocationAccessPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOC_REQ_CODE);
    }


    public static class NearByHolder extends RecyclerView.ViewHolder {

        TextView placeName;
        TextView placeDistance;
        TextView placeAddress;
        ImageView placeDirection;

        double lat,lng,tolat,tolng;

        public NearByHolder(@NonNull final View itemView) {
            super(itemView);

            placeName = itemView.findViewById(R.id.place_name);
            placeDistance = itemView.findViewById(R.id.place_distance);
            placeAddress = itemView.findViewById(R.id.place_address);
            placeDirection = itemView.findViewById(R.id.place_direction);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = "http://maps.google.com/maps?saddr=" + lat + ","
                            + lng + "&daddr=" + tolat + "," + tolng;
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse(uri));
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }

    public static class NearByAdapter extends RecyclerView.Adapter<NearByHolder> {

        private List<NearByGym> mPlacesList;
        private Context mContext;
        private double mLatitudeCurrent;
        private double mLongitudeCurrent;

        public NearByAdapter(List<NearByGym> placeList, Context context, double latitudeCurrent, double longitudeCurrent) {
            mPlacesList = placeList;
            mContext = context;
            mLatitudeCurrent = latitudeCurrent;
            mLongitudeCurrent = longitudeCurrent;

        }

        @NonNull
        @Override
        public NearByHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.place_list_item, parent, false);
            return new NearByHolder(itemView);
        }

        @Override
        public void onBindViewHolder(NearByHolder holder, int position) {
            final NearByGym nearByGym = mPlacesList.get(position);

            float distanceOfPlace = getDistanceOfPlace(nearByGym.getLat(),nearByGym.getLon());
            holder.placeName.setText(nearByGym.getName());
            holder.placeDistance.setText(formatFloatValue(distanceOfPlace) + " km");
            holder.placeAddress.setText(nearByGym.getAddress());

            holder.lat = mLatitudeCurrent;
            holder.lng = mLongitudeCurrent;
            holder.tolat = nearByGym.getLat();
            holder.tolng = nearByGym.getLon();

//            holder.placeDirection.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String uri = "http://maps.google.com/maps?saddr=" + mLatitudeCurrent + ","
//                            + mLongitudeCurrent + "&daddr=" + nearByGym.getLat() + "," + nearByGym.getLon();
//                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
//                            Uri.parse(uri));
//                    mContext.startActivity(intent);
//                }
//            });
        }

        private String formatFloatValue(float distanceOfPlace) {
            DecimalFormat format = new DecimalFormat("0.00");
            return format.format(distanceOfPlace);
        }


        private float getDistanceOfPlace(double lat, double lon) {

            Location placeLocation = new Location("place");
            placeLocation.setLatitude(lat);
            placeLocation.setLongitude(lon);

            Location currentLocation = new Location("current");
            currentLocation.setLatitude(mLatitudeCurrent);
            currentLocation.setLongitude(mLongitudeCurrent);

            float distance = currentLocation.distanceTo(placeLocation);

            return distance/1000;
        }

        @Override
        public int getItemCount() {
            return mPlacesList == null ? 0 : mPlacesList.size();
        }
    }

}
