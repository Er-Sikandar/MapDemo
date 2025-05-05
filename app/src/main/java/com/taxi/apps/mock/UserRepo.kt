package com.taxi.apps.mock

class UserRepo {
    val users=listOf<Users>(
        Users(1,"John","john@gmail.com","2asdfg1asdf"),
        Users(2,"Wien","wien@gmail.com","2asdfg1asdf"),
        Users(3,"Emily","emily@gmail.com","2asdfg1asdf"),
    )

    fun loginUserRepo(email: String,password: String): LoginStatus{
    val user=users.filter { users->users.email==email }
        return if (user.size==1){
              if (users[0].password==password){
                  LoginStatus.SUCCESS
              }else{
                  LoginStatus.INVALID_PASSWORD
              }
        }else{
            LoginStatus.INVALID_USER
        }
    }
}