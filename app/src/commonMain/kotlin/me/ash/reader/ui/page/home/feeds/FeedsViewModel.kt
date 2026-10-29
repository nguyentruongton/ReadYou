package me.ash.reader.ui.page.home.feeds

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.ash.reader.*
import me.ash.reader.domain.data.DiffMapHolder
import me.ash.reader.domain.data.FilterState
import me.ash.reader.domain.data.FilterStateUseCase
import me.ash.reader.domain.data.GroupWithFeedsListUseCase
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.platform.StringsHelper
import me.ash.reader.infrastructure.platform.SyncScheduler
import me.ash.reader.infrastructure.preference.SettingsProvider

private const val TAG = "FeedsViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
class FeedsViewModel(
    private val accountService: AccountService,
    private val rssService: RssService,
    private val syncScheduler: SyncScheduler,
    private val stringsHelper: StringsHelper,
    private val defaultDispatcher: CoroutineDispatcher,
    private val ioDispatcher: CoroutineDispatcher,
    private val applicationScope: CoroutineScope,
    private val settingsProvider: SettingsProvider,
    private val diffMapHolder: DiffMapHolder,
    private val filterStateUseCase: FilterStateUseCase,
    private val groupWithFeedsListUseCase: GroupWithFeedsListUseCase,
) : ViewModel() {

    private val _feedsUiState =
        MutableStateFlow(FeedsUiState())
    val feedsUiState: StateFlow<FeedsUiState> = _feedsUiState.asStateFlow()

    val isSyncing: StateFlow<Boolean> = syncScheduler.isSyncing

    val filterStateFlow = filterStateUseCase.filterStateFlow
    val groupWithFeedsListFlow = groupWithFeedsListUseCase.groupWithFeedListFlow

    private var pullImportantJob: Job? = null

    init {
        viewModelScope.launch {
            filterStateFlow.collect {
                pullImportant(it.filter)
            }
        }
        viewModelScope.launch {
            accountService.currentAccountFlow.collect { account ->
                if (account != null) {
                    _feedsUiState.update {
                        it.copy(account = account)
                    }
                }
            }
        }
    }

    fun pullImportant(filter: Filter) {
        pullImportantJob?.cancel()
        pullImportantJob =
            when (filter) {
                Filter.Starred -> pullStarredFeeds()
                Filter.Unread -> pullUnreadFeeds()
                else -> pullAllFeeds()
            }
    }

    fun sync() {
        syncScheduler.triggerImmediateSync()
    }

    fun commitDiffs() {
        diffMapHolder.commitDiffsToDb()
    }

    fun switchAccount(account: Account) {
        viewModelScope.launch(ioDispatcher) {
            accountService.switch(account)
        }
    }

    fun setGroupVisible(groupId: String, visible: Boolean) {
        _feedsUiState.value.groupsVisible[groupId] = visible
    }

    fun changeFilter(filterState: FilterState) {
        viewModelScope.launch(defaultDispatcher) {
            filterStateUseCase.updateFilterState(filterState)
        }
    }

    private fun pullAllFeeds(): Job {
        val articleCountMapFlow =
            rssService.get().pullImportant(isStarred = false, isUnread = false)

        return viewModelScope.launch {
            launch {
                articleCountMapFlow.mapLatest {
                    val sum = it.values.sum()
                    stringsHelper.getQuantity(Res.plurals.all_desc, sum, sum)
                }.flowOn(defaultDispatcher).collect { text ->
                    _feedsUiState.update { it.copy(importantSum = text) }
                }
            }
        }
    }

    private fun pullStarredFeeds(): Job {
        val starredCountMap = rssService.get().pullImportant(isStarred = true, isUnread = false)

        return viewModelScope.launch {
            starredCountMap.mapLatest {
                val sum = it.values.sum()
                stringsHelper.getQuantity(Res.plurals.starred_desc, sum, sum)
            }.flowOn(defaultDispatcher).collect { text ->
                _feedsUiState.update { it.copy(importantSum = text) }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun pullUnreadFeeds(): Job {
        val unreadCountMapFlow = rssService.get().pullImportant(isStarred = false, isUnread = true)

        return viewModelScope.launch {
            diffMapHolder.diffMapSnapshotFlow
                .combine(
                    unreadCountMapFlow
                ) { diffMap, unreadCountMap ->
                    val sum = unreadCountMap.values.sum()
                    val combinedSum =
                        sum + diffMap.values.sumOf { if (it.isUnread) 1.toInt() else -1 }
                    stringsHelper.getQuantity(
                        Res.plurals.unread_desc,
                        combinedSum,
                        combinedSum
                    )
                }.debounce(200L).flowOn(defaultDispatcher).collect { text ->
                    _feedsUiState.update { it.copy(importantSum = text) }
                }
        }
    }
}

data class FeedsUiState(
    val account: Account? = null,
    val importantSum: String = "",
    val listState: LazyListState = LazyListState(),
    val groupsVisible: SnapshotStateMap<String, Boolean> = mutableStateMapOf(),
)
