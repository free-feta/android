package et.fira.freefeta.data

interface FileDownloaderRepository {
    fun download(url: String) : Int

}