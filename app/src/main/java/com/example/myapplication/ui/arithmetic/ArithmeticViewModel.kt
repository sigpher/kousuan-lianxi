package com.example.myapplication.ui.arithmetic

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.arithmetic.ArithmeticProblemGenerator
import com.example.myapplication.arithmetic.ArithmeticQuestion
import com.example.myapplication.arithmetic.ArithmeticRepository
import com.example.myapplication.arithmetic.EnergyRules
import com.example.myapplication.arithmetic.ResultComment
import com.example.myapplication.arithmetic.ReviewSelector
import com.example.myapplication.arithmetic.RewardRules
import com.example.myapplication.arithmetic.RoundComment
import com.example.myapplication.arithmetic.WrongProblem

enum class QuizPhase { SETUP, QUIZ, RESULT }

data class Feedback(val question: ArithmeticQuestion, val isCorrect: Boolean, val userAnswer: String)

class ArithmeticViewModel(application: Application) : AndroidViewModel(application) {

    private val generator = ArithmeticProblemGenerator()
    private val repository = ArithmeticRepository(application)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val advanceCallback = Runnable { advance() }
    private val timerCallback = object : Runnable {
        override fun run() {
            _elapsedSeconds.value = (System.currentTimeMillis() - startTime) / 1000
            if (_phase.value == QuizPhase.QUIZ) {
                _energy.value = EnergyRules.afterDrain(_energy.value ?: 0f)
                mainHandler.postDelayed(this, 1000)
            }
        }
    }

    private val _phase = MutableLiveData(QuizPhase.SETUP)
    val phase: LiveData<QuizPhase> = _phase

    private val _questionCount = MutableLiveData(100)
    val questionCount: LiveData<Int> = _questionCount

    private val _questions = MutableLiveData<List<ArithmeticQuestion>>(emptyList())
    private val _index = MutableLiveData(0)
    val index: LiveData<Int> = _index

    private val _input = MutableLiveData("")
    val input: LiveData<String> = _input

    private val _score = MutableLiveData(0)
    val score: LiveData<Int> = _score

    private val _feedback = MutableLiveData<Feedback?>(null)
    val feedback: LiveData<Feedback?> = _feedback

    private val _reviewRound = MutableLiveData(false)
    val reviewRound: LiveData<Boolean> = _reviewRound

    private val _wrongProblems = MutableLiveData<List<WrongProblem>>(emptyList())
    val wrongProblems: LiveData<List<WrongProblem>> = _wrongProblems

    private val _elapsedSeconds = MutableLiveData(0L)
    val elapsedSeconds: LiveData<Long> = _elapsedSeconds

    private val _crownEarned = MutableLiveData(false)
    val crownEarned: LiveData<Boolean> = _crownEarned

    private val _sapphireEarned = MutableLiveData(false)
    val sapphireEarned: LiveData<Boolean> = _sapphireEarned

    private val _canRedo = MutableLiveData(false)
    val canRedo: LiveData<Boolean> = _canRedo

    private val _combo = MutableLiveData(0)
    val combo: LiveData<Int> = _combo

    private val _energy = MutableLiveData(0f)
    val energy: LiveData<Float> = _energy

    private val _newRecord = MutableLiveData(false)
    val newRecord: LiveData<Boolean> = _newRecord

    private val _crownCount = MutableLiveData(0)
    val crownCount: LiveData<Int> = _crownCount

    private val _sapphireCount = MutableLiveData(0)
    val sapphireCount: LiveData<Int> = _sapphireCount

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    private var startTime = 0L
    private var bestCombo = 0
    private var redoUsed = false

    fun questionCount(): Int = _questions.value?.size ?: 0

    fun selectQuestionCount(count: Int) {
        _questionCount.value = count
    }

    fun refreshCrownCount() {
        repository.countRewards(RewardRules.CROWN) { count ->
            mainHandler.post { _crownCount.value = count }
        }
    }

    fun refreshSapphireCount() {
        repository.countRewards(RewardRules.SAPPHIRE) { count ->
            mainHandler.post { _sapphireCount.value = count }
        }
    }

    fun startRound() {
        begin(generator.generateMixed(_questionCount.value ?: 100), review = false)
    }

    fun startReviewRound() {
        repository.fetchWrong { all ->
            mainHandler.post {
                val picked = ReviewSelector.pick(all, 100)
                if (picked.isEmpty()) {
                    _message.value = "还没有错题，先做一轮练习吧"
                } else {
                    begin(picked.map { it.question }, review = true)
                }
            }
        }
    }

    fun startRoundRedo() {
        val wrongs = _wrongProblems.value.orEmpty()
        if (wrongs.isEmpty()) {
            _message.value = "没有需要重做的错题"
            return
        }
        begin(wrongs.shuffled().map { it.question }, review = true)
        redoUsed = true
        _canRedo.value = false
    }

