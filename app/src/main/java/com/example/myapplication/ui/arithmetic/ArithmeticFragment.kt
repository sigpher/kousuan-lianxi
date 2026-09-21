package com.example.myapplication.ui.arithmetic

import android.content.res.ColorStateList
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentArithmeticBinding
import kotlin.random.Random

class ArithmeticFragment : Fragment() {

    private var _binding: FragmentArithmeticBinding? = null
    private val binding get() = _binding!!
    private var defaultCountTextColors: ColorStateList? = null
    private var toneGenerator: ToneGenerator? = null
    private lateinit var currentViewModel: ArithmeticViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArithmeticBinding.inflate(inflater, container, false)
        val view = binding.root
        currentViewModel = ViewModelProvider(this).get(ArithmeticViewModel::class.java)
        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        setupSetup(currentViewModel)
        setupQuiz(currentViewModel)
        setupResult(currentViewModel)
        observe(currentViewModel)
        return view
    }

    private fun setupSetup(viewModel: ArithmeticViewModel) {
        binding.btnStart.setOnClickListener { viewModel.startRound() }
        binding.btnReview.setOnClickListener { viewModel.startReviewRound() }
        binding.btnOpenReview.setOnClickListener {
            requireView().findNavController().navigate(R.id.nav_review)
        }
        binding.btnOpenAchievements.setOnClickListener {
            requireView().findNavController().navigate(R.id.nav_achievements)
        }

        val countButtons = listOf(
            binding.count10 to 10,
            binding.count20 to 20,
            binding.count50 to 50,
            binding.count100 to 100
        )
        countButtons.forEach { (button, count) ->
            button.setOnClickListener { viewModel.selectQuestionCount(count) }
        }
        defaultCountTextColors = binding.count10.textColors
        val welcomes = resources.getStringArray(R.array.setup_welcome)
        binding.textSetupWelcome.text = welcomes[Random.nextInt(welcomes.size)]
    }

    private fun setupQuiz(viewModel: ArithmeticViewModel) {
        val digitButtons = listOf(
            binding.key1 to 1,
            binding.key2 to 2,
            binding.key3 to 3,
            binding.key4 to 4,
            binding.key5 to 5,
            binding.key6 to 6,
            binding.key7 to 7,
            binding.key8 to 8,
            binding.key9 to 9,
            binding.key0 to 0
        )
        digitButtons.forEach { (button, digit) ->
            button.setOnClickListener { viewModel.onDigit(digit) }
        }
        binding.keyBackspace.setOnClickListener { viewModel.onBackspace() }
        binding.keyOk.setOnClickListener { viewModel.onSubmit() }
        binding.btnExit.setOnClickListener { viewModel.backToSetup() }
    }

    private fun setupResult(viewModel: ArithmeticViewModel) {
        binding.btnRetry.setOnClickListener { viewModel.startRound() }
        binding.btnRedoWrong.setOnClickListener { viewModel.startReviewRound() }
        binding.btnBackSetup.setOnClickListener { viewModel.backToSetup() }
    }

    private fun observe(viewModel: ArithmeticViewModel) {
        viewModel.phase.observe(viewLifecycleOwner) { phase ->
            binding.phaseSetup.visibility =
                if (phase == QuizPhase.SETUP) View.VISIBLE else View.GONE
            binding.phaseQuiz.visibility =
                if (phase == QuizPhase.QUIZ) View.VISIBLE else View.GONE
            binding.phaseResult.visibility =
                if (phase == QuizPhase.RESULT) View.VISIBLE else View.GONE
            if (phase == QuizPhase.SETUP) viewModel.refreshCrownCount()
            if (phase == QuizPhase.RESULT) renderResult(viewModel)
        }

        viewModel.index.observe(viewLifecycleOwner) { index ->
            binding.textQuestion.text = viewModel.currentQuestion()?.text.orEmpty()
            binding.textProgress.text = "第 ${index + 1} 题"
        }

        viewModel.input.observe(viewLifecycleOwner) { value ->
            binding.textInput.text = value.ifEmpty { getString(R.string.input_hint) }
        }

        viewModel.feedback.observe(viewLifecycleOwner) { feedback ->
            val isShown = feedback != null
            binding.textFeedback.visibility = if (isShown) View.VISIBLE else View.GONE
            if (feedback != null) {
                val color = ContextCompat.getColor(
                    requireContext(),
                    if (feedback.isCorrect) R.color.color_correct else R.color.color_wrong
                )
                binding.textFeedback.setTextColor(color)
                binding.textFeedback.text = if (feedback.isCorrect) {
                    getString(R.string.feedback_correct)
                } else {
                    getString(R.string.feedback_wrong, feedback.question.answer)
                }
                if (feedback.isCorrect) {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 180)
                    vibrateShort()
                    bounceQuestion()
                    animateCorrect()
                } else {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 300)
                    vibrateWrong()
                    shakeInput()
                }
            }
        }

        viewModel.combo.observe(viewLifecycleOwner) { combo ->
            if (combo >= 2) {
                binding.textCombo.visibility = View.VISIBLE
                binding.textCombo.text = getString(R.string.combo_label, combo)
            } else {
                binding.textCombo.visibility = View.GONE
            }
        }

        viewModel.elapsedSeconds.observe(viewLifecycleOwner) { seconds ->
            binding.textQuizTimer.text = getString(R.string.quiz_timer, formatTime(seconds))
        }

        viewModel.crownCount.observe(viewLifecycleOwner) { count ->
            binding.textSetupCrown.text = getString(R.string.setup_crown_chip, count)
        }

        viewModel.questionCount.observe(viewLifecycleOwner) { selected ->
            val buttons = listOf(
                binding.count10 to 10,
                binding.count20 to 20,
                binding.count50 to 50,
                binding.count100 to 100
            )
            val purple = ContextCompat.getColor(requireContext(), R.color.purple_500)
            val white = ContextCompat.getColor(requireContext(), R.color.white)
            buttons.forEach { (button, count) ->
                val activated = count == selected
                button.isActivated = activated
                button.alpha = if (activated) 1f else 0.6f
                button.backgroundTintList =
                    if (activated) ColorStateList.valueOf(purple) else null
                button.setTextColor(
                    if (activated) ColorStateList.valueOf(white) else defaultCountTextColors
                )
            }
        }

        viewModel.reviewRound.observe(viewLifecycleOwner) { review ->
            binding.quizTitle.text = getString(
                if (review) R.string.quiz_review else R.string.quiz_practice
            )
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.consumeMessage()
            }
        }
    }

    private fun renderResult(viewModel: ArithmeticViewModel) {
        val total = viewModel.questionCount()
        val earnedCrown = viewModel.crownEarned.value == true
        binding.textResultCrown.visibility = if (earnedCrown) View.VISIBLE else View.GONE
        binding.textResultScore.text =
            getString(R.string.result_score, viewModel.score.value ?: 0, total)
        val seconds = viewModel.elapsedSeconds.value ?: 0L
        binding.textResultTime.text = getString(R.string.result_time, formatTime(seconds))
        val avg = if (total <= 0) 0.0 else seconds.toDouble() / total
        binding.textResultAvg.text =
            getString(R.string.result_avg, "%.1f".format(avg))
        val comment = viewModel.resultComment()
        binding.textResultTitle.text = getString(R.string.result_comment_line, comment.title)
        binding.textResultComment.text = "★★★★★".take(comment.stars) + " " + comment.comment
        binding.textResultRecord.visibility =
            if (viewModel.newRecord.value == true) View.VISIBLE else View.GONE
        val wrong = viewModel.wrongProblems.value.orEmpty()
        binding.textResultWrongHeader.visibility =
            if (wrong.isEmpty()) View.GONE else View.VISIBLE
        binding.textResultWrong.text = if (wrong.isEmpty()) {
            getString(R.string.result_wrong_empty)
        } else {
            wrong.joinToString("\n") {
                getString(
                    R.string.result_wrong_line,
                    it.question.text,
                    it.question.answer,
                    it.userAnswer
                )
            }
        }
    }

    private fun bounceQuestion() {
        ObjectAnimator.ofPropertyValuesHolder(
            binding.textQuestion,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 0.85f, 1.15f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.85f, 1.15f, 1f)
        ).apply { duration = 350 }.start()
    }

    private fun animateCorrect() {
        ObjectAnimator.ofPropertyValuesHolder(
            binding.textCombo,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1.3f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.3f, 1f)
        ).apply { duration = 250 }.start()
    }

    private fun shakeInput() {
        ObjectAnimator.ofFloat(
            binding.textInput,
            View.TRANSLATION_X,
            0f, -18f, 18f, -12f, 12f, -6f, 6f, 0f
        ).apply { duration = 450 }.start()
    }

    private fun vibrator(): Vibrator? {
        val context = requireContext()
        return if (Build.VERSION.SDK_INT >= 31) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Vibrator::class.java)
            v
        }
    }

    private fun vibrateShort() {
        val v = vibrator() ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(40)
        }
    }

    private fun vibrateWrong() {
        val combo = currentViewModel.combo.value ?: 0
        val v = vibrator() ?: return
        if (!v.hasVibrator()) return
        val pattern = if (combo >= 3) longArrayOf(0, 120, 80, 200) else longArrayOf(0, 80)
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(pattern, -1)
        }
    }

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toneGenerator?.release()
        toneGenerator = null
        _binding = null
    }
}