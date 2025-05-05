package com.taxi.apps.mock

data class Users(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
)

enum class LoginStatus{
    INVALID_USER,
    INVALID_PASSWORD,
    UNKNOWN_ERROR,
    SUCCESS
}
