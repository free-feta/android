package et.fira.freefeta.data

import et.fira.freefeta.model.Media
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    /**
     * Retrieve all the medias from the the given data source.
     */
    fun getAllMediasStream(): Flow<List<Media>>

    /**
     * Retrieve an media from the given data source that matches with the [id].
     */
    fun getMediaStream(id: Int): Flow<Media?>

    /**
     * Insert media in the data source
     */
    suspend fun insertMedia(media: Media)

    /**
     * Delete media from the data source
     */
    suspend fun deleteMedia(media: Media)

    /**
     * Update media in the data source
     */
    suspend fun updateMedia(media: Media)
}