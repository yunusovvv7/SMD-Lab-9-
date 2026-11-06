package com.example.practicework

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

// Practice work: Coroutines Introduction Solutions (Thoroughly Verified & Corrected)
// Kotlin/JVM, kotlinx-coroutines-core 1.10.2

suspend fun main() {
    println("=== Q1: First coroutine ===")
    question1()

    println("\n=== Q2: Three baristas ===")
    question2()

    println("\n=== Q3: Chain of suspend functions ===")
    question3()

    println("\n=== Q4: Calling suspend from normal code ===")
    question4()

    println("\n=== Q5: Suspend is not a thread ===")
    question5()

    println("\n=== Q6: Ten downloads, one thread ===")
    question6()

    println("\n=== Q7: Order of launch ===")
    question7()

    println("\n=== Q8: Parent waits for child ===")
    question8()

    println("\n=== Q9: Screen closed, requests cancelled ===")
    question9()

    println("\n=== Q10: Profile ready ===")
    question10()

    println("\n=== Q11: Reusable counter ===")
    question11()

    println("\n=== Q12: Returns at once or waits ===")
    question12()

    println("\n=== Q13: Five builders ===")
    question13()

    println("\n=== Q14: Job or Deferred ===")
    question14()

    println("\n=== Q15: Three exports in a scope ===")
    question15()

    println("\n=== Extra 1: Delay or sleep: measure it ===")
    extra1()

    println("\n=== Extra 2: Three levels of coroutines ===")
    extra2()

    println("\n=== Extra 3: No scope, no launch ===")
    extra3()

    println("\n=== Extra 4: What the compiler sees ===")
    extra4()

    println("\n=== Extra 5: Greet everybody ===")
    extra5()
}

// Question 1
suspend fun question1() {
    runBlocking {
        launch {
            delay(1000L)
            println("Coffee ready")
        }
        println("Order accepted")
    }
}

// Question 2
suspend fun question2() {
    println("--- With delay ---")
    runBlocking {
        launch {
            println("espresso start")
            delay(300L)
            println("espresso end")
        }
        launch {
            println("tea start")
            delay(100L)
            println("tea end")
        }
        launch {
            println("latte start")
            delay(200L)
            println("latte end")
        }
    }

    println("--- With Thread.sleep ---")
    runBlocking {
        launch {
            println("espresso start")
            Thread.sleep(300L)
            println("espresso end")
        }
        launch {
            println("tea start")
            Thread.sleep(100L)
            println("tea end")
        }
        launch {
            println("latte start")
            Thread.sleep(200L)
            println("latte end")
        }
    }
}

// Question 3
suspend fun step1() {
    delay(100L)
    println("Step 1: Load user")
    step2()
}

suspend fun step2() {
    delay(100L)
    println("Step 2: Check password")
    step3()
}

suspend fun step3() {
    delay(100L)
    println("Step 3: Open profile")
}

suspend fun question3() {
    // Called from suspend main
    step1()
}

// Question 4
suspend fun loadUserSuspend(): String {
    delay(100L)
    return "User"
}

// Normal function that would fail if calling suspend directly:
fun normalFunctionDemo() {
    // loadUserSuspend() // Compile error: Suspend function 'loadUserSuspend' should be called only from a coroutine or another suspend function
}

suspend fun question4() {
    println("Compile error message: Suspend function 'loadUserSuspend' should be called only from a coroutine or another suspend function")
    // Fix 1: runBlocking (blocks current thread)
    runBlocking {
        println("Fix 1 (runBlocking): ${loadUserSuspend()}")
    }
    // Fix 2: GlobalScope.launch (or CoroutineScope.launch)
    val job = GlobalScope.launch {
        println("Fix 2 (launch): ${loadUserSuspend()}")
    }
    job.join()
}

// Question 5
suspend fun getThreadName(): String {
    delay(10L)
    return Thread.currentThread().name
}

suspend fun question5() {
    runBlocking {
        val before = Thread.currentThread().name
        println("Before call thread: $before")
        val res = getThreadName()
        println("Returned thread: $res")
    }
}

// Question 6
suspend fun question6() {
    runBlocking {
        repeat(10) { i ->
            launch {
                println("download $i started on ${Thread.currentThread().name}")
                delay(500L)
                println("download $i finished")
            }
        }
        delay(600L)
    }
}

// Question 7
suspend fun question7() {
    runBlocking {
        println("1")
        launch {
            println("2")
            delay(100L)
            println("3")
        }
        println("4")
        delay(50L)
        println("5")
    }
}

// Question 8
suspend fun question8() {
    runBlocking {
        val parent = launch {
            launch {
                delay(200L)
                println("child done")
            }
            println("parent body done")
        }
        parent.join()
        println("report completed")
    }
}

