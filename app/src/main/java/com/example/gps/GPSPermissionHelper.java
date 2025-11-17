package com.example.gps;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

/**
 * GPS权限管理工具类
 * 处理权限申请和GPS设置
 */
public class GPSPermissionHelper {
    
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 1001;
    public static final int REQUEST_CODE_GPS_SETTINGS = 1002;
    
    private Activity activity;
    
    public GPSPermissionHelper(Activity activity) {
        this.activity = activity;
    }
    
    /**
     * 检查定位权限
     * @return true表示已授权,false表示未授权
     */
    public boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int fineLocationPermission = activity.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION);
            int coarseLocationPermission = activity.checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION);
            
            return fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                   coarseLocationPermission == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    
    /**
     * 请求定位权限
     */
    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            };
            
            activity.requestPermissions(
                permissions,
                REQUEST_CODE_LOCATION_PERMISSION
            );
        }
    }
    
    /**
     * 判断是否应该显示权限说明
     * @return true表示应该显示,false表示不应该显示
     */
    public boolean shouldShowRequestPermissionRationale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return activity.shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION) ||
                   activity.shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        return false;
    }
    
    /**
     * 检查GPS是否开启
     * @return true表示已开启,false表示未开启
     */
    public boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) activity
            .getSystemService(Activity.LOCATION_SERVICE);
        if (locationManager != null) {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        return false;
    }
    
    /**
     * 打开GPS设置页面
     */
    public void openGPSSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivityForResult(intent, REQUEST_CODE_GPS_SETTINGS);
    }
    
    /**
     * 处理权限请求结果
     * @param requestCode 请求码
     * @param permissions 权限数组
     * @param grantResults 授权结果数组
     * @return true表示权限已授权,false表示权限被拒绝
     */
    public boolean handlePermissionResult(int requestCode, String[] permissions, 
                                          int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0) {
                boolean allGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
                return allGranted;
            }
            return false;
        }
        return false;
    }
}
