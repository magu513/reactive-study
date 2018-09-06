package chaptor3

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import other.Counter
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
	useFlatMap()
	concatMap()
	useConcatMapEager()
	twoThreadUpdateObject()
	twoThreadUpdateObjectMerge()
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

/**
 * 2つのスレッドから同じオブジェクトの更新を行う
 */
fun twoThreadUpdateObject() {
	val counter = Counter()
	Flowable
			.range(1, 10000)
			.subscribeOn(Schedulers.computation())
			.subscribe({ counter.increment() }, { println("エラー=$it") }, { println("counter.get()=${counter.count}") })
	Flowable
			.range(1, 10000)
			// Flowableを異なるスレッド場で処理を行うようにする
			.subscribeOn(Schedulers.computation())
			// 異なるスレッド上で処理を行うようにする
			.observeOn(Schedulers.computation())
			.subscribe({ counter.increment() }, { println("エラー=$it") }, { println("counter.get()=${counter.count}") })

	Thread.sleep(1000L)
	// シーケンシャルに処理が行われないため、出力値は20000にならない
}

/**
 * mergeを使って結合した場合
 */
fun twoThreadUpdateObjectMerge() {
	val counter = Counter()
	val source1 = Flowable
			.range(1, 10000)
			.subscribeOn(Schedulers.computation())
			.observeOn(Schedulers.computation())
	val source2 = Flowable
			.range(1, 10000)
			// Flowableを異なるスレッド場で処理を行うようにする
			.subscribeOn(Schedulers.computation())
			// 異なるスレッド上で処理を行うようにする
			.observeOn(Schedulers.computation())

	// 2つのFlowableをマージする
	Flowable
			.merge(source1, source2)
			.subscribe({ counter.increment() }, { println("エラー=$it") }, { println("counter.get()=${counter.count}") })
	// シーケンシャルに処理が行われるため20000が出力される
	// 共有のインスタンスを操作する場合は、複数のFlowable/Observalbeを結合しから終端処理を行う必要がある
	Thread.sleep(1000L)
}