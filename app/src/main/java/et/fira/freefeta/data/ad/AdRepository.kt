package et.fira.freefeta.data.ad

import android.util.Log
import et.fira.freefeta.model.Advertisement
import et.fira.freefeta.network.FreeFetaApiService
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.coroutineContext

interface AdRepository {
    suspend fun insertAd(advertisement: Advertisement)
    suspend fun insertAd(advertisements: List<Advertisement>)
    suspend fun deleteAd(advertisement: Advertisement)
    fun getAd(id: Int): Flow<Advertisement>
    fun getAllAdsStream(): Flow<List<Advertisement>>
    suspend fun fetchRemoteAds(): List<Advertisement>
    suspend fun syncNewAds(): Int
    suspend fun getStartUpAd(): Advertisement?
    suspend fun getOnDemandAd(): Advertisement?
}

class AdRepositoryImpl(
    private val adDao: AdDao,
    private val freeFetaApiService: FreeFetaApiService,
): AdRepository {
    override suspend fun insertAd(advertisement: Advertisement) = adDao.insert(advertisement)
    override suspend fun insertAd(advertisements: List<Advertisement>) = adDao.insert(advertisements)

    override suspend fun deleteAd(advertisement: Advertisement) = adDao.delete(advertisement)
    override fun getAd(id: Int): Flow<Advertisement> = adDao.getAd(id)

    override fun getAllAdsStream(): Flow<List<Advertisement>> = adDao.getAllAdsStream()
    override suspend fun fetchRemoteAds(): List<Advertisement> = freeFetaApiService.getAds()

    override suspend fun syncNewAds(): Int {
        try {
            val fetchedAds = fetchRemoteAds()
            insertAd(fetchedAds)
            return fetchedAds.size
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            e.printStackTrace()
            return 0
        }
    }

    override suspend fun getStartUpAd(): Advertisement? {
        syncNewAds()
        try {
            val ads = adDao.getAllAds()
//            Log.d("AdRepositoryImpl", "Ads filtered random: ${ads.filter { it.showOnStartup }.random()}")
            val starUpAd = ads.filter { it.showOnStartup }.random()

            if (starUpAd.isOneTime) {
                adDao.delete(starUpAd)
            }
            return starUpAd
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            e.printStackTrace()
            return null
        }
    }

    override suspend fun getOnDemandAd(): Advertisement? {
        syncNewAds()
        try {
            val ads = adDao.getAllAds()
            Log.d("AdRepositoryImpl", "Ads 4 on demand: $ads")
            val onDemandAd = ads.filter { !it.showOnStartup }.random()
            if (onDemandAd.isOneTime) {
                adDao.delete(onDemandAd)
            }
            return onDemandAd
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            e.printStackTrace()
            return null
        }

    }

}