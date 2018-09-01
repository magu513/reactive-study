package chaptor2

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

fun main(args: Array<String>) {
	multiThreadAccessThreadUnSafe()
	useImmutableVariableChangeableState()
	multiThreadAccessThreadSafe()

	multiThreadAccessThreadUnSafeForPoint()
	multiThreadAccessThreadSafeForPoint()
}

/**
 * 2つのスレッドからアクセスし処理を行う場合
 * incrementはスレッドセーフでない
 */
fun multiThreadAccessThreadUnSafe() {
	val counter = Counter()

	val runnable = Runnable {
		for (i in 0 until 10000) {
			counter.increment()
		}
	}

	val executorService = Executors.newCachedThreadPool()
	// 新しいスレッドで開始
	val future1 = executorService.submit(runnable, true)
	val future2 = executorService.submit(runnable, true)

	if (future1.get() && future2.get()) {
		println(counter.count)
	} else {
		println("失敗しました")
	}

	// 非同期タスクをシャットダウンする
	executorService.shutdown()
}

/**
 * Immutableな変数宣言をしても状態の変更が可能な例
 */
fun useImmutableVariableChangeableState() {
	val instance = ReferenceTypeObject()
	assert(instance.value == "A")
	instance.value = "B"
	assert(instance.value == "B")
}

/**
 * 2つのスレッドからcounterにアクセスする
 * スレッドセーフ(counterが持つ変数が一つのため)
 */
fun multiThreadAccessThreadSafe() {
	val counter = AtomicCounter()

	val runnable = Runnable {
		for (i in 0 until 10000) {
			counter.increment()
		}
	}

	val executorService = Executors.newCachedThreadPool()
	// 新しいスレッドで開始
	val future1 = executorService.submit(runnable, true)
	val future2 = executorService.submit(runnable, true)

	if (future1.get() && future2.get()) {
		println(counter.get())
	} else {
		println("失敗しました")
	}

	// 非同期タスクをシャットダウンする
	executorService.shutdown()
}

/**
 * 2つのスレッドからpointにアクセスする
 * スレッドセーフではない
 *
 * 再現性は低いが期待しない値が帰ってくる
 */
fun multiThreadAccessThreadUnSafeForPoint() {
	val point = Point()
	val runnable = Runnable {
		for (i in 0 until 10000) {
			point.rightUp()
		}
	}

	val executorService = Executors.newCachedThreadPool()
	// 新しいスレッドで開始
	val future1 = executorService.submit(runnable, true)
	val future2 = executorService.submit(runnable, true)
	val future3 = executorService.submit(runnable, true)
	val future4 = executorService.submit(runnable, true)
	// 結果が帰ってくるまで待つ
	if (future1.get() && future2.get() && future3.get() && future4.get()) {
		// 期待する値は 20000 だがカウント処理が同期されていないため、実行するたび結果が変わる
		println("x: ${point.getX()}, y: ${point.getY()}")
	} else {
		println("失敗しました")
	}

	// 非同期タスクをシャットダウンする
	executorService.shutdown()
}

/**
 * 2つのスレッドからpointにアクセスする
 * スレッドセーフではない
 *
 * 再現性は低いが期待しない値が帰ってくる
 */
fun multiThreadAccessThreadSafeForPoint() {
	val point = SynchronizedPoint()
	val runnable = Runnable {
		for (i in 0 until 10000) {
			point.rightUp()
		}
	}

	val executorService = Executors.newCachedThreadPool()
	// 新しいスレッドで開始
	val future1 = executorService.submit(runnable, true)
	val future2 = executorService.submit(runnable, true)
	val future3 = executorService.submit(runnable, true)
	val future4 = executorService.submit(runnable, true)
	// 結果が帰ってくるまで待つ
	if (future1.get() && future2.get() && future3.get() && future4.get()) {
		// 期待する値は 20000 だがカウント処理が同期されていないため、実行するたび結果が変わる
		println("x: ${point.x}, y: ${point.y}")
	} else {
		println("失敗しました")
	}

	// 非同期タスクをシャットダウンする
	executorService.shutdown()
}


class Counter {
	@Volatile
	var count: Int = 0
		private set

	fun increment() {
		count++
	}
}

/**
 * Mutableなクラス
 */
data class ReferenceTypeObject(var value: String = "A")

/**
 * スレッドセーフなCountクラス
 */
class AtomicCounter {
	private val count = AtomicInteger(0)

	fun get(): Int = count.get()
	fun increment() {
		count.incrementAndGet()
	}
}

/*
 * 値が複数ある場合に複数のスレッドからアクセスされると、アトミック性が担保できない
 * (アトミック性 -> 一連の処理の途中に外部からアクセスできないこと)
 */
class Point {
	private val x = AtomicInteger(0)
	private val y = AtomicInteger(0)

	fun rightUp() {
		x.incrementAndGet()
		y.incrementAndGet()
	}

	fun getX() = x.get()
	fun getY() = y.get()
}

class SynchronizedPoint {
	var x = 0
		@Synchronized get
	var y = 0
		@Synchronized get

	@Synchronized fun rightUp() {
			x++
			y++

	}
}