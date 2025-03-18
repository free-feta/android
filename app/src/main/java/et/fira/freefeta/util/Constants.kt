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
//            "sendid" to "1012673623603201:978019208678401",
            "sendid" to "1012673623603201:978374728448001",
        )
    }

    object Worker {
        val UPDATE_SYNC_NOTIFICATION_CHANNEL_NAME: CharSequence =
            "Update Sync Notifications"
        const val UPDATE_SYNC_NOTIFICATION_CHANNEL_DESCRIPTION =
            "Shows notifications whenever update is available"
        val APP_UPDATE_NOTIFICATION_TITLE: CharSequence = "App Update Available"
        val NEW_FILE_RELEASE_NOTIFICATION_TITLE: CharSequence = "New file release"
        const val UPDATE_SYNC_CHANNEL_ID = "UPDATE_SYNC_NOTIFICATION"

        const val UPDATE_SYNC_WORK_NAME = "UPDATE_SYNC_WORK"

    }
}