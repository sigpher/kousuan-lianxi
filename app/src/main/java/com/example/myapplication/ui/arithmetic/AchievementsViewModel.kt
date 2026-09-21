package com.example.myapplication.ui.arithmetic

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.arithmetic.ArithmeticRepository
import com.example.myapplication.arithmetic.ArithmeticRepository.Record
import com.example.myapplication.arithmetic.RewardRules

class AchievementsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ArithmeticRepository(application)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _crownCount = MutableLiveData(0)
    val crownCount: LiveData<Int> = _crownCount

    private val _sapphireCount = MutableLiveData(0)
    val sapphireCount: LiveData<Int> = _sapphireCount

    private val _records = MutableLiveData<List<Record>>(emptyList())
    val records: LiveData<List<Record>> = _records

    fun loadCrownCount() {
        repository.countRewards(RewardRules.CROWN) { count ->
            mainHandler.post { _crownCount.value = count }
        }
    }

    fun loadSapphireCount() {
        repository.countRewards(RewardRules.SAPPHIRE) { count ->
            mainHandler.post { _sapphireCount.value = count }
        }
    }

    fun loadRecords() {
        repository.fetchRecords { records ->
            mainHandler.post { _records.value = records }
        }
    }
}