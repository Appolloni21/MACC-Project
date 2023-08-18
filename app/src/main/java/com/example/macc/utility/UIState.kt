package com.example.macc.utility

object UIState {
    const val SUCCESS: String = "SUCCESS"
    const val FAILURE: String = "FAILURE"
    const val FAIL_101: String = "FAILURE: the user is already in this travel"
    const val FAIL_102: String = "FAILURE: user not found"
    const val FAIL_103: String = "FAILURE: wrong password"
    const val FAIL_104: String = "FAILURE: fail in checking if user already registered through google"
    const val WARN_101: String = "WARN: user with this email already exists and it is registered with google provider"
    const val WARN_102: String = "WARN: user doesn't exist in the Auth DB, it needs to be created"
    const val WARN_103: String = "WARN: user with this email already exists but it is registered with standard email-password method"
}