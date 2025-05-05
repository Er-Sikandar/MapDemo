package com.taxi.apps.mock

class UserService(private val userRepo: UserRepo) {
    fun loginUser(email: String,password: String): String{
        val status=userRepo.loginUserRepo(email,password)
        return when(status){
            LoginStatus.INVALID_USER ->"User does not exist."
            LoginStatus.INVALID_PASSWORD -> "Password is invalid"
            LoginStatus.UNKNOWN_ERROR -> "Unknown error occurred"
            LoginStatus.SUCCESS -> "Logged in successfully"
        }
    }
}