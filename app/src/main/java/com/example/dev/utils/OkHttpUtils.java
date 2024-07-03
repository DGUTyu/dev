package com.example.dev.utils;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.dev.R;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 网络请求工具类
 */
public class OkHttpUtils {
    private static final OkHttpClient client = new OkHttpClient();
    //核心线程数为 3，最大线程数为 10，存活时间为 5 秒的线程池，并使用 LinkedBlockingQueue 作为任务的阻塞队列，队列的容量为 100。
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            3,
            10,
            5,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(100)
    );

    /**
     * 发送 GET 请求
     *
     * @param url      请求地址
     * @param callback 回调函数
     */
    public static void get(final String url, final Callback callback) {
        // 检查网络连接状态
        if (!checkNetwork()) {
            ToastUtils.showLong(R.string.network_not_available);
            return;
        }
        executor.execute(() -> {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).enqueue(callback);
        });
    }

    /**
     * 发送 POST 请求
     *
     * @param url      请求地址
     * @param params   请求参数
     * @param callback 回调函数
     */
    public static void post(final String url, final Map<String, String> params, final Callback callback) {
        // 检查网络连接状态
        if (!checkNetwork()) {
            ToastUtils.showLong(R.string.network_not_available);
            return;
        }
        executor.execute(() -> {
            FormBody.Builder builder = new FormBody.Builder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
            RequestBody requestBody = builder.build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(callback);
        });
    }

    /**
     * 关闭网络请求线程池
     */
    public static void shutdown() {
        executor.shutdown();
    }

    /**
     * 检查网络连接状态
     */
    public static boolean checkNetwork() {
        // 使用线程池执行网络检查操作
        Future<Boolean> future = executor.submit(() -> {
            // 在子线程中检查网络连接状态
            return NetworkUtils.isAvailable();
        });

        try {
            // 获取异步任务的结果，设置超时时间为 3 秒
            return future.get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // 发生异常时，默认返回网络不可用
            return false;
        }
    }
}