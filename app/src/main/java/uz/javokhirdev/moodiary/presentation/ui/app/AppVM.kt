package uz.javokhirdev.moodiary.presentation.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import uz.javokhirdev.moodiary.data.db.days.DayEntity
import uz.javokhirdev.moodiary.data.db.days.DaysRepository
import uz.javokhirdev.moodiary.utils.*
import uz.javokhirdev.moodiary.utils.DATE_FORMAT_3
import javax.inject.Inject

@HiltViewModel
class AppVM @Inject constructor(private val daysRepository: DaysRepository) : ViewModel() {

    private val _todayState = MutableStateFlow<UIState<DayEntity>>(UIState.Ready)
    val todayState = _todayState.asStateFlow()

    fun getDay() {
        _todayState.value = UIState.loading(true)

        viewModelScope.launch {
            try {
                val today = DateUtils.getToday(DATE_FORMAT_1)

                val entity = daysRepository.getDay(today)

                _todayState.value = UIState.loading(false)
                _todayState.value = UIState.success(entity)
            } catch (ex: Exception) {
                _todayState.value = UIState.loading(false)
                _todayState.value = UIState.failure(ex.message.orEmpty(), 0)
            }
        }
    }

    fun insertOrUpdate(isGoodDay: Boolean) {
        viewModelScope.launch {
            try {
                val dateTime = DateTime()
                val today = dateTime.toString(DATE_FORMAT_1)
                val dayOfMonth = dateTime.toString(DAY_PATTERN)
                val monthAndYear = dateTime.toString(DATE_FORMAT_3)
                val entity = DayEntity(today, dayOfMonth, monthAndYear, isGoodDay)

                daysRepository.insert(entity)

                getDay()
            } catch (ex: Exception) {
                getDay()
            }
        }
    }
}