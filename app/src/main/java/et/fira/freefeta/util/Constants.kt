package et.fira.freefeta.util

object AppConstants {
    object File {
        const val DOWNLOAD_FOLDER_NAME = "FreeFeta"
    }

    object Network {
        const val ZERO_RATING_URL = "https://telebirrchat.ethiomobilemoney.et:21006/sfs/ufile?digest=fid5e4d5ff5bb0f20b7e70ec3d0bb01d1d2&filename=Khalid+-+Young+Dumb+_+Broke+(Lyrics)(720P_HD).mp4"
        val HEADER_FOR_ZERO_RATING_URL = hashMapOf(
            "appid" to "1012673623603201",
            "access-token" to "34A1367993D9409639D081B3A91159D37FD5DF999E2B97C8C822F92E44DE6A65",
            "User-Agent" to "Dalvik/2.1.0 (Linux; U; Android 7.1.2; ASUS_Z01QD Build/N2G48H)",
            "sendid" to "1012673623603201:978019208678401",
        )

    }
}