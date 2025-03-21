package et.fira.freefeta.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import et.fira.freefeta.data.file.LocalFileRepository
import et.fira.freefeta.model.FileType
import et.fira.freefeta.model.MediaType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(private val repository: LocalFileRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFileType = MutableStateFlow<FileType?>(null)
    val selectedFileType = _selectedFileType.asStateFlow()

    private val _selectedMediaType = MutableStateFlow<MediaType?>(null)
    val selectedMediaType = _selectedMediaType.asStateFlow()

    val availableFileTypes = repository.getAllFileTypes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val availableMediaTypes = repository.getAllMediaTypes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Combine the search query and filters to get the filtered results
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults = combine(
        searchQuery,
        selectedFileType,
        selectedMediaType
    ) { query, fileType, mediaType ->
        Triple(query, fileType, mediaType)
    }
        .flatMapLatest { (query, fileType, mediaType) ->
            repository.searchFiles(query, fileType, mediaType)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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