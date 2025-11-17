package com.example.gps;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * GPS位置监听器
 * 实时监听GPS位置变化
 */
public class GPSLocationListener implements LocationListener {
    
    private static final String TAG = "GPSLocationListener";
    private OnLocationChangeListener locationChangeListener;
    
    public interface OnLocationChangeListener {
        /**
         * 位置改变时回调
         * @param latitude 纬度
         * @param longitude 经度
         * @param altitude 海拔高度
         * @param accuracy 精度
         * @param speed 速度
         * @param bearing 方向
         */
        void onLocationChanged(double latitude, double longitude, double altitude, 
                             float accuracy, float speed, float bearing);
        
        /**
         * GPS状态改变时回调
         * @param provider 提供者
         * @param status 状态
         * @param extras 额外信息
         */
        void onStatusChanged(String provider, int status, Bundle extras);
        
        /**
         * GPS启用时回调
         */
        void onProviderEnabled(String provider);
        
        /**
         * GPS禁用时回调
         */
        void onProviderDisabled(String provider);
    }
    
    public GPSLocationListener(OnLocationChangeListener listener) {
        this.locationChangeListener = listener;
    }
    
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            double altitude = location.getAltitude();
            float accuracy = location.getAccuracy();
            float speed = location.getSpeed();
            float bearing = location.getBearing();
            
            Log.d(TAG, "位置更新 - 纬度: " + latitude + ", 经度: " + longitude + 
                  ", 精度: " + accuracy + "米, 速度: " + speed + "米/秒");
            
            if (locationChangeListener != null) {
                locationChangeListener.onLocationChanged(latitude, longitude, altitude, 
                                                        accuracy, speed, bearing);
            }
        }
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "GPS状态改变: " + provider + " 状态: " + status);
        if (locationChangeListener != null) {
            locationChangeListener.onStatusChanged(provider, status, extras);
        }
    }
    
    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "GPS已启用: " + provider);
        if (locationChangeListener != null) {
            locationChangeListener.onProviderEnabled(provider);
        }
    }
    
    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "GPS已禁用: " + provider);
        if (locationChangeListener != null) {
            locationChangeListener.onProviderDisabled(provider);
        }
    }
}
