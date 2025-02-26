package com.shinh.mealody.data.model

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.shinh.mealody.data.api.HotpepperClient
import com.shinh.mealody.data.database.converter.Converters
import com.shinh.mealody.data.location.LocationUtil

data class GourmetSearchResponse(
    @SerializedName("results")
    val results: GourmetSearchResults
)

data class GourmetSearchResults(
    @SerializedName("api_version")
    val apiVersion: String,
    @SerializedName("results_available")
    val resultsAvailable: Int,
    @SerializedName("results_returned")
    val resultsReturned: Int,
    @SerializedName("results_start")
    val resultsStart: Int,
    val shop: List<Shop>
){
    /**
     * 2つのGourmetSearchResultsを結合する
     *
     * @param other 結合する別のGourmetSearchResults
     * @return 結合されたGourmetSearchResults
     */
    fun merge(other: GourmetSearchResults): GourmetSearchResults {
        // ショップリストをマージ
        val mergedShops = mergeShopLists(this.shop, other.shop)

        // 新しいGourmetSearchResultsを作成
        return GourmetSearchResults(
            apiVersion = this.apiVersion,
            resultsAvailable = this.resultsAvailable.coerceAtLeast(other.resultsAvailable),
            resultsReturned = mergedShops.size,
            resultsStart = this.resultsStart.coerceAtMost(other.resultsStart),
            shop = mergedShops
        )
    }
    /**
     * まだ取得できる結果があるかどうかを確認
     *
     * @return まだ取得できる結果がある場合はtrue
     */
    fun hasMoreResults(): Boolean {
        return this.resultsStart + this.resultsReturned <= this.resultsAvailable
    }
}

data class Shop(
    // 基本情報（lite対応フィールド）
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val genre: Genre,
    val catch: String,
    val urls: Urls,
    val photo: Photo,

    // 追加情報
    @SerializedName("logo_image")
    val logoImage: String? = null,
    @SerializedName("name_kana")
    val nameKana: String? = null,
    @SerializedName("station_name")
    val stationName: String? = null,
    @SerializedName("ktai_coupon")
    val ktaiCoupon: Int? = null,

    // エリア情報
    @SerializedName("large_service_area")
    val largeServiceArea: ServiceAreaResult? = null,
    @SerializedName("service_area")
    val serviceArea: ServiceAreaResult? = null,
    @SerializedName("large_area")
    val largeArea: ServiceAreaResult? = null,
    @SerializedName("middle_area")
    val middleArea: ServiceAreaResult? = null,
    @SerializedName("small_area")
    val smallArea: ServiceAreaResult? = null,

    // 店舗詳細情報
    @SerializedName("sub_genre")
    val subGenre: Genre? = null,
    val budget: Budget? = null,
    @SerializedName("budget_memo")
    val budgetMemo: String? = null,
    val access: String? = null,
    @SerializedName("mobile_access")
    val mobileAccess: String? = null,
    val capacity: Int? = null,
    @SerializedName("party_capacity")
    @JsonAdapter(EmptyStringAsNullTypeAdapter::class)
    val partyCapacity: Int? = null,

    // 営業情報
    val open: String? = null,
    val close: String? = null,

    // 設備・サービス
    val wifi: String? = null,
    val wedding: String? = null,
    val course: String? = null,
    @SerializedName("free_drink")
    val freeDrink: String? = null,
    @SerializedName("free_food")
    val freeFood: String? = null,
    @SerializedName("private_room")
    val privateRoom: String? = null,
    val horigotatsu: String? = null,
    val tatami: String? = null,
    val card: String? = null,
    @SerializedName("non_smoking")
    val nonSmoking: String? = null,
    val charter: String? = null,
    val ktai: String? = null,
    val parking: String? = null,
    @SerializedName("barrier_free")
    val barrierFree: String? = null,
    val sommelier: String? = null,
    @SerializedName("night_view")
    val nightView: String? = null,
    @SerializedName("open_air")
    val openAir: String? = null,
    val show: String? = null,
    val equipment: String? = null,
    val karaoke: String? = null,
    val band: String? = null,
    val tv: String? = null,
    val english: String? = null,
    val pet: String? = null,
    val child: String? = null,
    val lunch: String? = null,
    val midnight: String? = null,

    // その他情報
    @SerializedName("shop_detail_memo")
    val shopDetailMemo: String? = null,
    @SerializedName("other_memo")
    val otherMemo: String? = null,

    // オプション情報（type指定による）
    val special: List<Special>? = null,
    @SerializedName("credit_card")
    val creditCard: List<CreditCard>? = null,

    // クーポン
    @SerializedName("coupon_urls")
    val couponUrls: CouponUrls? = null
){
    fun getArea(): SmallArea?{
        smallArea?.let{ smallArea ->
            middleArea?.let { middleArea ->
                largeArea?.let { largeArea ->
                    largeServiceArea?.let { largeServiceArea ->
                        return SmallArea(smallArea, middleArea, largeArea, largeServiceArea)
                    }
                }
            }
        }
        return null
    }

    suspend fun getArea(
        context: Context,
        hotpepperClient: HotpepperClient
    ): SmallArea?{
        smallArea?.let{ smallArea ->
            middleArea?.let { middleArea ->
                largeArea?.let { largeArea ->
                    largeServiceArea?.let { largeServiceArea ->
                        return SmallArea(smallArea, middleArea, largeArea, largeServiceArea)
                    }
                }
            }
        }
        val _address = LocationUtil.getAddress(context = context, LatLng(lat,lng))
        return _address?.let {
            hotpepperClient.findMatchingArea(
                _address
            )
        }
    }
}

