package com.example.myapplication.ui.arithmetic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.arithmetic.RewardRules
import com.example.myapplication.databinding.FragmentAchievementsBinding

class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        val view = binding.root
        val viewModel = ViewModelProvider(this).get(AchievementsViewModel::class.java)
        val levelNames = resources.getStringArray(R.array.crown_level_names)
        viewModel.crownCount.observe(viewLifecycleOwner) { count ->
            binding.textCrownCount.text = getString(R.string.achievement_crown_count, count)
            binding.textAchievementEmpty.visibility =
                if (count == 0) View.VISIBLE else View.GONE
            val levelIndex = RewardRules.levelIndexForCrowns(count)
            binding.textLevel.text =
                getString(R.string.achievement_level, levelNames[levelIndex])
            val next = RewardRules.nextLevelCrowns(levelIndex)
            binding.textNext.text = if (next == null) {
                getString(R.string.achievement_max_level)
            } else {
                getString(
                    R.string.achievement_next_level,
                    next - count,
                    levelNames.getOrElse(levelIndex + 1) { "" }
                )
            }
        }
        viewModel.yellowFlowerCount.observe(viewLifecycleOwner) { count ->
            binding.textYellowFlowerCount.text =
                getString(R.string.achievement_yellow_flower_count, count)
        }
        viewModel.redHeartCount.observe(viewLifecycleOwner) { count ->
            binding.textRedHeartCount.text =
                getString(R.string.achievement_red_heart_count, count)
        }
        viewModel.sapphireCount.observe(viewLifecycleOwner) { count ->
            binding.textSapphireCount.text =
                getString(R.string.achievement_sapphire_count, count)
        }
        viewModel.records.observe(viewLifecycleOwner) { records ->
            binding.textRecordsHeader.visibility =
                if (records.isEmpty()) View.GONE else View.VISIBLE
            binding.textRecordsList.text = records.joinToString("\n") {
                getString(
                    R.string.achievement_record_line,
                    it.count,
                    it.bestScore,
                    formatTime(it.bestTimeSeconds.toLong()),
                    it.bestCombo
                )
            }
        }
        viewModel.loadRewardCounts()
        viewModel.loadRecords()
        return view
    }

    private fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}