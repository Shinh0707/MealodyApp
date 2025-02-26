package com.shinh.mealody.domain.model

import android.util.Log
import com.shinh.mealody.data.model.GourmetSearchResults

data class SearchQuery(
    // 必須パラメーター（いずれか1つは必要）
    val id: List<String>? = null,            // 最大20個
    val keyword: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,

    // エリア関連（任意）
    val largeServiceArea: String? = null,    // 大サービスエリアコード
    val serviceArea: List<String>? = null,   // 最大3個
    val largeArea: List<String>? = null,     // 最大3個
    val middleArea: List<String>? = null,    // 最大5個
    val smallArea: List<String>? = null,     // 最大5個

    // 店舗検索関連（任意）
    val name: String? = null,
    val nameKana: String? = null,
    val nameAny: String? = null,
    val tel: String? = null,
    val address: String? = null,

    // 特集関連（任意）
    val special: List<String>? = null,
    val specialOr: List<String>? = null,
    val specialCategory: List<String>? = null,
    val specialCategoryOr: List<String>? = null,

    // 位置検索オプション（任意）
    val range: Int? = null,                  // 1:300m, 2:500m, 3:1000m, 4:2000m, 5:3000m
    val datum: String? = null,               // world or tokyo

    // 店舗条件（任意）
    val genre: List<String>? = null,
    val budget: List<String>? = null,        // 最大2個
    val partyCapacity: Int? = null,

    // 設備・サービス（Boolean型で統一）
    val wifi: Boolean? = null,
    val wedding: Boolean? = null,
    val course: Boolean? = null,
    val freeDrink: Boolean? = null,
    val freeFood: Boolean? = null,
    val privateRoom: Boolean? = null,
    val horigotatsu: Boolean? = null,
    val tatami: Boolean? = null,
    val cocktail: Boolean? = null,
    val shochu: Boolean? = null,
    val sake: Boolean? = null,
    val wine: Boolean? = null,
    val card: Boolean? = null,
    val nonSmoking: Boolean? = null,
    val charter: Boolean? = null,
    val ktai: Boolean? = null,
    val parking: Boolean? = null,
    val barrierFree: Boolean? = null,
    val sommelier: Boolean? = null,
    val nightView: Boolean? = null,
    val openAir: Boolean? = null,
    val show: Boolean? = null,
    val equipment: Boolean? = null,
    val karaoke: Boolean? = null,
    val band: Boolean? = null,
    val tv: Boolean? = null,
    val lunch: Boolean? = null,
    val midnight: Boolean? = null,
    val midnightMeal: Boolean? = null,
    val english: Boolean? = null,
    val pet: Boolean? = null,
    val child: Boolean? = null,

    // クレジットカード（任意）
    val creditCard: List<String>? = null,

    // レスポンス制御（任意）
    val type: QueryType? = QueryType.DETAIL,                // lite, credit_card, special
    val order: Int? = null,                  // 1:店名かな順, 2:ジャンル順, 3:エリア順, 4:おすすめ順
    val start: Int? = null,                  // 開始位置（デフォルト1）
    val count: Int? = null                   // 取得件数（1-100、デフォルト10）
) {
    // バリデーション用コンパニオンオブジェクト
    companion object {
        const val MAX_ID_COUNT = 20
        const val MAX_SERVICE_AREA_COUNT = 3
        const val MAX_LARGE_AREA_COUNT = 3
        const val MAX_MIDDLE_AREA_COUNT = 5
        const val MAX_SMALL_AREA_COUNT = 5
        const val MAX_BUDGET_COUNT = 2

        const val MIN_COUNT = 1
        const val MAX_COUNT = 100
        const val DEFAULT_COUNT = 10
    }

    // 必須パラメーターのいずれかが設定されているかチェック
    fun isValid(): Boolean {
        return !id.isNullOrEmpty() ||
                !name.isNullOrEmpty() ||
                !nameKana.isNullOrEmpty() ||
                !nameAny.isNullOrEmpty() ||
                !tel.isNullOrEmpty() ||
                !address.isNullOrEmpty() ||
                !keyword.isNullOrEmpty() ||
                (lat != null && lng != null) ||
                !largeServiceArea.isNullOrEmpty() ||
                !serviceArea.isNullOrEmpty() ||
                !largeArea.isNullOrEmpty() ||
                !middleArea.isNullOrEmpty() ||
                !smallArea.isNullOrEmpty()
    }

    /**
     * 現在のレスポンスに基づいて次のページのクエリを生成する
     *
     * @param response 現在のGourmetSearchResults
     * @return 次のページのSearchQuery、もう結果がない場合はnull
     */
    fun nextPageQuery(response: GourmetSearchResults): SearchQuery? {
        // 次のページがない場合はnullを返す
        val totalResults = response.resultsAvailable
        val currentEnd = response.resultsStart + response.resultsReturned - 1
        Log.d("nextPageQuery", "totalResults: $totalResults, currentEnd: $currentEnd")
        if (currentEnd >= totalResults) {
            return null
        }

        // 次の開始位置を設定
        val nextStart = response.resultsStart + response.resultsReturned
        Log.d("nextPageQuery", "resultStart: ${response.resultsStart}, resultReturned: ${response.resultsReturned}")
        // 現在のクエリをコピーして、開始位置だけ更新
        return this.copy(start = nextStart)
    }
}

