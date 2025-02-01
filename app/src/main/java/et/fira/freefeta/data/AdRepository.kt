package et.fira.freefeta.data

import et.fira.freefeta.model.Advertisement
import kotlinx.coroutines.flow.Flow

interface AdRepository {
    suspend fun insertAd(advertisement: Advertisement)
    suspend fun deleteAd(advertisement: Advertisement)
    fun getAd(id: Int): Flow<Advertisement>
    fun getAllAds(): Flow<List<Advertisement>>
}

class AdRepositoryImpl(
    private val adDao: AdDao
): AdRepository {
    override suspend fun insertAd(advertisement: Advertisement) = adDao.insert(advertisement)

    override suspend fun deleteAd(advertisement: Advertisement) = adDao.delete(advertisement)
    override fun getAd(id: Int): Flow<Advertisement> = adDao.getAd(id)

    override fun getAllAds(): Flow<List<Advertisement>> = adDao.getAllAds()

}