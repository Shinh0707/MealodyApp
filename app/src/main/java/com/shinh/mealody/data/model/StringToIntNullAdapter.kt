package com.shinh.mealody.data.model

import android.os.Build
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import android.util.Log
import com.google.gson.JsonDeserializationContext
import java.lang.reflect.Type

/**
 * 空文字列をnullとして扱うカスタムデシリアライザー
 * 詳細ログ出力も実装
 */
class EmptyStringAsNullTypeAdapter<T> : JsonDeserializer<T> {
    companion object {
        private const val TAG = "EmptyStringAsNull"
    }

    override fun deserialize(
        jsonElement: JsonElement,
        type: Type,
        context: JsonDeserializationContext
    ): T? {
        if (jsonElement.isJsonPrimitive) {
            val jsonPrimitive = jsonElement.asJsonPrimitive
            if (jsonPrimitive.isString && jsonPrimitive.asString.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Log.w(TAG, "空文字列が検出されました。タイプ: ${type.typeName}")
                }
                return null
            }
        }
        return context.deserialize(jsonElement, type)
    }
}