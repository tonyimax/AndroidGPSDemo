package com.example.gps;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.view.View;
import android.view.Window;

public class GPSActivity extends Activity {

    private static final String TAG = "GPSActivity";

    private TextView tvLatitude;
    private TextView tvLongitude;
    private TextView tvAltitude;
    private TextView tvAccuracy;
    private TextView tvSpeed;
    private TextView tvBearing;
    private TextView tvStatus;
    private Button btnStartGPS;
    private Button btnStopGPS;

    private GPSManager gpsManager;
    private GPSPermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViews();
        setupImmersiveMode();   // ⭐ 全屏 + 去底部黑条
        initGPSManager();

        permissionHelper = new GPSPermissionHelper(this);
        setupListeners();

        startGPSLocation();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        setContentView(R.layout.activity_gps);

        tvLatitude = findViewById(R.id.tv_latitude);
        tvLongitude = findViewById(R.id.tv_longitude);
        tvAltitude = findViewById(R.id.tv_altitude);
        tvAccuracy = findViewById(R.id.tv_accuracy);
        tvSpeed = findViewById(R.id.tv_speed);
        tvBearing = findViewById(R.id.tv_bearing);
        tvStatus = findViewById(R.id.tv_status);
        btnStartGPS = findViewById(R.id.btn_start_gps);
        btnStopGPS = findViewById(R.id.btn_stop_gps);
    }

    /**
     * ⭐ 彻底去掉底部黑条 + 全屏沉浸式
     */
    private void setupImmersiveMode() {
        Window window = getWindow();

        // 1. 让内容延伸到状态栏 + 导航栏（关键）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }

        // 2. 状态栏透明
        window.setStatusBarColor(Color.TRANSPARENT);

        // 3. 导航栏透明（解决底部黑色条）
        window.setNavigationBarColor(Color.TRANSPARENT);

        // 4. 白色背景需要深色导航栏图标
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int flags = window.getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR; // 深色图标
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }

    /**
     * 初始化GPS管理器
     */
    private void initGPSManager() {
        gpsManager = new GPSManager(this, new GPSLocationListener.OnLocationChangeListener() {
            @Override
            public void onLocationChanged(double latitude, double longitude, double altitude,
                                          float accuracy, float speed, float bearing) {
                updateLocationUI(latitude, longitude, altitude, accuracy, speed, bearing);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                updateStatus("GPS状态: " + provider + " - " + status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                updateStatus("GPS已启用");
                Toast.makeText(GPSActivity.this, "GPS已启用", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                updateStatus("GPS已禁用");
                Toast.makeText(GPSActivity.this, "GPS未开启", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 设置按钮监听器
     */
    private void setupListeners() {
        btnStartGPS.setOnClickListener(v -> startGPSLocation());
        btnStopGPS.setOnClickListener(v -> stopGPSLocation());
    }

    /**
     * 开始GPS定位
     */
    private void startGPSLocation() {
        if (!permissionHelper.checkLocationPermission()) {
            if (permissionHelper.shouldShowRequestPermissionRationale()) {
                showPermissionRationale();
            } else {
                permissionHelper.requestLocationPermission();
            }
            return;
        }

        if (!permissionHelper.isGPSEnabled()) {
            showGPSDisabledDialog();
            return;
        }

        if (gpsManager.startGPS()) {
            updateStatus("GPS定位已启动");
            btnStartGPS.setEnabled(false);
            btnStopGPS.setEnabled(true);
        } else {
            updateStatus("GPS启动失败");
        }
    }

    /**
     * 停止GPS定位
     */
    private void stopGPSLocation() {
        gpsManager.stopGPS();
        updateStatus("GPS已停止");
        btnStartGPS.setEnabled(true);
        btnStopGPS.setEnabled(false);
    }

    /**
     * 更新位置信息
     */
    private void updateLocationUI(double latitude, double longitude, double altitude,
                                  float accuracy, float speed, float bearing) {
        runOnUiThread(() -> {
            tvLatitude.setText(String.format("%.6f°", latitude));
            tvLongitude.setText(String.format("%.6f°", longitude));
            tvAltitude.setText(String.format("%.2f 米", altitude));
            tvAccuracy.setText(String.format("%.2f 米", accuracy));
            tvSpeed.setText(String.format("%.2f 米/秒", speed));
            tvBearing.setText(String.format("%.2f°", bearing));
        });
    }

    private void updateStatus(String status) {
        runOnUiThread(() -> tvStatus.setText("状态: " + status));
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("需要定位权限")
                .setMessage("请允许位置权限以启用GPS功能")
                .setPositiveButton("确定", (d, w) -> permissionHelper.requestLocationPermission())
                .setNegativeButton("取消", null)
                .show();
    }

    private void showGPSDisabledDialog() {
        new AlertDialog.Builder(this)
                .setTitle("GPS未启用")
                .setMessage("开启GPS以使用实时定位")
                .setPositiveButton("前往设置", (d, w) -> permissionHelper.openGPSSettings())
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (permissionHelper.handlePermissionResult(requestCode, permissions, grantResults)) {
            startGPSLocation();
        } else {
            Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        gpsManager.stopGPS();
        super.onDestroy();
    }
}