    fun currentQuestion(): ArithmeticQuestion? =
        _questions.value?.getOrNull(_index.value ?: 0)

    fun onDigit(d: Int) {
        if (_phase.value != QuizPhase.QUIZ || _feedback.value != null) return
        val cur = _input.value.orEmpty()
        if (cur.length < 6) _input.value = cur + d
    }

    fun onBackspace() {
        if (_phase.value != QuizPhase.QUIZ || _feedback.value != null) return
        _input.value = _input.value.orEmpty().dropLast(1)
    }

    fun onSubmit() {
        if (_phase.value != QuizPhase.QUIZ || _feedback.value != null) return
        val question = currentQuestion() ?: return
        val typed = _input.value.orEmpty()
        if (typed.isEmpty()) return
        val isCorrect = typed.toIntOrNull() == question.answer
        if (isCorrect) {
            _score.value = (_score.value ?: 0) + 1
            _combo.value = (_combo.value ?: 0) + 1
            _energy.value = EnergyRules.afterGain(_energy.value ?: 0f)
            if ((_combo.value ?: 0) > bestCombo) bestCombo = _combo.value ?: 0
            if (_reviewRound.value == true) repository.deleteWrong(question)
        } else {
            _combo.value = 0
            _energy.value = 0f
            val wrong = WrongProblem(question, typed, System.currentTimeMillis())
            _wrongProblems.value = _wrongProblems.value.orEmpty() + wrong
            repository.recordWrong(question, typed)
        }
        _feedback.value = Feedback(question, isCorrect, typed)
        mainHandler.removeCallbacks(advanceCallback)
        if (isCorrect) {
            advance()
        } else {
            mainHandler.postDelayed(advanceCallback, 500L)
        }
    }

    private fun advance() {
        if (_phase.value != QuizPhase.QUIZ || _feedback.value == null) return
        val total = _questions.value?.size ?: return
        val idx = (_index.value ?: 0) + 1
        if (idx >= total) {
            stopTimer()
            _elapsedSeconds.value = (System.currentTimeMillis() - startTime) / 1000
            val review = _reviewRound.value ?: false
            val plannedCount = _questionCount.value ?: total
            val score = _score.value ?: 0
            val wrongCount = _wrongProblems.value.orEmpty().size
            val earnedCrown = RewardRules.awardsCrown(
                plannedCount = plannedCount,
                score = score,
                review = review
            )
            _crownEarned.value = earnedCrown
            if (earnedCrown) {
                repository.insertReward(RewardRules.CROWN)
                _message.value = "太棒了！奖励一枚皇冠 👑"
            }
            if (RewardRules.awardsSapphire(plannedCount, score, total, review) && redoUsed) {
                repository.insertReward(RewardRules.SAPPHIRE)
                _sapphireEarned.value = true
                _message.value = "太棒了！错题全部改正，奖励一颗蓝宝石 💎"
            }
            _canRedo.value = !review && redoUsed.not() &&
                plannedCount in RewardRules.SAPPHIRE_QUESTION_COUNTS && wrongCount > 0
            if (!review && total > 0) {
                val seconds = (_elapsedSeconds.value ?: 0L).toInt()
                repository.updateRecord(total, score, seconds, bestCombo) { improved ->
                    mainHandler.post { _newRecord.value = improved }
                }
            }
            _phase.value = QuizPhase.RESULT
        } else {
            _index.value = idx
            _input.value = ""
            _feedback.value = null
        }
    }

    fun backToSetup() {
        stopTimer()
        mainHandler.removeCallbacks(advanceCallback)
        _feedback.value = null
        _input.value = ""
        _newRecord.value = false
        _phase.value = QuizPhase.SETUP
    }

    fun consumeMessage() {
        _message.value = null
    }

    fun resultComment(): RoundComment {
        val total = (_questions.value?.size ?: (_questionCount.value ?: 100)).coerceAtLeast(1)
        return ResultComment.forRound(total, _score.value ?: 0)
    }

    private fun begin(list: List<ArithmeticQuestion>, review: Boolean) {
        mainHandler.removeCallbacks(advanceCallback)
        _questions.value = list
        _index.value = 0
        _input.value = ""
        _score.value = 0
        _feedback.value = null
        _wrongProblems.value = emptyList()
        _crownEarned.value = false
        _sapphireEarned.value = false
        _canRedo.value = false
        redoUsed = false
        _combo.value = 0
        bestCombo = 0
        _energy.value = 0f
        _newRecord.value = false
        _reviewRound.value = review
        startTime = System.currentTimeMillis()
        _elapsedSeconds.value = 0L
        mainHandler.removeCallbacks(timerCallback)
        mainHandler.postDelayed(timerCallback, 1000)
        _phase.value = QuizPhase.QUIZ
    }

    private fun stopTimer() {
        mainHandler.removeCallbacks(timerCallback)
    }
}