/**
 * 2つのShopリストを結合し、同じIDのShopがあれば情報を補完する
 *
 * @param list1 1つ目のShopリスト
 * @param list2 2つ目のShopリスト
 * @return 結合されたShopリスト
 */
fun mergeShopLists(list1: List<Shop>, list2: List<Shop>): List<Shop> {
    val shopMap = mutableMapOf<String, Shop>()

    // 最初のリストをマップに追加
    list1.forEach { shop ->
        shopMap[shop.id] = shop
    }

    // 2つ目のリストで補完またはマージ
    list2.forEach { shop ->
        if (shopMap.containsKey(shop.id)) {
            // 既存のShopと情報をマージ
            val existingShop = shopMap[shop.id]!!
            shopMap[shop.id] = mergeShops(existingShop, shop)
        } else {
            // 新しいShopを追加
            shopMap[shop.id] = shop
        }
    }

    return shopMap.values.toList()
}

/**
 * 2つのShopオブジェクトをマージし、欠けている情報を補完する
 *
 * @param shop1 ベースとなるShop
 * @param shop2 補完に使用するShop
 * @return マージされたShop
 */
fun mergeShops(shop1: Shop, shop2: Shop): Shop {
    // nullでないプロパティを優先して使用
    return shop1.copy(
        // 基本情報（常に存在するはずなので上書きしない）
        // 追加情報（nullの場合は補完）
        logoImage = shop1.logoImage ?: shop2.logoImage,
        nameKana = shop1.nameKana ?: shop2.nameKana,
        stationName = shop1.stationName ?: shop2.stationName,
        ktaiCoupon = shop1.ktaiCoupon ?: shop2.ktaiCoupon,

        // エリア情報
        largeServiceArea = shop1.largeServiceArea ?: shop2.largeServiceArea,
        serviceArea = shop1.serviceArea ?: shop2.serviceArea,
        largeArea = shop1.largeArea ?: shop2.largeArea,
        middleArea = shop1.middleArea ?: shop2.middleArea,
        smallArea = shop1.smallArea ?: shop2.smallArea,

        // 店舗詳細情報
        subGenre = shop1.subGenre ?: shop2.subGenre,
        budget = shop1.budget ?: shop2.budget,
        budgetMemo = shop1.budgetMemo ?: shop2.budgetMemo,
        access = shop1.access ?: shop2.access,
        mobileAccess = shop1.mobileAccess ?: shop2.mobileAccess,
        capacity = shop1.capacity ?: shop2.capacity,
        partyCapacity = shop1.partyCapacity ?: shop2.partyCapacity,

        // 営業情報
        open = shop1.open ?: shop2.open,
        close = shop1.close ?: shop2.close,

        // 設備・サービス（全て網羅）
        wifi = shop1.wifi ?: shop2.wifi,
        wedding = shop1.wedding ?: shop2.wedding,
        course = shop1.course ?: shop2.course,
        freeDrink = shop1.freeDrink ?: shop2.freeDrink,
        freeFood = shop1.freeFood ?: shop2.freeFood,
        privateRoom = shop1.privateRoom ?: shop2.privateRoom,
        horigotatsu = shop1.horigotatsu ?: shop2.horigotatsu,
        tatami = shop1.tatami ?: shop2.tatami,
        card = shop1.card ?: shop2.card,
        nonSmoking = shop1.nonSmoking ?: shop2.nonSmoking,
        charter = shop1.charter ?: shop2.charter,
        ktai = shop1.ktai ?: shop2.ktai,
        parking = shop1.parking ?: shop2.parking,
        barrierFree = shop1.barrierFree ?: shop2.barrierFree,
        sommelier = shop1.sommelier ?: shop2.sommelier,
        nightView = shop1.nightView ?: shop2.nightView,
        openAir = shop1.openAir ?: shop2.openAir,
        show = shop1.show ?: shop2.show,
        equipment = shop1.equipment ?: shop2.equipment,
        karaoke = shop1.karaoke ?: shop2.karaoke,
        band = shop1.band ?: shop2.band,
        tv = shop1.tv ?: shop2.tv,
        english = shop1.english ?: shop2.english,
        pet = shop1.pet ?: shop2.pet,
        child = shop1.child ?: shop2.child,
        lunch = shop1.lunch ?: shop2.lunch,
        midnight = shop1.midnight ?: shop2.midnight,
        // その他情報
        shopDetailMemo = shop1.shopDetailMemo ?: shop2.shopDetailMemo,
        otherMemo = shop1.otherMemo ?: shop2.otherMemo,

        // オプション情報
        special = shop1.special ?: shop2.special,
        creditCard = shop1.creditCard ?: shop2.creditCard,

        // クーポン
        couponUrls = shop1.couponUrls ?: shop2.couponUrls
    )
}

