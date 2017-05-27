package login.tagbox.task.loginbox.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by shashank on 5/26/2017.
 */

public class LocationInfo extends Service implements LocationListener{

        private final Context mContext;
        boolean isGPSEnabled= false;
        boolean isNetworkEnabled = false;
        boolean canGetLocation = false;
        Location location;
        double latitude;
        double longitude;

        protected LocationManager locationManager;

        public LocationInfo(Context context){
            this.mContext = context;
            getLocation();
        }

        public Location getLocation(){
            try{
                locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if(!isGPSEnabled&&!isNetworkEnabled){

                }
                else {

                    this.canGetLocation = true;
                    if(isNetworkEnabled){

                        if(locationManager!= null){
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if(location!=null){
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();

                            }
                        }
                    }
                    if(isGPSEnabled){
                        if(location==null){
                            if(locationManager!=null){
                                location= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            }
                            if(location!=null){
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }

                    }
                }
            }
            catch (SecurityException se){
                se.printStackTrace();
            }
            catch (Exception e){
                e.printStackTrace();
            }

            return location;
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

}
