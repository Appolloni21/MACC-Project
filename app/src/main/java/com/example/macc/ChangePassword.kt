package com.example.macc

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.macc.databinding.ChangePasswordPageBinding
import com.example.macc.utility.UIState
import com.example.macc.viewmodel.AuthViewModel

class ChangePassword : Fragment() {

    private var _binding: ChangePasswordPageBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = ChangePasswordPageBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Toolbar with nav component
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val toolbar: Toolbar = binding.toolbar.toolbar

        toolbar.setupWithNavController(navController, appBarConfiguration)

        val updateButton: Button = binding.cpUpdatePassBtn
        updateButton.setOnClickListener {
            val currentPassword: String = binding.cpCurrentPassword.text.toString().trim { it <= ' ' }
            val newPassword: String = binding.cpNewPassword.text.toString().trim { it <= ' ' }
            val repeatPassword: String = binding.cpRepeatPassword.text.toString().trim { it <= ' ' }

            when {
                TextUtils.isEmpty(currentPassword) -> {
                    Toast.makeText(context, "Please enter current password", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(newPassword) -> {
                    Toast.makeText(context, "Please enter new password", Toast.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(repeatPassword) -> {
                    Toast.makeText(context, "Please repeat new password", Toast.LENGTH_SHORT).show()
                }
                TextUtils.getTrimmedLength(newPassword) < 6 -> {
                    Toast.makeText(context, "Password must contain at least 6 characters", Toast.LENGTH_SHORT).show()
                }
                !TextUtils.equals(newPassword,repeatPassword) -> {
                    Toast.makeText(context, "New password and the repeated one are not equals", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    sharedViewModel.changePasswordUser(currentPassword,newPassword)
                }
            }
        }

        sharedViewModel.uiState.observe(viewLifecycleOwner){
            when(it){
                UIState.SUCCESS -> {
                    Toast.makeText(context, "Password updated!", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                    navController.navigateUp()
                }
                UIState.FAILURE -> {
                    Toast.makeText(context, "Error in updating the password", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
                UIState.FAIL_103 ->{
                    Toast.makeText(context, "The current password is wrong, please re-enter it", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
                UIState.WARN_101 ->{
                    Toast.makeText(context, "You can not change password because you are signed in with Google", Toast.LENGTH_SHORT).show()
                    sharedViewModel.resetUiState()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}