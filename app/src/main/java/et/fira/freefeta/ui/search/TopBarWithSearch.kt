package et.fira.freefeta.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import et.fira.freefeta.R
import et.fira.freefeta.network.NetworkState
import et.fira.freefeta.ui.network.NetworkStatusView
import et.fira.freefeta.ui.theme.FreeFetaTheme
import et.fira.freefeta.util.AppConstants
import kotlin.reflect.KFunction0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithSearch(
    networkState: NetworkState,
    modifier: Modifier = Modifier,
    onSearchQueryChanged: (String) -> Unit = {},
    isSearchActive: Boolean,
    onSearchActiveChanged: (Boolean) -> Unit,
    isFilterActive: Boolean,
    onFilterActiveChanged: (Boolean) -> Unit,
    restartNetworkStateMonitoring: KFunction0<Unit>
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val uriHandler = LocalUriHandler.current

    Column {
        TopAppBar(
            title = {},
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    IconButton(
                        onClick = {
                            uriHandler.openUri(AppConstants.About.APP_TG_CHANNEL)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.telegram_icon),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                        if (isSearchActive) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Spacer(modifier = Modifier.width(8.dp))

                                TextField(
                                    value = searchText,
                                    onValueChange = {
                                        searchText = it
                                        onSearchQueryChanged(it.text)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    placeholder = { Text("Search...") },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {keyboardController?.hide()}
                                    )
                                )

                                LaunchedEffect(isSearchActive) {
                                    if (isSearchActive) {
                                        focusRequester.requestFocus()
                                    }
                                }
                            }

                            IconButton(onClick = {
                                searchText = TextFieldValue("")
                                onSearchActiveChanged(false)
                                onSearchQueryChanged("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close search")
                            }
                        } else {
                            IconButton(onClick = { onSearchActiveChanged(true)}) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }

                    IconButton(
                        onClick = {onFilterActiveChanged(!isFilterActive)},
                    ) {
                        Icon(painterResource(
                            R.drawable.filter_list_icon),
                            "Filter",
                            tint = if(isFilterActive) MaterialTheme.colorScheme.primary else
                                MaterialTheme.colorScheme.onSurface )
                    }
                    if (isSearchActive) Spacer(Modifier.width(16.dp))

                    NetworkStatusView(state = networkState,
                        restartNetworkStateMonitoring = restartNetworkStateMonitoring)
                }
            }
        )

    }

}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipsRow(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}
//
//@Composable
//fun FilterChipsRow(
//    categories: List<String>,
//    onCategorySelected: (String) -> Unit
//) {
//    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
//
//    LazyRow(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        contentPadding = PaddingValues(horizontal = 16.dp)
//    ) {
//        items(categories) { category ->
//            val isSelected = selectedCategories.contains(category)
//
//            FilterChip(
//                selected = isSelected,
//                onClick = {
//                    selectedCategories = if (isSelected) {
//                        selectedCategories - category
//                    } else {
//                        selectedCategories + category
//                    }
//                    onCategorySelected(category)
//                },
//                label = { Text(category) },
//                colors = FilterChipDefaults.filterChipColors(
//                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
//                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
//                )
//            )
//        }
//    }
//}

@Composable
fun FilterFiles(modifier: Modifier = Modifier) {
    IconButton(
        onClick = {}
    ) {
        Icon(
            painter = painterResource(R.drawable.filter_list_icon),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = modifier.size(32.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    FreeFetaTheme {
        TopBarWithSearch(
            NetworkState.NoInternet,
            isSearchActive = false,
            onSearchActiveChanged = { },
            isFilterActive = false,
            onFilterActiveChanged = {},
            restartNetworkStateMonitoring = {} as KFunction0
        )
    }
}