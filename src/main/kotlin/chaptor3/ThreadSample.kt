package chaptor3

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.ResourceSubscriber
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
	mainThread()
	otherThread()
	choiceScheduler()
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

/**
 * 有効になるスケジューラ
 */
fun choiceScheduler() {
	Flowable.range(1, 5)
			.subscribeOn(Schedulers.computation()) // 一番最初に書かれたsubscribeOnが有効になる
			.subscribeOn(Schedulers.io()) // I/O処理を行う際に使う。ThreadPoolから取得され、必要なら新規にThreadを生成
			.subscribeOn(Schedulers.single()) // 単一のThreadで処理する際に使われる
			.subscribe { println("${Thread.currentThread().name} : $it") }
	Thread.sleep(500)
}