data class Genre(
    val code: String,
    val catch: String,
    val name: String
)

data class Budget(
    val code: String,
    val name: String,
    val average: String
)

data class Urls(
    val pc: String
)

data class Photo(
    val pc: PhotoUrls,
    val mobile: PhotoUrls? = null
)

data class PhotoUrls(
    val l: String,
    val m: String,
    val s: String
)

data class Special(
    val code: String,
    val name: String,
    @SerializedName("special_category")
    val specialCategory: SpecialCategory,
    val title: String
)

data class SpecialCategory(
    val code: String,
    val name: String
)

data class CreditCard(
    val code: String,
    val name: String
)

data class CouponUrls(
    val pc: String? = null,
    val sp: String? = null
)

data class ServiceAreaResult(
    val code: String,
    val name: String
)

data class LargeAreaResponse(
    @SerializedName("results")
    val results: LargeAreaResults
)

data class LargeAreaResults(
    @SerializedName("large_area")
    val largeAreas: List<LargeAreaResult>
)

data class LargeAreaResult(
    val code: String,
    val name: String,
    @SerializedName("service_area")
    val serviceArea: ServiceAreaResult,
    @SerializedName("large_service_area")
    val largeServiceArea: ServiceAreaResult
)

data class MiddleAreaResponse(
    @SerializedName("results")
    val results: MiddleAreaResults
)

data class MiddleAreaResults(
    @SerializedName("middle_area")
    val middleAreas: List<MiddleAreaResult>
)

data class MiddleAreaResult(
    val code: String,
    val name: String,
    @SerializedName("large_area")
    val largeArea: ServiceAreaResult,
    @SerializedName("service_area")
    val serviceArea: ServiceAreaResult,
    @SerializedName("large_service_area")
    val largeServiceArea: ServiceAreaResult
)

