package chaptor1

import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

// 面倒なので同一プロジェクトに複数Mainを作って実行する
fun main(args: Array<String>) {
	val observable = Observable.create<String> {
		val datas = listOf("Hello, World", "こんにちは、世界")

		for (d in datas) {
			if (it.isDisposed) return@create
			it.onNext(d)
		}

		it.onComplete()
	}

	observable
		.observeOn(Schedulers.computation())
		.subscribeBy(onNext = {
			val threadName = Thread.currentThread().name
			println("$threadName : $it")
		}, onComplete = {
			println("${Thread.currentThread().name} : 完了しました")
		}, onError = {
			it.printStackTrace()
		})

	Thread.sleep(500L)
}