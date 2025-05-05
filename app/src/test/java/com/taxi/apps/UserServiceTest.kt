package com.taxi.apps

//import com.google.android.gms.common.internal.Asserts
import com.taxi.apps.mock.LoginStatus
import com.taxi.apps.mock.UserRepo
import com.taxi.apps.mock.UserService
import org.junit.Assert
import org.junit.Before
import org.junit.Test
/*import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations*/

class UserServiceTest {
   // @Mock
    lateinit var userRepo: UserRepo


    @Before
    fun setUp(){
      //  MockitoAnnotations.openMocks(this)
      //  Mockito.`when`(userRepo.loginUserRepo(anyString(),anyString())).thenReturn(LoginStatus.INVALID_PASSWORD)
    }

    @Test
    fun testUserService(){
    val userService= UserService(userRepo)
      val status= userService.loginUser("asd@gmail.com","wehgvjdxmbvjd")
        Assert.assertEquals(LoginStatus.INVALID_PASSWORD,status)
    }
}