data class SmallAreaResponse(
    @SerializedName("results")
    val results: SmallAreaResults
)

data class SmallAreaResults(
    @SerializedName("small_area")
    val smallAreas: List<SmallAreaResult>
)

data class SmallAreaResult(
    val code: String,
    val name: String,
    @SerializedName("middle_area")
    val middleArea: ServiceAreaResult,
    @SerializedName("large_area")
    val largeArea: ServiceAreaResult,
    @SerializedName("service_area")
    val serviceArea: ServiceAreaResult,
    @SerializedName("large_service_area")
    val largeServiceArea: ServiceAreaResult
)

interface Area{
    val code: String
    val name: String
    val parentArea: Area?

    fun getFullName(childName: String=""): String{
        if (childName.isEmpty()){
            return parentArea?.getFullName(name) ?: name
        }
        return parentArea?.getFullName("$name $childName") ?: ("$name $childName")
    }

    fun getAreaType(): AreaType?{
        return Area.getAreaType(code)
    }

    fun getParentAreas(childResult: MutableList<Area>): List<Area>{
        parentArea?.getParentAreas(childResult)
        childResult.add(this)
        return childResult.reversed()
    }

    companion object {
        fun getAreaType(code: String): AreaType?{
            if (code.startsWith("SS") || code.startsWith("SA")){
                return AreaType.Service
            }
            if (code.startsWith("X")){
                return AreaType.SMALL
            }
            if (code.startsWith("Y")){
                return AreaType.MIDDLE
            }
            if (code.startsWith("Z")){
                return AreaType.LARGE
            }
            return null
        }
    }
}

enum class AreaType{
    Service, LARGE, MIDDLE, SMALL
}

data class LargeArea(
    override val code: String,
    override val name: String,
    override val parentArea: ServiceArea
) : Area
{
    constructor(largeArea: LargeAreaResult):this(
        code = largeArea.code,
        name = largeArea.name,
        parentArea = ServiceArea(largeArea.largeServiceArea)
    )
    constructor(largeArea: ServiceAreaResult, serviceArea: ServiceAreaResult):this(
        code = largeArea.code,
        name = largeArea.name,
        parentArea = ServiceArea(serviceArea)
    )
}

data class MiddleArea(
    override val code: String,
    override val name: String,
    override val parentArea: LargeArea
) : Area
{
    constructor(middleArea: MiddleAreaResult):this(
        code = middleArea.code,
        name = middleArea.name,
        parentArea = LargeArea(middleArea.largeArea,middleArea.largeServiceArea)
    )
    constructor(middleArea: ServiceAreaResult ,largeArea: ServiceAreaResult, serviceArea: ServiceAreaResult):this(
        code = middleArea.code,
        name = middleArea.name,
        parentArea = LargeArea(largeArea, serviceArea)
    )
}

data class SmallArea(
    override val code: String,
    override val name: String,
    override val parentArea: MiddleArea
) : Area
{
    constructor(smallArea: SmallAreaResult):this(
        code = smallArea.code,
        name = smallArea.name,
        parentArea = MiddleArea(smallArea.middleArea, smallArea.largeArea, smallArea.largeServiceArea)
    )

    constructor(smallArea: ServiceAreaResult,middleArea: ServiceAreaResult ,largeArea: ServiceAreaResult, serviceArea: ServiceAreaResult):this(
        code = smallArea.code,
        name = smallArea.name,
        parentArea = MiddleArea(middleArea, largeArea, serviceArea)
    )
}

data class ServiceArea(
    override val code: String,
    override val name: String,
    override val parentArea: Area? = null
) : Area
{
    constructor(serviceArea: ServiceAreaResult):this(
        code = serviceArea.code,
        name = serviceArea.name
    )
    constructor(serviceArea: ServiceAreaResult, largeServiceArea: ServiceAreaResult):this(
        code = serviceArea.code,
        name = serviceArea.name,
        parentArea = ServiceArea(largeServiceArea)
    )
}