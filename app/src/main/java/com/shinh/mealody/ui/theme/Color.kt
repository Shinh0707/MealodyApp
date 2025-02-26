package com.shinh.mealody.ui.theme

import androidx.compose.ui.graphics.Color

// Base colors
object MealodyColors {
    val HokkaidoBase = Color(0xFF006A6A)
    val TohokuBase = Color(0xFF8B4513)
    val KantoBase = Color(0xFF6750A4)
    val HokurikuBase = Color(0xFF00639C)
    val TokaiBase = Color(0xFF1B6C50)
    val KansaiBase = Color(0xFF984061)
    val ChugokuBase = Color(0xFF4A5BA9)
    val ShikokuBase = Color(0xFF7C5800)
    val KyushuBase = Color(0xFF5B4FA9)
}

// Derived color schemes for each region
data class RegionColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val surface: Color = Color.White,
    val surfaceVariant: Color
)

object RegionColors {
    private fun createScheme(baseColor: Color): RegionColorScheme {
        // MaterialデザインのカラーユーティリティやColor.copyを使用して
        // ベースカラーから各バリエーションを生成
        return RegionColorScheme(
            primary = baseColor,
            onPrimary = Color.White,
            primaryContainer = baseColor.copy(alpha = 0.12f),
            onPrimaryContainer = baseColor.copy(alpha = 0.87f),
            secondary = baseColor.copy(alpha = 0.7f),
            onSecondary = Color.White,
            secondaryContainer = baseColor.copy(alpha = 0.08f),
            onSecondaryContainer = baseColor.copy(alpha = 0.75f),
            surfaceVariant = baseColor.copy(alpha = 0.05f)
        )
    }

    val regionSchemes = mapOf(
        "SS40" to createScheme(MealodyColors.HokkaidoBase), // 北海道
        "SS70" to createScheme(MealodyColors.TohokuBase),   // 東北
        "SS10" to createScheme(MealodyColors.KantoBase),    // 関東
        "SS60" to createScheme(MealodyColors.HokurikuBase), // 北陸・甲信越
        "SS30" to createScheme(MealodyColors.TokaiBase),    // 東海
        "SS20" to createScheme(MealodyColors.KansaiBase),   // 関西
        "SS80" to createScheme(MealodyColors.ChugokuBase),  // 中国
        "SS90" to createScheme(MealodyColors.ShikokuBase),  // 四国
        "SS50" to createScheme(MealodyColors.KyushuBase)    // 九州・沖縄
    )

    fun getScheme(areaCode: String?): RegionColorScheme {
        if (areaCode == null) {
            return regionSchemes["SS10"]!! // デフォルトは関東
        }
        return regionSchemes[areaCode] ?: regionSchemes["SS10"]!! // デフォルトは関東
    }
}

// Color.kt
object GenreColors {
    // 和風・伝統系
    val Izakaya = Color(0xFFE94709)        // 暖かい朱色
    val Washoku = Color(0xFF2D4F3A)        // 深い抹茶色
    val Okonomiyaki = Color(0xFFB7542C)    // 焼き色の茶色

    // 洋風・国際系
    val DiningBar = Color(0xFF722F37)      // ワインレッド
    val Western = Color(0xFF1B365C)        // ネイビーブルー
    val ItalianFrench = Color(0xFF007844)  // イタリアングリーン
    val Chinese = Color(0xFFCC2C2C)        // チャイニーズレッド
    val Korean = Color(0xFF354B99)         // 韓国の伝統色に近い青
    val Asian = Color(0xFFB163A3)          // エキゾチックな紫
    val International = Color(0xFF5C7C9D)  // コスモポリタンなグレイブルー

    // 専門料理系
    val Creative = Color(0xFF6B4E71)       // 創造的な紫
    val Yakiniku = Color(0xFFA61C1C)       // 情熱的な赤
    val Ramen = Color(0xFFD4A017)          // ラーメンスープ色

    // エンターテイメント系
    val Karaoke = Color(0xFF9B4F96)        // パーティーパープル
    val Bar = Color(0xFF453F3C)            // シックな黒

    // カフェ・その他
    val Cafe = Color(0xFF795C34)           // コーヒーブラウン
    val Other = Color(0xFF616161)          // ニュートラルグレー
}

data class GenreColorScheme(
    val primary: Color,
    val container: Color,
    val shadow: Color
)

object GenreTheme {
    private fun createScheme(baseColor: Color): GenreColorScheme {
        return GenreColorScheme(
            primary = baseColor,
            container = baseColor.copy(alpha = 0.12f),
            shadow = baseColor.copy(alpha = 0.3f)
        )
    }

    private val genreSchemes = mapOf(
        "G001" to createScheme(GenreColors.Izakaya),        // 居酒屋
        "G002" to createScheme(GenreColors.DiningBar),      // ダイニングバー・バル
        "G003" to createScheme(GenreColors.Creative),       // 創作料理
        "G004" to createScheme(GenreColors.Washoku),        // 和食
        "G005" to createScheme(GenreColors.Western),        // 洋食
        "G006" to createScheme(GenreColors.ItalianFrench),  // イタリアン・フレンチ
        "G007" to createScheme(GenreColors.Chinese),        // 中華
        "G008" to createScheme(GenreColors.Yakiniku),      // 焼肉・ホルモン
        "G017" to createScheme(GenreColors.Korean),         // 韓国料理
        "G009" to createScheme(GenreColors.Asian),          // アジア・エスニック料理
        "G010" to createScheme(GenreColors.International),  // 各国料理
        "G011" to createScheme(GenreColors.Karaoke),       // カラオケ・パーティ
        "G012" to createScheme(GenreColors.Bar),           // バー・カクテル
        "G013" to createScheme(GenreColors.Ramen),         // ラーメン
        "G016" to createScheme(GenreColors.Okonomiyaki),   // お好み焼き・もんじゃ
        "G014" to createScheme(GenreColors.Cafe),          // カフェ・スイーツ
        "G015" to createScheme(GenreColors.Other)          // その他グルメ
    )

    private val genreNameSchemes = mapOf(
        "居酒屋" to createScheme(GenreColors.Izakaya),
        "ダイニングバー・バル" to createScheme(GenreColors.DiningBar),
        "創作料理" to createScheme(GenreColors.Creative),
        "和食" to createScheme(GenreColors.Washoku),
        "洋食" to createScheme(GenreColors.Western),
        "イタリアン・フレンチ" to createScheme(GenreColors.ItalianFrench),
        "中華" to createScheme(GenreColors.Chinese),
        "焼肉・ホルモン" to createScheme(GenreColors.Yakiniku),
        "韓国料理" to createScheme(GenreColors.Korean),
        "アジア・エスニック料理" to createScheme(GenreColors.Asian),
        "各国料理" to createScheme(GenreColors.International),
        "カラオケ・パーティ" to createScheme(GenreColors.Karaoke),
        "バー・カクテル" to createScheme(GenreColors.Bar),
        "ラーメン" to createScheme(GenreColors.Ramen),
        "お好み焼き・もんじゃ" to createScheme(GenreColors.Okonomiyaki),
        "カフェ・スイーツ" to createScheme(GenreColors.Cafe),
        "その他グルメ" to createScheme(GenreColors.Other)
    )

    fun getScheme(genreCode: String): GenreColorScheme {
        return genreSchemes[genreCode] ?: genreSchemes["G015"]!! // デフォルトはその他
    }

    fun getSchemeByName(genreName: String): GenreColorScheme {
        return genreNameSchemes[genreName] ?: genreNameSchemes["その他グルメ"]!! // デフォルトはその他
    }
}