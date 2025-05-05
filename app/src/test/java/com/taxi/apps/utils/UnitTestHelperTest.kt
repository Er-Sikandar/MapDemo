package com.taxi.apps.utils

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UnitTestHelperTest {
    lateinit var unitTestHelper: UnitTestHelper

    @Before
    fun setUp(){
        println("Before Every Test cases.")
        unitTestHelper= UnitTestHelper()
    }
    @After
    fun tearDown(){
        println("After Every Test cases.")
    }
    @Test
    fun isPallindrome() {
        //Act
        val result=unitTestHelper.isPallindrome("hello")
        //Assert
        assertEquals(false,result)
    }
    @Test
    fun isPallindrome_Input_level() {
        //Act
        val result=unitTestHelper.isPallindrome("level")
        //Assert
        assertEquals(true,result)
    }

}