package chaptor3

import io.reactivex.Flowable
import io.reactivex.subscribers.ResourceSubscriber
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
	mainThread()
	otherThread()
}

/**
 * 処理をメインスレッド上で行う関数
 */
fun mainThread() {
	Flowable.just(1, 2, 3)
			.subscribe(object: ResourceSubscriber<Int>() {
				override fun onComplete() {
					println("${Thread.currentThread().name}: 完了")
				}

				override fun onNext(t: Int?) {
					println("${Thread.currentThread().name}: $t")
				}

				override fun onError(t: Throwable?) {
					t?.printStackTrace()
				}

			})
}

/**
 * 処理をメインスレッド以外で行う関数
 */
fun otherThread() {
	println("start")
	Flowable.interval(300L, TimeUnit.MILLISECONDS)
			.subscribe(object: ResourceSubscriber<Long>() {
				override fun onComplete() {
					println("${Thread.currentThread().name}: 完了")
				}

				override fun onNext(t: Long?) {
					println("${Thread.currentThread().name}: $t")
				}

				override fun onError(t: Throwable?) {
					t?.printStackTrace()
				}

			})
	println("end")
	Thread.sleep(1000L)
}

