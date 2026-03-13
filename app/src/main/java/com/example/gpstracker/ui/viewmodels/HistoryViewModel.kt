package com.example.gpstracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gpstracker.data.database.LocationDao
import com.example.gpstracker.data.database.SessionSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val locationDao: LocationDao
) : ViewModel() {

    val sessions: StateFlow<List<SessionSummary>> = locationDao.getAllSessions().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            locationDao.deleteSession(sessionId)
        }
    }

    suspend fun getPointsForSession(sessionId: Long) = locationDao.getLocationPointsForSessionSync(sessionId)
}
