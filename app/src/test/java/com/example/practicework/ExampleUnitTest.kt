package com.example.practicework

import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testCoroutinesPractice() = runBlocking {
        // Run coroutines practice functions to verify execution
        question1()
        question15()
        extra1()
        assertTrue(true)
    }
}