package com.example.myapplication.ui.arithmetic

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.arithmetic.AccountRepository
import com.example.myapplication.audio.MusicMode
import com.example.myapplication.audio.MusicPlayer
import com.example.myapplication.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var accountRepository: AccountRepository
    private val modeDefaultColors = mutableMapOf<Int, ColorStateList>()

    private val pickMusicDir = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val ctx = _binding?.root?.context
            if (ctx != null) {
                MusicPlayer.setMusicDirectory(ctx, uri) { ok ->
                    if (_binding == null) return@setMusicDirectory
                    if (!ok) {
                        Toast.makeText(ctx, R.string.music_dir_empty, Toast.LENGTH_SHORT).show()
                    }
                    refreshMusicSource()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountRepository = AccountRepository(requireContext())

        binding.switchMusic.isChecked = MusicPlayer.isEnabled(requireContext())
        binding.switchMusic.setOnCheckedChangeListener { _, checked ->
            MusicPlayer.setEnabled(requireContext(), checked)
        }

        setupMusicControls()
        setupAccount()
        refreshAccount()
    }

    private fun setupMusicControls() {
        val context = requireContext()
        val modeMap = listOf(
            binding.btnModeSingle to MusicMode.SINGLE_LOOP,
            binding.btnModeList to MusicMode.LIST_LOOP,
            binding.btnModeShuffle to MusicMode.SHUFFLE
        )
        modeMap.forEach { (button, _) ->
            modeDefaultColors[button.id] = button.textColors
        }
        modeMap.forEach { (button, mode) ->
            button.setOnClickListener {
                MusicPlayer.setMode(context, mode)
                applyModeHighlight(modeMap, mode)
            }
        }
        applyModeHighlight(modeMap, MusicPlayer.getMode(context))

        binding.btnPickMusicDir.setOnClickListener {
            pickMusicDir.launch(null)
        }
        binding.btnResetMusicDir.setOnClickListener {
            MusicPlayer.clearMusicDirectory(_binding?.root?.context ?: context)
            refreshMusicSource()
        }
        refreshMusicSource()
    }

    private fun applyModeHighlight(
        modeMap: List<Pair<Button, MusicMode>>,
        selected: MusicMode
    ) {
        val purple = ContextCompat.getColor(requireContext(), R.color.purple_500)
        val white = ContextCompat.getColor(requireContext(), R.color.white)
        modeMap.forEach { (button, mode) ->
            val active = mode == selected
            button.isActivated = active
            button.alpha = if (active) 1f else 0.6f
            button.backgroundTintList =
                if (active) ColorStateList.valueOf(purple) else null
            button.setTextColor(
                if (active) ColorStateList.valueOf(white) else modeDefaultColors[button.id]
            )
        }
    }

    private fun refreshMusicSource() {
        if (_binding == null) return
        val context = _binding!!.root.context
        val dirUri = MusicPlayer.musicDirectoryUri(context)
        if (dirUri == null) {
            binding.textMusicSource.text =
                getString(R.string.music_source_builtin, MusicPlayer.builtinTrackCount())
            binding.btnResetMusicDir.visibility = View.GONE
        } else {
            val name = runCatching { Uri.parse(dirUri) }
                .getOrNull()
                ?.lastPathSegment
                ?.takeIf { it.isNotBlank() }
                ?: dirUri
            binding.textMusicSource.text = getString(R.string.music_source_custom, name)
            binding.btnResetMusicDir.visibility = View.VISIBLE
        }
    }

    private fun setupAccount() {
        binding.btnSettingsAccount.setOnClickListener {
            val sheet = AccountBottomSheet()
            sheet.onAccountChanged = {
                refreshAccount()
            }
            sheet.show(parentFragmentManager, "account_sheet")
        }
    }

    private fun refreshAccount() {
        accountRepository.currentUser { user ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                if (user == null) {
                    binding.textSettingsUser.setText(R.string.account_guest)
                    binding.textSettingsUserHint.visibility = View.VISIBLE
                    binding.textSettingsUserHint.setText(R.string.account_guest_hint)
                    AvatarUtil.loadDefault(binding.imgSettingsAvatar, _binding!!.root.context)
                } else {
                    binding.textSettingsUser.text = user.username
                    binding.textSettingsUserHint.visibility = View.GONE
                    AvatarUtil.load(binding.imgSettingsAvatar, user.avatarPath)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}