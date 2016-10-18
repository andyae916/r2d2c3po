package com.vokevr.simpleviewer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jackalopelite.JackalopeUtil;
import com.jackalopelite.JackalopeView;


public class SelectionActivity extends AppCompatActivity {

    public final String TAG = this.getClass().getSimpleName();


//    http://link.theplatform.com/s/xc6n8B/bBMX0WalwWxm?switch=hls&assetTypes=medium_video_ak&mbr=true&metafile=false

    public static final String URL_VIDEO_2D = "https://os-vh.akamaihd.net/i/AETN-Lifetime_VMS/BRAND_LFT_DMOM_182226_TVE_000_5994_90_20160928_00_,4,18,13,10,7,2,1,00.mp4.csmil/master.m3u8?__b__=400&hdnea=st=1476800529~exp=1476811359~acl=/i/AETN-Lifetime_VMS/BRAND_LFT_DMOM_182226_TVE_000_5994_90_20160928_00_*~hmac=f3d6b3781602f8eaf62344291b697b65bb983c1c7bb552dffe23f7732b865357&set-cc-attribute=CC";
//    public static final String URL_VIDEO_2D = "http://hsvr.akamaized.net/2016KabaddiWorldCup/KORvsARG/mobile/pod1/mobile_master.m3u8";
    public static final String URL_VIDEO_TD = "https://os-vh.akamaihd.net/i/AETN-Lifetime_VMS/BRAND_LFT_DMOM_182226_TVE_000_5994_90_20160928_00_,4,18,13,10,7,2,1,00.mp4.csmil/master.m3u8?__b__=400&hdnea=st=1476800529~exp=1476811359~acl=/i/AETN-Lifetime_VMS/BRAND_LFT_DMOM_182226_TVE_000_5994_90_20160928_00_*~hmac=f3d6b3781602f8eaf62344291b697b65bb983c1c7bb552dffe23f7732b865357&set-cc-attribute=CC";
    //public static final String URL_VIDEO_TD = "http://hsvr.akamaized.net/2016KabaddiWorldCup/KORvsARG/gearvr/pod1/gearvr_master.m3u8";

    public static final String URL_VIDEO_LOW = "https://os-vh.akamaihd.net/i/AETN-Lifetime_VMS/BRAND_LFT_DMOM_182226_TVE_000_5994_90_20160928_00_,4,18,13,10,7,2,1,00.mp4.csmil/master.m3u8?__b__=400&hdnea=st=1476800529~exp=1476811359~acl=/i/AETN-Lifetime_VMS/BRAND_LFT_DMOM_182226_TVE_000_5994_90_20160928_00_*~hmac=f3d6b3781602f8eaf62344291b697b65bb983c1c7bb552dffe23f7732b865357&set-cc-attribute=CC";
    //public static final String URL_VIDEO_LOW = "http://hsvr.akamaized.net/2016KabaddiWorldCup/KORvsARG/mobile/pod1/lo_mobile_master.m3u8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        Log.d(TAG, "onCreate: ");
        
        TextView tv2d2d = (TextView) findViewById(R.id.tv_2d2d);
        TextView tvcbtd = (TextView) findViewById(R.id.tv_cbtd);

        if (tv2d2d != null) {
            tv2d2d.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playVideo(URL_VIDEO_2D, JackalopeView.VideoType.Video2D, false);
                }
            });
        }
        if (tvcbtd != null) {
            tvcbtd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playVideo(URL_VIDEO_TD, JackalopeView.VideoType.VideoTB, true);
                }
            });
        }

//        Toast.makeText(this, "isHardwareSupported: " + isHardwareSupported2(SelectionActivity.this), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "isAccelerometerSupported: " + isAccelerometerSupported(SelectionActivity.this) +
                        "isGyroscopeSupported: " + isGyroscopeSupported(SelectionActivity.this) +
                        "isES2Supported: " + isES2Supported(SelectionActivity.this), Toast.LENGTH_SHORT).show();
    }

    private void playVideo(String url, JackalopeView.VideoType mode, boolean isVrMode) {
        Log.d(TAG, "playVideo: ");
        Intent videoIntent = new Intent(SelectionActivity.this, VideoPlayerActivity.class);
        videoIntent.putExtra(VideoPlayerActivity.KEY_URL, url);
        videoIntent.putExtra(VideoPlayerActivity.KEY_TYPE, mode.ordinal());
        videoIntent.putExtra(VideoPlayerActivity.KEY_MODE, isVrMode);
        startActivity(videoIntent);
    }

    public static boolean isHardwareSupported2(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ActivityManager activityManager = (ActivityManager)context.getSystemService("activity");
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 131072;
        return packageManager.hasSystemFeature("android.hardware.sensor.accelerometer") && packageManager.hasSystemFeature("android.hardware.sensor.gyroscope") && supportsEs2;
    }

    public static boolean isAccelerometerSupported(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ActivityManager activityManager = (ActivityManager)context.getSystemService("activity");
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 131072;
        return packageManager.hasSystemFeature("android.hardware.sensor.accelerometer") ;
    }

    public static boolean isGyroscopeSupported(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ActivityManager activityManager = (ActivityManager)context.getSystemService("activity");
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 131072;
        return packageManager.hasSystemFeature("android.hardware.sensor.gyroscope");
    }

    public static boolean isES2Supported(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ActivityManager activityManager = (ActivityManager)context.getSystemService("activity");
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 131072;
        return supportsEs2;
    }


}
