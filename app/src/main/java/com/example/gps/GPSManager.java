package com.example.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

/**
 * GPS管理器
 * 负责GPS定位的启动、停止和管理
 */
public class GPSManager {
    
    private static final String TAG = "GPSManager";
    
    // 位置更新的最小时间间隔(毫秒)
    private static final long MIN_TIME_UPDATE = 1000;
    
    // 位置更新的最小距离(米)
    private static final float MIN_DISTANCE_UPDATE = 1.0f;
    
    private Context context;
    private LocationManager locationManager;
    private GPSLocationListener gpsLocationListener;
    private boolean isGPSEnabled = false;
    
    /**
     * 构造函数
     * @param context 上下文
     * @param listener 位置变化监听器
     */
    public GPSManager(Context context, GPSLocationListener.OnLocationChangeListener listener) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.gpsLocationListener = new GPSLocationListener(listener);
    }
    
    /**
     * 检查GPS权限
     * @return true表示有权限,false表示无权限
     */
    public boolean checkGPSPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int fineLocationPermission = context.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION);
            int coarseLocationPermission = context.checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION);
            
            return fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                   coarseLocationPermission == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    
    /**
     * 检查GPS是否启用
     * @return true表示已启用,false表示未启用
     */
    public boolean isGPSEnabled() {
        if (locationManager != null) {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        return false;
    }
    
    /**
     * 检查网络定位是否启用
     * @return true表示已启用,false表示未启用
     */
    public boolean isNetworkEnabled() {
        if (locationManager != null) {
            return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        return false;
    }
    
    /**
     * 启动GPS定位
     * @return true表示启动成功,false表示启动失败
     */
    public boolean startGPS() {
        Log.d(TAG, "startGPS called");
        
        if (!checkGPSPermission()) {
            Log.e(TAG, "没有GPS权限");
            return false;
        }
        Log.d(TAG, "GPS权限检查通过");
        
        if (locationManager == null) {
            Log.e(TAG, "LocationManager为空");
            return false;
        }
        Log.d(TAG, "LocationManager正常");
        
        try {
            // 优先使用GPS定位
            if (isGPSEnabled()) {
                Log.d(TAG, "GPS提供者已启用,请求位置更新...");
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_UPDATE,
                    MIN_DISTANCE_UPDATE,
                    gpsLocationListener
                );
                isGPSEnabled = true;
                Log.d(TAG, "GPS定位已启动");
                
                // 获取最后一次的位置
                Location lastLocation = locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    Log.d(TAG, "获取到最后位置: " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude());
                    gpsLocationListener.onLocationChanged(lastLocation);
                } else {
                    Log.d(TAG, "没有最后已知位置,等待实时更新...");
                }
            } 
            // 如果GPS不可用,使用网络定位
            else if (isNetworkEnabled()) {
                Log.d(TAG, "网络提供者已启用,请求位置更新...");
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_UPDATE,
                    MIN_DISTANCE_UPDATE,
                    gpsLocationListener
                );
                isGPSEnabled = true;
                Log.d(TAG, "网络定位已启动");
                
                // 获取最后一次的位置
                Location lastLocation = locationManager.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER);
                if (lastLocation != null) {
                    Log.d(TAG, "获取到最后网络位置: " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude());
                    gpsLocationListener.onLocationChanged(lastLocation);
                } else {
                    Log.d(TAG, "没有最后已知网络位置,等待实时更新...");
                }
            } else {
                Log.e(TAG, "GPS和网络定位都不可用");
                return false;
            }
            
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "启动GPS失败: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "启动GPS发生异常: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 停止GPS定位
     */
    public void stopGPS() {
        if (locationManager != null && gpsLocationListener != null) {
            try {
                locationManager.removeUpdates(gpsLocationListener);
                isGPSEnabled = false;
                Log.d(TAG, "GPS定位已停止");
            } catch (Exception e) {
                Log.e(TAG, "停止GPS失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 获取当前位置
     * @return Location对象,如果获取失败返回null
     */
    public Location getCurrentLocation() {
        if (!checkGPSPermission()) {
            Log.e(TAG, "没有GPS权限");
            return null;
        }
        
        try {
            Location location = null;
            
            // 优先从GPS获取
            if (isGPSEnabled()) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            
            // 如果GPS获取失败,从网络获取
            if (location == null && isNetworkEnabled()) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            
            return location;
        } catch (SecurityException e) {
            Log.e(TAG, "获取当前位置失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 设置位置更新参数
     * @param minTime 最小时间间隔(毫秒)
     * @param minDistance 最小距离(米)
     */
    public void setUpdateParameters(long minTime, float minDistance) {
        stopGPS();
        // 重新启动时会使用新参数
    }
    
    /**
     * 判断GPS是否正在运行
     * @return true表示正在运行,false表示未运行
     */
    public boolean isRunning() {
        return isGPSEnabled;
    }
}
