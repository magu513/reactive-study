package chaptor3

import io.reactivex.Flowable
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
	useFlatMap()
}

/**
 * flatMapメソッド内で異なるスレッド場で動くFlowableを生成した場合のサンプル
 */
fun useFlatMap() {
	// FlatMapは通知されたデータを同時に実行する
	val flowable = Flowable.just("A", "B", "C")
			// 受け取ったデータからFlowableを生成、通知する
			.flatMap {
				// 1000ms後にデータを通知するFlowableを生成
				Flowable.just(it).delay(1000L, TimeUnit.MILLISECONDS)
			}

	flowable.subscribe { println("${Thread.currentThread().name} : $it") }
	Thread.sleep(2000L)
}