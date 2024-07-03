package com.example.dev.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ToastUtils;

import com.example.dev.R;
import com.example.dev.utils.OkHttpUtils;

/**
 * @author
 */
public class BaseActivity extends AppCompatActivity {
    protected BaseActivity mBaseContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBaseContext = this;
    }

    //获取字符串
    public String getStringById(int stringId) {
        try {
            return this.getString(stringId);
        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
    }

    //去设置页面
    public void gotoSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + mBaseContext.getPackageName()));
        mBaseContext.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetworkd();
    }

    //检测网络状态
    public void checkNetworkd() {
        if (!OkHttpUtils.checkNetwork()) {
            ToastUtils.showLong(getStringById(R.string.network_not_available));
        }
    }
}
