package et.fira.freefeta.ui.player

import androidx.lifecycle.ViewModel
import et.fira.freefeta.data.file.LocalFileRepository

class PlayerViewModel(
    private val localFileRepository: LocalFileRepository,
): ViewModel() {
}