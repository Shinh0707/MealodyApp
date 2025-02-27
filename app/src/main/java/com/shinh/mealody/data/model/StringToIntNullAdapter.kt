package com.shinh.mealody.data.model

import android.os.Build
import android.text.Html
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import android.util.Log
import com.google.gson.JsonDeserializationContext
import java.lang.reflect.Type

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
            if (type == String::class.java) {
                val stringValue = jsonElement.asString
                val decodedString = decodeNumericCharacterReferences(stringValue)
                @Suppress("UNCHECKED_CAST")
                return decodedString as T
            }
        }
        return context.deserialize(jsonElement, type)
    }
    private fun decodeNumericCharacterReferences(input: String): String {
        // 正規表現で数値文字参照を検出
        val regex = Regex("&#(\\d+);")
        // マッチした部分を変換
        return regex.replace(input) { matchResult ->
            val codePoint = matchResult.groupValues[1].toInt()
            // コードポイントを文字に変換
            codePoint.toChar().toString()
        }
    }
}