package chaptor3

import io.reactivex.Flowable
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
	useFlatMap()
	concatMap()
	useConcatMapEager()
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

/**
 * concatMapメソッド内で異なるスレッド場で動くFlowableを生成した場合
 */
fun concatMap() {
	// ConcatMapは受け取ったデータを、受け取った順番に実行する
	Flowable.just("D", "E", "F")
			.concatMap { Flowable.just(it).delay(1000L, TimeUnit.MILLISECONDS) }
			.subscribe {
				val time = LocalTime.now().format(DateTimeFormatter.ofPattern("ss:SSS"))
				println("${Thread.currentThread().name}: data=$it, time=$time")
			}
	Thread.sleep(4000L)
}

/**
 * concatMapEagerメソッド内で異なるスレッド上で動くFlowableを生成した場合
 */
fun useConcatMapEager() {
	// concatMapEagerは処理は同時に実行されるが、結果は元のデータの順にバッファされる
	Flowable.just("G", "H", "I")
			.concatMapEager { Flowable.just(it).delay(1000L, TimeUnit.MILLISECONDS) }
			.subscribe {
				val time = LocalTime.now().format(DateTimeFormatter.ofPattern("ss:SSS"))
				// 結果は同一のThreadで出力される
				println("${Thread.currentThread().name}: data=$it, time=$time")
			}
	Thread.sleep(2000L)
}