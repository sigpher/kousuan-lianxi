package com.example.myapplication.ui.arithmetic

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.arithmetic.AccountRepository
import com.example.myapplication.arithmetic.ArithmeticRepository
import com.example.myapplication.arithmetic.ArithmeticRepository.Record
import com.example.myapplication.arithmetic.RewardRules

class AchievementsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ArithmeticRepository(application)
    private val accountRepository = AccountRepository(application)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _yellowFlowerCount = MutableLiveData(0)
    val yellowFlowerCount: LiveData<Int> = _yellowFlowerCount

    private val _redHeartCount = MutableLiveData(0)
    val redHeartCount: LiveData<Int> = _redHeartCount

    private val _sapphireCount = MutableLiveData(0)
    val sapphireCount: LiveData<Int> = _sapphireCount

    private val _crownCount = MutableLiveData(0)
    val crownCount: LiveData<Int> = _crownCount

    private val _records = MutableLiveData<List<Record>>(emptyList())
    val records: LiveData<List<Record>> = _records

    fun loadRewardCounts() {
        val userId = accountRepository.currentUserId()
        repository.countRewards(RewardRules.YELLOW_FLOWER, userId) { count ->
            mainHandler.post { _yellowFlowerCount.value = count }
        }
        repository.countRewards(RewardRules.RED_HEART, userId) { count ->
            mainHandler.post { _redHeartCount.value = count }
        }
        repository.countRewards(RewardRules.SAPPHIRE, userId) { count ->
            mainHandler.post { _sapphireCount.value = count }
        }
        repository.countRewards(RewardRules.CROWN, userId) { count ->
            mainHandler.post { _crownCount.value = count }
        }
    }

    fun loadRecords() {
        repository.fetchRecords(accountRepository.currentUserId()) { records ->
            mainHandler.post { _records.value = records }
        }
    }
}