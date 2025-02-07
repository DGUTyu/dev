package com.example.dev.utils

import android.content.Context
import android.widget.Toast

class KotlinUtils {
    /**
     * Companion Object：
     * companion object 是类内部的一个对象，它是类的一个单一实例，与类的实例无关。
     * 它通常用于在类内部创建静态成员。成员方法和属性可以直接通过类名调用，类似于 Java 中的静态方法和属性。
     */
    companion object {
        fun showToast(context: Context, message: String, long: Boolean) {
            val duration = if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            Toast.makeText(context, message, duration).show()
        }

        fun showToast(context: Context, message: String) {
            showToast(context, message, false)
        }
    }

}