// Question 9
suspend fun question9() {
    runBlocking {
        val parent = launch {
            launch {
                try {
                    delay(1000L)
                } finally {
                    println("cleanup 1")
                }
            }
            launch {
                try {
                    delay(1000L)
                } finally {
                    println("cleanup 2")
                }
            }
        }
        delay(100L)
        parent.cancelAndJoin()
        println("end")
    }
}

// Question 10
suspend fun question10() {
    runBlocking {
        coroutineScope {
            launch {
                delay(200L)
                println("Avatar loaded")
            }
            launch {
                delay(100L)
                println("Settings loaded")
            }
        }
        println("Profile ready")

        launch {
            delay(50L)
            println("after scope coroutine")
        }
        println("end of main")
    }
}

// Question 11
fun CoroutineScope.startCounter(name: String, limit: Int): Job = launch {
    repeat(limit) { i ->
        println("Counter $name: ${i + 1}")
        delay(100L)
    }
}

suspend fun question11() {
    runBlocking {
        coroutineScope {
            println("launched")
            val job1 = startCounter("A", 3)
            val job2 = startCounter("B", 2)
            job1.join()
            job2.join()
        }
    }
}

// Question 12
fun CoroutineScope.startBackgroundGreeter(name: String): Job = launch {
    delay(200L)
    println("Background hello, $name")
}

suspend fun greetAll(names: List<String>) = coroutineScope {
    for (name in names) {
        launch {
            delay(100L)
            println("Hello, $name")
        }
    }
    println("All greetings initiated")
}

suspend fun question12() {
    runBlocking {
        startBackgroundGreeter("Alice")
        println("After startBackgroundGreeter call")

        greetAll(listOf("Bob", "Charlie"))
        println("After greetAll call")
    }
}

// Question 13
suspend fun question13() {
    runBlocking {
        val l = launch { println("launch job") }
        l.join()

        val a = async {
            println("async deferred")
            42
        }
        val deferredRes = a.await()

        val rbRes = runBlocking {
            println("runBlocking block")
            "runBlocking result"
        }

        val csRes = coroutineScope {
            println("coroutineScope block")
            "coroutineScope result"
        }

        val wcRes = withContext(Dispatchers.Default) {
            println("withContext block")
            "withContext result"
        }

        println("Results: launch=$l, async=$deferredRes, runBlocking=$rbRes, coroutineScope=$csRes, withContext=$wcRes")
    }
}

// Question 14
suspend fun question14() {
    runBlocking {
        // launch version returns Job, cannot return result
        val launchJob = launch {
            // 6 * 7
        }
        // launchJob.await() // does not compile

        val deferred = async {
            delay(100L)
            6 * 7
        }
        println("Result from async await(): ${deferred.await()}")
    }
}

// Question 15
suspend fun question15() {
    runBlocking {
        println("Start")
        coroutineScope {
            launch {
                delay(300L)
                println("X")
            }
            launch {
                delay(100L)
                println("Y")
            }
            launch {
                delay(200L)
                println("Z")
            }
        }
        println("All done")
    }
}

// Extra 1
suspend fun extra1() {
    runBlocking {
        val timeDelay = measureTimeMillis {
            coroutineScope {
                launch { delay(300L) }
                launch { delay(300L) }
                launch { delay(300L) }
            }
        }

        val timeSleep = measureTimeMillis {
            coroutineScope {
                launch { Thread.sleep(300L) }
                launch { Thread.sleep(300L) }
                launch { Thread.sleep(300L) }
            }
        }

        println("Delay time: ${timeDelay}ms (rounded: ${(timeDelay + 50) / 100 * 100}ms)")
        println("Sleep time: ${timeSleep}ms (rounded: ${(timeSleep + 50) / 100 * 100}ms)")
    }
}

// Extra 2
suspend fun extra2() {
    runBlocking {
        coroutineScope {
            launch {
                launch {
                    delay(200L)
                    println("grandchild done")
                }
                println("child 1 done")
            }
            launch {
                delay(100L)
                println("child 2 done")
            }
        }
        println("scope done")
    }
}

// Extra 3
suspend fun extra3() {
    // launch without scope in plain function: Unresolved reference: launch
    runBlocking {
        println("runBlocking builder used with current thread: ${Thread.currentThread().name}")
    }
}

// Extra 4
suspend fun loadUserById(id: Int): String {
    delay(100L)
    return "User_$id"
}

suspend fun extra4() {
    runBlocking {
        val user = loadUserById(1)
        println("Loaded: $user (Bytecode signature: final Object loadUserById(int id, Continuation continuation))")
    }
}

// Extra 5
suspend fun greetEverybody(names: List<String>) = coroutineScope {
    for ((index, name) in names.withIndex()) {
        val i = index + 1
        launch {
            delay((i * 100).toLong())
            println("Hello, $name")
        }
    }
}

suspend fun extra5() {
    runBlocking {
        println("before")
        greetEverybody(listOf("Ada", "Linus", "Grace"))
        println("after")
    }
}
