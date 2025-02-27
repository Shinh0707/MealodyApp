package com.shinh.mealody.data.api

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.util.LruCache
import com.shinh.mealody.data.model.Area
import com.shinh.mealody.data.model.AreaType
import com.shinh.mealody.data.model.GourmetSearchResponse
import com.shinh.mealody.data.model.GourmetSearchResults
import com.shinh.mealody.data.model.LargeArea
import com.shinh.mealody.data.model.LargeAreaResponse
import com.shinh.mealody.data.model.MiddleArea
import com.shinh.mealody.data.model.MiddleAreaResponse
import com.shinh.mealody.data.model.ServiceArea
import com.shinh.mealody.data.model.Shop
import com.shinh.mealody.data.model.SmallArea
import com.shinh.mealody.data.model.SmallAreaResponse
import com.shinh.mealody.data.model.mergeShops
import com.shinh.mealody.domain.model.QueryType
import com.shinh.mealody.domain.model.SearchQuery
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HotpepperClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private interface HotpepperApi {
        @GET("gourmet/v1/")
        suspend fun searchShops(
            @Query("key") key: String,
            @Query("id") id: String? = null,
            @Query("name") name: String? = null,
            @Query("name_kana") nameKana: String? = null,
            @Query("name_any") nameAny: String? = null,
            @Query("tel") tel: String? = null,
            @Query("address") address: String? = null,

            // エリア関連
            @Query("large_service_area") largeServiceArea: String? = null,
            @Query("service_area") serviceArea: String? = null,
            @Query("large_area") largeArea: String? = null,
            @Query("middle_area") middleArea: String? = null,
            @Query("small_area") smallArea: String? = null,

            // 位置検索関連
            @Query("lat") lat: Double? = null,
            @Query("lng") lng: Double? = null,
            @Query("range") range: Int? = null,
            @Query("datum") datum: String? = null,

            // キーワード・ジャンル・予算
            @Query("keyword") keyword: String? = null,
            @Query("genre") genre: String? = null,
            @Query("budget") budget: String? = null,
            @Query("party_capacity") partyCapacity: Int? = null,

            // 特集関連
            @Query("special") special: String? = null,
            @Query("special_or") specialOr: String? = null,
            @Query("special_category") specialCategory: String? = null,
            @Query("special_category_or") specialCategoryOr: String? = null,

            // 設備・サービス
            @Query("wifi") wifi: Int? = null,
            @Query("wedding") wedding: Int? = null,
            @Query("course") course: Int? = null,
            @Query("free_drink") freeDrink: Int? = null,
            @Query("free_food") freeFood: Int? = null,
            @Query("private_room") privateRoom: Int? = null,
            @Query("horigotatsu") horigotatsu: Int? = null,
            @Query("tatami") tatami: Int? = null,
            @Query("cocktail") cocktail: Int? = null,
            @Query("shochu") shochu: Int? = null,
            @Query("sake") sake: Int? = null,
            @Query("wine") wine: Int? = null,
            @Query("card") card: Int? = null,
            @Query("non_smoking") nonSmoking: Int? = null,
            @Query("charter") charter: Int? = null,
            @Query("ktai") ktai: Int? = null,
            @Query("parking") parking: Int? = null,
            @Query("barrier_free") barrierFree: Int? = null,
            @Query("sommelier") sommelier: Int? = null,
            @Query("night_view") nightView: Int? = null,
            @Query("open_air") openAir: Int? = null,
            @Query("show") show: Int? = null,
            @Query("equipment") equipment: Int? = null,
            @Query("karaoke") karaoke: Int? = null,
            @Query("band") band: Int? = null,
            @Query("tv") tv: Int? = null,
            @Query("lunch") lunch: Int? = null,
            @Query("midnight") midnight: Int? = null,
            @Query("midnight_meal") midnightMeal: Int? = null,
            @Query("english") english: Int? = null,
            @Query("pet") pet: Int? = null,
            @Query("child") child: Int? = null,

            // クレジットカード
            @Query("credit_card") creditCard: String? = null,

            // レスポンス制御
            @Query("type") type: String? = null,
            @Query("order") order: Int? = null,
            @Query("start") start: Int? = null,
            @Query("count") count: Int? = null,
            @Query("format") format: String = "json"
        ): GourmetSearchResponse

        @GET("large_area/v1/")
        suspend fun getLargeAreas(
            @Query("key") key: String,
            @Query("format") format: String = "json"
        ): LargeAreaResponse

        @GET("middle_area/v1/")
        suspend fun getMiddleAreas(
            @Query("key") key: String,
            @Query("middle_area") middleArea: String? = null,
            @Query("large_area") largeArea: String? = null,
            @Query("format") format: String = "json"
        ): MiddleAreaResponse

        @GET("small_area/v1/")
        suspend fun getSmallAreas(
            @Query("key") key: String,
            @Query("small_area") smallArea: String? = null,
            @Query("middle_area") middleArea: String? = null,
            @Query("format") format: String = "json"
        ): SmallAreaResponse
    }

    private val apiKey: String by lazy {
        context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData.getString("com.hotpepper.GRUM_API_KEY") ?: ""
    }

    private val api = Retrofit.Builder()
        .baseUrl("https://webservice.recruit.co.jp/hotpepper/")
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(HotpepperApi::class.java)

    private suspend fun searchShop(query: SearchQuery, doCache: Boolean = false): GourmetSearchResponse {
        try {
            val response = api.searchShops(
                key = apiKey,
                id = query.id?.joinToString(","),
                name = query.name,
                nameKana = query.nameKana,
                nameAny = query.nameAny,
                tel = query.tel,
                address = query.address,

                // エリア関連
                largeServiceArea = query.largeServiceArea,
                serviceArea = query.serviceArea?.joinToString(","),
                largeArea = query.largeArea?.joinToString(","),
                middleArea = query.middleArea?.joinToString(","),
                smallArea = query.smallArea?.joinToString(","),

                // 位置検索関連
                lat = query.lat,
                lng = query.lng,
                range = query.range,
                datum = query.datum,

                // キーワード・ジャンル・予算
                keyword = query.keyword,
                genre = query.genre?.joinToString(","),
                budget = query.budget?.joinToString(","),
                partyCapacity = query.partyCapacity,

                // 特集関連
                special = query.special?.joinToString(","),
                specialOr = query.specialOr?.joinToString(","),
                specialCategory = query.specialCategory?.joinToString(","),
                specialCategoryOr = query.specialCategoryOr?.joinToString(","),

                // 設備・サービス（Boolean -> Int変換）
                wifi = query.wifi?.let { if (it) 1 else 0 },
                wedding = query.wedding?.let { if (it) 1 else 0 },
                course = query.course?.let { if (it) 1 else 0 },
                freeDrink = query.freeDrink?.let { if (it) 1 else 0 },
                freeFood = query.freeFood?.let { if (it) 1 else 0 },
                privateRoom = query.privateRoom?.let { if (it) 1 else 0 },
                horigotatsu = query.horigotatsu?.let { if (it) 1 else 0 },
                tatami = query.tatami?.let { if (it) 1 else 0 },
                cocktail = query.cocktail?.let { if (it) 1 else 0 },
                shochu = query.shochu?.let { if (it) 1 else 0 },
                sake = query.sake?.let { if (it) 1 else 0 },
                wine = query.wine?.let { if (it) 1 else 0 },
                card = query.card?.let { if (it) 1 else 0 },
                nonSmoking = query.nonSmoking?.let { if (it) 1 else 0 },
                charter = query.charter?.let { if (it) 1 else 0 },
                ktai = query.ktai?.let { if (it) 1 else 0 },
                parking = query.parking?.let { if (it) 1 else 0 },
                barrierFree = query.barrierFree?.let { if (it) 1 else 0 },
                sommelier = query.sommelier?.let { if (it) 1 else 0 },
                nightView = query.nightView?.let { if (it) 1 else 0 },
                openAir = query.openAir?.let { if (it) 1 else 0 },
                show = query.show?.let { if (it) 1 else 0 },
                equipment = query.equipment?.let { if (it) 1 else 0 },
                karaoke = query.karaoke?.let { if (it) 1 else 0 },
                band = query.band?.let { if (it) 1 else 0 },
                tv = query.tv?.let { if (it) 1 else 0 },
                lunch = query.lunch?.let { if (it) 1 else 0 },
                midnight = query.midnight?.let { if (it) 1 else 0 },
                midnightMeal = query.midnightMeal?.let { if (it) 1 else 0 },
                english = query.english?.let { if (it) 1 else 0 },
                pet = query.pet?.let { if (it) 1 else 0 },
                child = query.child?.let { if (it) 1 else 0 },

                // クレジットカード
                creditCard = query.creditCard?.joinToString(","),

                // レスポンス制御
                type = query.type?.toQuery(),
                order = query.order,
                start = query.start,
                count = query.count ?: SearchQuery.DEFAULT_COUNT
            )
            if (doCache) {
                for (result in response.results.shop) {
                    val cache = shopDetailCache[result.id]
                    if (cache != null) {
                        val (type, shop) = cache
                        if (!type.contains(query.type)) {
                            shopDetailCache.put(
                                result.id,
                                Pair(type.merge(query.type), mergeShops(shop, result))
                            )
                        }
                    } else {
                        shopDetailCache.put(result.id, Pair(query.type ?: QueryType.DETAIL, result))
                    }
                }
            }
            Log.d("SearchManager", "searchShops: ${response.results.resultsReturned}")
            return response
        } catch (e: Exception){
            Log.d("SearchManager", "searchShops: ${e.message}")
            throw e
        }
    }

    suspend fun search(query: SearchQuery, doCache: Boolean = false): Result<GourmetSearchResults> {
        return try {
            val response = searchShop(query, doCache = doCache)
            Log.d("SearchManager", "search: ${response.results.resultsReturned}")
            Result.success(response.results)
        } catch (e: Exception) {
            Log.d("SearchManager", "search: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun searchShopsByID(ids: List<String>, type: QueryType = QueryType.DETAIL, doCache: Boolean = true): Result<List<Shop>?> {
        return try {
            val cached = ids.mapNotNull { shopDetailCache[it] }.filter{it.first.contains(type)}.map{it.second}
            if (cached.size == ids.size) {
                Result.success(cached)
            } else {
                val cachedIds = cached.map { it.id }
                val response = searchShop(
                    SearchQuery(
                        id = ids.filter { !cachedIds.contains(it) },
                        type = type
                    ),
                    doCache = doCache
                )
                Result.success(response.results.shop)
            }
        } catch (e: Exception) {
            Log.d("SearchManager", "searchShopsByID: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun searchShopByID(id: String, type: QueryType = QueryType.DETAIL, doCache: Boolean = true): Result<Shop?> {
        return try {
            val cached = shopDetailCache[id]
            if (cached != null && cached.first.contains(type)) {
                Result.success(cached.second)
            } else {
                val response = searchShop(
                    SearchQuery(
                        id = listOf(id),
                        type = type
                    ),
                    doCache = doCache
                )
                Result.success(response.results.shop.firstOrNull())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private var shopDetailCache: LruCache<String, Pair<QueryType,Shop>> = LruCache(20)
    private var largeServiceAreaCache: List<ServiceArea>? = null
    private var largeAreasCache: List<LargeArea>? = null
    private val middleAreasCache = mutableMapOf<String, List<MiddleArea>>()
    private val smallAreasCache = mutableMapOf<String, List<SmallArea>>()

    suspend fun getLargeServiceAreas(): Result<List<ServiceArea>> = withContext(Dispatchers.IO) {
        largeServiceAreaCache?.let{
            return@withContext Result.success(it)
        }
        try {
            val response = api.getLargeAreas(apiKey)
            val largeAreas = response.results.largeAreas.map { largeAreaResponse ->
                LargeArea(largeAreaResponse)
            }
            // キャッシュに保存
            val largeServiceAreas = largeAreas.map { largeArea -> largeArea.parentArea}.distinctBy {largeServiceArea -> largeServiceArea.code}
            largeServiceAreaCache = largeServiceAreas
            largeAreasCache = largeAreas
            Result.success(largeServiceAreas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLargeServiceArea(serviceAreaCode: String): Result<ServiceArea> = withContext(Dispatchers.IO) {
        try{
            if (largeServiceAreaCache == null){
                val response = api.getLargeAreas(apiKey)
                val largeAreas = response.results.largeAreas.map { largeAreaResponse ->
                    LargeArea(largeAreaResponse)
                }
                // キャッシュに保存
                val largeServiceAreas = largeAreas.map { largeArea -> largeArea.parentArea}.distinctBy {largeServiceArea -> largeServiceArea.code}
                largeServiceAreaCache = largeServiceAreas
                largeAreasCache = largeAreas
            }
            largeServiceAreaCache
                ?.find{serviceArea -> serviceArea.code == serviceAreaCode}
                ?.let {
                    return@withContext Result.success(it)
                }
            Result.failure(Exception("Cannot find ServiceArea $serviceAreaCode"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLargeAreas(serviceAreaCode: String?=null): Result<List<LargeArea>> = withContext(Dispatchers.IO) {
        // キャッシュがあれば返す
        largeAreasCache?.let {
            if (serviceAreaCode == null){
                return@withContext Result.success(it)
            }
            return@withContext Result.success(it.filter{area->area.parentArea.code == serviceAreaCode})
        }

        try {
            val response = api.getLargeAreas(apiKey)
            val largeAreas = response.results.largeAreas.map { largeAreaResponse ->
                LargeArea(largeAreaResponse)
            }
            // キャッシュに保存
            val largeServiceAreas = largeAreas.map { largeArea -> largeArea.parentArea}.distinctBy {largeServiceArea -> largeServiceArea.code}
            largeServiceAreaCache = largeServiceAreas
            largeAreasCache = largeAreas
            if (serviceAreaCode == null) {
                Result.success(largeAreas)
            }else{
                Result.success(largeAreas.filter{area->area.parentArea.code == serviceAreaCode})
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLargeArea(serviceAreaCode: String): Result<LargeArea> = withContext(Dispatchers.IO) {
        try{
            if (largeAreasCache == null){
                val response = api.getLargeAreas(apiKey)
                val largeAreas = response.results.largeAreas.map { largeAreaResponse ->
                    LargeArea(largeAreaResponse)
                }
                // キャッシュに保存
                val largeServiceAreas = largeAreas.map { largeArea -> largeArea.parentArea}.distinctBy {largeServiceArea -> largeServiceArea.code}
                largeServiceAreaCache = largeServiceAreas
                largeAreasCache = largeAreas
            }
            largeAreasCache
                ?.find{serviceArea -> serviceArea.code == serviceAreaCode}
                ?.let {
                    return@withContext Result.success(it)
                }
            Result.failure(Exception("Cannot find LargeArea $serviceAreaCode"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMiddleAreas(largeAreaCode: String): Result<List<MiddleArea>> = withContext(Dispatchers.IO) {
        // キャッシュがあれば返す
        middleAreasCache[largeAreaCode]?.let {
            return@withContext Result.success(it)
        }

        try {
            val response = api.getMiddleAreas(apiKey, largeArea = largeAreaCode)
            val middleAreas = response.results.middleAreas.map { middleAreaResponse ->
                MiddleArea(middleAreaResponse)
            }
            // キャッシュに保存
            middleAreasCache[largeAreaCode] = middleAreas
            Result.success(middleAreas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMiddleArea(middleAreaCode: String): Result<MiddleArea> = withContext(Dispatchers.IO) {
        middleAreasCache.values
            .flatten()
            .find { it.code == middleAreaCode }
            ?.let { return@withContext Result.success(it) }

        try {
            val response = api.getMiddleAreas(apiKey, middleArea = middleAreaCode)
            val middleArea = response.results.middleAreas
                .firstOrNull{middleAreaResponse -> middleAreaResponse.code == middleAreaCode}
                ?.let{
                    MiddleArea(it)
                }
            if (middleArea == null){
                Result.failure(Exception("Cannot find MiddleArea $middleAreaCode"))
            }
            else
            {
                val res2 = api.getMiddleAreas(apiKey, largeArea = middleArea.parentArea.code)
                val middleAreas = res2.results.middleAreas.map { middleAreaResponse ->
                    MiddleArea(middleAreaResponse)
                }
                // キャッシュに保存
                middleAreasCache[middleArea.parentArea.code] = middleAreas
                Result.success(middleArea)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSmallAreas(middleAreaCode: String): Result<List<SmallArea>> = withContext(Dispatchers.IO) {
        // キャッシュがあれば返す
        smallAreasCache[middleAreaCode]?.let {
            return@withContext Result.success(it)
        }

        try {
            val response = api.getSmallAreas(apiKey, middleArea = middleAreaCode)
            val smallAreas = response.results.smallAreas.map { smallAreaResponse ->
                SmallArea(smallAreaResponse)
            }
            // キャッシュに保存
            smallAreasCache[middleAreaCode] = smallAreas
            Result.success(smallAreas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getSmallArea(smallAreaCode: String): Result<SmallArea> = withContext(Dispatchers.IO) {
        smallAreasCache.values
            .flatten()
            .find { it.code == smallAreaCode }
            ?.let { return@withContext Result.success(it) }

        try {
            val response = api.getSmallAreas(apiKey, smallArea = smallAreaCode)
            val smallArea = response.results.smallAreas
                .firstOrNull{smallAreaResponse -> smallAreaResponse.code == smallAreaCode}
                ?.let{
                    SmallArea(it)
                }
            if (smallArea == null){
                Result.failure(Exception("Cannot find SmallArea $smallAreaCode"))
            }
            else
            {
                val res2 = api.getSmallAreas(apiKey, middleArea = smallArea.parentArea.code)
                val smallAreas = res2.results.smallAreas.map { smallAreaResponse ->
                    SmallArea(smallAreaResponse)
                }
                // キャッシュに保存
                smallAreasCache[smallArea.parentArea.code] = smallAreas
                Result.success(smallArea)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChildAreas(areaCode: String): Result<List<Area>> = withContext(Dispatchers.IO){
        val areaType = Area.getAreaType(areaCode)
        when(areaType) {
            AreaType.Service -> getLargeAreas(areaCode)
            AreaType.LARGE -> getMiddleAreas(areaCode)
            AreaType.MIDDLE -> getSmallAreas(areaCode)
            AreaType.SMALL -> Result.failure(Exception("$areaCode is Small Area Type"))
            else -> Result.failure(Exception("Invalid area type"))
        }
    }

    suspend fun getArea(areaCode: String): Result<Area> = withContext(Dispatchers.IO){
        val areaType = Area.getAreaType(areaCode)
        when(areaType) {
            AreaType.Service -> getLargeServiceArea(areaCode)
            AreaType.LARGE -> getLargeArea(areaCode)
            AreaType.MIDDLE -> getMiddleArea(areaCode)
            AreaType.SMALL -> getSmallArea(areaCode)
            else -> Result.failure(Exception("Invalid area type"))
        }
    }

    fun clearCache() {
        largeAreasCache = null
        middleAreasCache.clear()
        smallAreasCache.clear()
    }

    suspend fun findMatchingArea(address: android.location.Address): SmallArea? {
        try {
            // 都道府県から検索
            getLargeAreas().getOrNull()?.let { largeAreas ->
                val matchingLargeArea = largeAreas.find { address.adminArea.contains(it.name) }
                matchingLargeArea?.let { largeArea ->
                    // 市区町村から検索
                    getMiddleAreas(largeArea.code).getOrNull()?.let { middleAreas ->
                        val matchingMiddleArea = middleAreas.find { address.locality.contains(it.name) }
                        matchingMiddleArea?.let { middleArea ->
                            // 小エリアを検索して最長一致を探す
                            getSmallAreas(middleArea.code).getOrNull()?.let { smallAreas ->
                                val subLocality = address.subLocality ?: ""
                                val matchingSmallArea = smallAreas.maxByOrNull { area ->
                                    val intersection = area.name.commonPrefixWith(subLocality)
                                    intersection.length
                                }
                                return matchingSmallArea
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("","エリア情報の取得に失敗しました")
        }
        return null
    }
    private val _visitedShops = MutableStateFlow<List<Shop>>(emptyList())
    val visitedShops: StateFlow<List<Shop>> = _visitedShops.asStateFlow()

    // 履歴の最大保持数
    private val maxHistorySize = 20

    // 履歴に店舗を追加
    fun addToHistory(shop: Shop) {
        val currentList = _visitedShops.value.toMutableList()

        // 既に存在する場合は削除（後で先頭に追加し直す）
        currentList.removeIf { it.id == shop.id }

        // 先頭に追加
        currentList.add(0, shop)

        // 最大数超過時は古いものを削除
        if (currentList.size > maxHistorySize) {
            currentList.removeAt(currentList.lastIndex)
        }

        _visitedShops.value = currentList
    }

    // 検索結果など、複数店舗を一度に履歴に追加
    fun addAllToHistory(shops: List<Shop>) {
        val uniqueShops = shops.take(5) // 一度に追加するのは最大5件まで
        uniqueShops.forEach { addToHistory(it) }
    }
}