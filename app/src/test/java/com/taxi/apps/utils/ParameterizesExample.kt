package com.taxi.apps.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Arrays


@RunWith(value = Parameterized::class)
class ParameterizesExample(val input: String,val expectValue: Boolean) {
    @Test
    fun isPallindrome() {
        val unitTestHelper= UnitTestHelper()
        val result=unitTestHelper.isPallindrome(input)
        assertEquals(expectValue,result)
    }
    companion object{
        @JvmStatic
        @Parameterized.Parameters(name = "{index} : {0} is Pallindrome- {1}")
        fun data(): List<Array<Any>>{
        return listOf(
            arrayOf("hello",false),
            arrayOf("level",true),
            arrayOf("a",true),
            arrayOf("",true)
        )
        }
    }
}