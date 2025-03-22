package et.fira.freefeta.ui.search

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ketch.DownloadModel
import et.fira.freefeta.data.file.FileDownloaderRepository
import et.fira.freefeta.data.file.LocalFileRepository
import et.fira.freefeta.model.FileEntity
import et.fira.freefeta.model.FileType
import et.fira.freefeta.model.MediaType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(
    private val repository: LocalFileRepository,
    fileDownloaderRepository: FileDownloaderRepository,
    ) : ViewModel() {

    private val _searchQuery = MutableStateFlow(TextFieldValue(""))
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    private val _isFilterActive = MutableStateFlow(false)
    val isFilterActive = _isFilterActive.asStateFlow()

    private val _selectedFileType = MutableStateFlow<FileType?>(null)
    val selectedFileType = _selectedFileType.asStateFlow()

    private val _selectedMediaType = MutableStateFlow<MediaType?>(null)
    val selectedMediaType = _selectedMediaType.asStateFlow()

    val availableFileTypes = repository.getAllFileTypes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val availableMediaTypes = repository.getAllMediaTypes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Combine the search query and filters to get the filtered results
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchResults = combine(
        searchQuery,
        selectedFileType,
        selectedMediaType
    ) { query, fileType, mediaType ->
        Triple(query, fileType, mediaType)
    }
        .debounce(300)
        .flatMapLatest { (query, fileType, mediaType) ->
            repository.searchFiles(query.text, fileType, mediaType)
        }
//        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val downloadModelsFlow: Flow<List<DownloadModel>> = fileDownloaderRepository.observeDownloads()

    val uiState: StateFlow<SearchUiState> = combine(searchResults, downloadModelsFlow) { files, downloads ->
        val itemList: List<DownloadItemData> = files.map { file ->
            val download = downloads.find { it.id == file.downloadId }
            DownloadItemData(file, download)
        }
        SearchUiState.Success(itemList)
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
       SearchUiState.Loading
    )

    fun setSearchState(state: Boolean) {
        _isSearchActive.value = state
    }
    fun setFilterState(state: Boolean) {
        _isFilterActive.value = state
    }

    fun setSearchQuery(textFieldValue: TextFieldValue) {
        _searchQuery.value = textFieldValue
    }

    fun toggleFileTypeFilter(fileType: FileType) {
        _selectedFileType.value = if (_selectedFileType.value == fileType) null else fileType
    }

    fun toggleMediaTypeFilter(mediaType: MediaType) {
        _selectedMediaType.value = if (_selectedMediaType.value == mediaType) null else mediaType
    }

    fun clearFilters() {
        _selectedFileType.value = null
        _selectedMediaType.value = null
    }
}

sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(val downloadItemDataList: List<DownloadItemData> = emptyList()) : SearchUiState
}

data class DownloadItemData(
    val file: FileEntity,
    val downloadModel: DownloadModel?,
)
