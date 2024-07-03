package com.example.dev.activity;

import android.Manifest;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.example.dev.BuildConfig;
import com.example.dev.R;
import com.example.dev.utils.CommonUtils;
import com.example.dev.utils.OkHttpUtils;
import com.example.dev.utils.PreferenceUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author
 */
public class MainActivity extends BaseActivity {
    //控件
    private LinearLayout ll_host;
    private TextView tv_host;
    private EditText et_host, et_txt;
    private Button btn_host, btn_txt, btn_clear;
    private ImageView iv_down;
    //请求参数
    private final String URL = "https://tts.mzzsfy.eu.org/api/";
    private String host, txt;
    boolean download;
    //媒体播放
    private MediaPlayer mediaPlayer;
    //存储权限
    private String[] mPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
        initMediaPlayer();
    }

    private void initView() {
        //域名
        ll_host = findViewById(R.id.ll_host);
        tv_host = findViewById(R.id.tv_host);
        et_host = findViewById(R.id.et_host);
        btn_host = findViewById(R.id.btn_host);
        //待转tts文本
        et_txt = findViewById(R.id.et_txt);
        btn_txt = findViewById(R.id.btn_txt);
        btn_clear = findViewById(R.id.btn_clear);
        iv_down = findViewById(R.id.iv_down);
        //Debug模式显示配置
        ll_host.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        //是否下载mp3
        download = PreferenceUtil.getBoolean("DownLoad", false);
        iv_down.setImageResource(download ? R.mipmap.iv_switch_open : R.mipmap.iv_switch_close);
    }

    private void initListener() {
        //域名
        btn_host.setOnClickListener(v -> {
            handleUrl();
        });
        //文本
        btn_txt.setOnClickListener(v -> {
            handleTxt();
            createTTs();
        });
        //清空文本
        btn_clear.setOnClickListener(v -> {
            et_txt.setText("");
        });

        //是否下载
        iv_down.setOnClickListener(v -> {
            setDownLoadStatus();
            if (download && !PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //申请权限
                PermissionUtils.permissionGroup(PermissionConstants.STORAGE)
                        .callback(new PermissionUtils.FullCallback() {
                            @Override
                            public void onGranted(List<String> granted) {
                                LogUtils.e(granted);
                            }

                            @Override
                            public void onDenied(List<String> deniedForever, List<String> denied) {
                                if (null != deniedForever && deniedForever.size() > 0) {
                                    //去系统设置页面
                                    ToastUtils.showLong(getStringById(R.string.enable_file_storage_permission));
                                    gotoSetting();
                                } else {
                                    ToastUtils.make().setDurationIsLong(true).show(R.string.save_audio_requires_permission);
                                }
                                //重置开关状态
                                setDownLoadStatus();
                            }
                        })
                        .request();
            }
        });
    }

    // 初始化 MediaPlayer
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());

        // 设置准备完成监听器
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 准备完成后开始播放音频
                mediaPlayer.start();
            }
        });

        // 设置错误监听器
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                LogUtils.e("MediaPlayer error: " + what + ", " + extra);
                return false;
            }
        });
    }

    //保存下载开关状态
    private void setDownLoadStatus() {
        download = !download;
        iv_down.setImageResource(download ? R.mipmap.iv_switch_open : R.mipmap.iv_switch_close);
        PreferenceUtil.commitBoolean("DownLoad", download);
    }

    //处理输入的服务器地址
    private void handleUrl() {
        host = et_host.getText().toString().trim();
        if (CommonUtils.isEmptyOrNull(host)) {
            host = URL;
        }
        if (!host.endsWith("/")) {
            host = host + "/";
        }
        et_host.setText(host);
        tv_host.setText(getStringById(R.string.current_server_address) + host);
    }

    //处理输入的文本
    private void handleTxt() {
        txt = et_txt.getText().toString().trim();
        if (CommonUtils.isEmptyOrNull(txt)) {
            txt = getStringById(R.string.nice_to_meet_you);
        }
        et_txt.setText(txt);
    }

    //生成tts
    private void createTTs() {
        // 构建完整的请求URL
        if (CommonUtils.isEmptyOrNull(host)) {
            host = URL;
        }
        String url = host + "tts?text=" + txt + "&download=" + download;

        // 发送 GET 请求
        OkHttpUtils.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e("createTTs: " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
//                    try {
                    File tempFile;
                    if (download) {
                        File downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                        //字符串的前 5 个字符
                        String name = txt.length() > 6 ? txt.substring(0, 5) : txt;
                        tempFile = new File(downloadDir, "tts_" + System.currentTimeMillis() + "_" + name + ".mp3");
                    } else {
                        tempFile = File.createTempFile("temp", ".mp3", getCacheDir());
                    }
                    FileOutputStream outputStream = new FileOutputStream(tempFile);
                    InputStream inputStream = response.body().byteStream();
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();
                    // 确保 MediaPlayer 处于正确的状态
                    if (mediaPlayer != null) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(tempFile.getPath());
                        mediaPlayer.prepareAsync();
                    } else {
                        // 如果MediaPlayer对象为空，重新初始化MediaPlayer
                        initMediaPlayer();
                        mediaPlayer.setDataSource(tempFile.getPath());
                        mediaPlayer.prepareAsync();
                    }
                    if (download) {
                        // 显示文件路径
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), getStringById(R.string.file_path) + tempFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        });
                        // 通知系统媒体库更新
                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{tempFile.getPath()}, null, null);
                    }
//                    } catch (IOException e) {
//                        LogUtils.e("setDataSource() failed: " + e.getMessage());
//                    }
                } else {
                    LogUtils.e("onResponse: Unexpected response code " + response.code());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        download = PermissionUtils.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        iv_down.setImageResource(download ? R.mipmap.iv_switch_open : R.mipmap.iv_switch_close);
        PreferenceUtil.commitBoolean("DownLoad", download);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放 MediaPlayer 资源
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}