/**
 * クエリタイプを表すデータクラス
 * @param isLite trueの場合は軽量情報、falseの場合は詳細情報
 * @param hasCredit クレジットカード情報を含むかどうか
 * @param hasSpecial 特集情報を含むかどうか
 */
data class QueryType(
    val isLite: Boolean = false,
    val hasCredit: Boolean = false,
    val hasSpecial: Boolean = false
) {
    companion object {
        // よく使うインスタンスを定数として定義
        val DETAIL = QueryType(isLite = false)
        val LITE = QueryType(isLite = true)
        val CREDIT = QueryType(isLite = false, hasCredit = true)
        val SPECIAL = QueryType(isLite = false, hasSpecial = true)
        val LITE_CREDIT = QueryType(isLite = true, hasCredit = true)
        val LITE_SPECIAL = QueryType(isLite = true, hasSpecial = true)
        val CREDIT_SPECIAL = QueryType(isLite = false, hasCredit = true, hasSpecial = true)
        val LITE_ALL = QueryType(isLite = true, hasCredit = true, hasSpecial = true)
    }

    /**
     * APIリクエスト用のパラメータ文字列に変換
     * @return API用のパラメータ文字列、DEFAULTの場合はnull
     */
    fun toQuery(): String? {
        val parts = mutableListOf<String>()

        if (isLite) parts.add("lite")
        if (hasCredit) parts.add("credit_card")
        if (hasSpecial) parts.add("special")

        return if (parts.isEmpty()) null else parts.joinToString("+")
    }

    /**
     * 指定されたQueryTypeの内容が全て含まれているかチェック
     * @param other チェックするQueryType
     * @return 全ての内容が含まれている場合はtrue
     */
    fun contains(other: QueryType?): Boolean {
        if (other == null) return contains(DETAIL)

        // 情報量(lite/detail)のチェック
        if ((!other.isLite) && isLite) return false

        // 追加情報のチェック
        if (other.hasCredit && !hasCredit) return false
        if (other.hasSpecial && !hasSpecial) return false

        return true
    }

    /**
     * 指定されたQueryTypeと情報を合成
     * @param other 合成するQueryType
     * @return 合成されたQueryType
     */
    fun merge(other: QueryType?): QueryType {
        if (other == null) return merge(DETAIL)

        return QueryType(
            isLite = isLite && other.isLite,
            hasCredit = hasCredit || other.hasCredit,
            hasSpecial = hasSpecial || other.hasSpecial
        )
    }
}