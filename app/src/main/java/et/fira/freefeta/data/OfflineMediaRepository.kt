package et.fira.freefeta.data

import et.fira.freefeta.model.Media
import kotlinx.coroutines.flow.Flow

class OfflineMediaRepository(
    private val mediaDao: MediaDao
): MediaRepository {
    override fun getAllMediasStream(): Flow<List<Media>> = mediaDao.getAllItems()

    override fun getMediaStream(id: Int): Flow<Media?> = mediaDao.getMedia(id)

    override suspend fun insertMedia(media: Media) = mediaDao.insert(media)

    override suspend fun deleteMedia(media: Media) = mediaDao.delete(media)

    override suspend fun updateMedia(media: Media) = mediaDao.update(media)
}