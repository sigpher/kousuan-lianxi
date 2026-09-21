package com.example.myapplication.ui.arithmetic

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myapplication.R
import com.example.myapplication.arithmetic.AccountRepository
import com.example.myapplication.arithmetic.UserAccount
import com.example.myapplication.arithmetic.UserValidation
import com.example.myapplication.databinding.DialogAccountBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AccountBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountRepository: AccountRepository
    private var registerMode = false
    private var stagedAvatarPath: String? = null
    private var currentAccount: UserAccount? = null

    var onAccountChanged: (() -> Unit)? = null

    private val pickAvatar =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) copyAvatar(uri)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountRepository = AccountRepository(requireContext())

        binding.btnPickAvatar.setOnClickListener { pickAvatar.launch("image/*") }
        binding.btnChangeAvatar.setOnClickListener { pickAvatar.launch("image/*") }
        binding.textAccountSwitch.setOnClickListener {
            registerMode = !registerMode
            renderForm()
        }
        binding.btnAccountSubmit.setOnClickListener { submit() }
        binding.btnAccountLogout.setOnClickListener { logout() }
        binding.textAccountClose.setOnClickListener { dismiss() }

        refresh()
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun refresh() {
        accountRepository.currentUser { user ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                currentAccount = user
                if (user == null) {
                    binding.accountForm.visibility = View.VISIBLE
                    binding.accountLogged.visibility = View.GONE
                    binding.inputUsername.setText("")
                    binding.inputPassword.setText("")
                    binding.textAccountError.visibility = View.GONE
                    renderForm()
                } else {
                    binding.accountForm.visibility = View.GONE
                    binding.accountLogged.visibility = View.VISIBLE
                    binding.textLoggedName.text =
                        getString(R.string.account_logged_in_as, user.username)
                    AvatarUtil.load(binding.imgLoggedAvatar, user.avatarPath)
                }
            }
        }
    }

    private fun renderForm() {
        binding.avatarPickRow.visibility = if (registerMode) View.VISIBLE else View.GONE
        binding.btnAccountSubmit.setText(
            if (registerMode) R.string.account_register else R.string.account_login
        )
        binding.textAccountSwitch.setText(
            if (registerMode) R.string.account_to_login else R.string.account_to_register
        )
        binding.textAccountError.visibility = View.GONE
    }

    private fun submit() {
        val username = binding.inputUsername.text.toString().trim()
        val password = binding.inputPassword.text.toString()
        UserValidation.validateUsername(username)?.let {
            showError(it)
            return
        }
        UserValidation.validatePassword(password)?.let {
            showError(it)
            return
        }
        binding.btnAccountSubmit.isEnabled = false
        if (registerMode) {
            accountRepository.register(username, password, stagedAvatarPath) { ok, error ->
                onResult(ok, error, getString(R.string.account_register_success, username))
            }
        } else {
            accountRepository.login(username, password) { ok, error ->
                onResult(ok, error, getString(R.string.account_login_success, username))
            }
        }
    }

    private fun onResult(ok: Boolean, error: String?, successMessage: String) {
        activity?.runOnUiThread {
            if (_binding == null) return@runOnUiThread
            binding.btnAccountSubmit.isEnabled = true
            if (ok) {
                Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()
                onAccountChanged?.invoke()
                dismiss()
            } else {
                showError(error ?: "操作失败，请重试")
            }
        }
    }

    private fun logout() {
        accountRepository.logout()
        Toast.makeText(requireContext(), R.string.account_logout_success, Toast.LENGTH_SHORT).show()
        onAccountChanged?.invoke()
        refresh()
    }

    private fun copyAvatar(uri: Uri) {
        val staged = accountRepository.stagedAvatarFile()
        val copied = try {
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                staged.outputStream().use { output -> input.copyTo(output) }
            }
            true
        } catch (e: Exception) {
            staged.delete()
            false
        }
        if (!copied) {
            Toast.makeText(
                requireContext(),
                R.string.account_avatar_failed,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val user = currentAccount
        if (user != null) {
            accountRepository.changeAvatar(user.id, staged.path) {
                activity?.runOnUiThread {
                    if (_binding == null) return@runOnUiThread
                    onAccountChanged?.invoke()
                    refresh()
                }
            }
        } else {
            stagedAvatarPath = staged.path
            AvatarUtil.loadFile(binding.imgAccountAvatar, staged)
            Toast.makeText(
                requireContext(),
                R.string.account_avatar_picked,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showError(message: String) {
        binding.textAccountError.text = message
        binding.textAccountError.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}