package chaptor1

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subscribers.ResourceSubscriber
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
	// SubscribeはReactive Streamに対応
	// 挨拶の言葉を通知するFlowableの生成
	val flowable = Flowables.create<String>(BackpressureStrategy.BUFFER) {
		val datas = listOf("Hello, World", "こんにちは、世界!")

		for (data in datas) {
			// 購読が解除された場合は処理をやめる
			if (it.isCancelled) {
				return@create
			}
			// データ通知
			it.onNext(data)
		}

		// 完了通知
		it.onComplete()
	}

	flowable.observeOn(Schedulers.computation())
			.subscribe(object : Subscriber<String> {
				private lateinit var subscription: Subscription

				// 購読が開始された際の処理
				override fun onSubscribe(s: Subscription?) {
					this.subscription = s as Subscription
					this.subscription.request(1L)
				}

				// データを受け取った際の処理
				override fun onNext(data: String?) {
					val threadName = Thread.currentThread().name
					println("$threadName : $data")
					this.subscription.request(1L)
				}

				override fun onComplete() {
					val threadName = Thread.currentThread().name
					println("$threadName : 完了しました")
				}

				override fun onError(t: Throwable?) {
					t?.printStackTrace()
				}
			})
	Thread.sleep(500L)

	// 途中で購読を解除する
	Flowable.interval(100L, TimeUnit.MILLISECONDS)
			.subscribe(object : Subscriber<Long> {
				private lateinit var subscription: Subscription
				private var startTime: Long = 0L

				override fun onSubscribe(s: Subscription?) {
					this.subscription = s as Subscription
					this.startTime = System.currentTimeMillis()
					this.subscription.request(Long.MAX_VALUE)
				}

				override fun onNext(t: Long?) {
					// 購読解除後に500ミリ秒後に購読解除
					if ((System.currentTimeMillis() - startTime) > 500) {
						subscription.cancel()
						println("購読解除")
						return
					}

					println("data=$t")
				}

				override fun onComplete() {}
				override fun onError(t: Throwable?) {}

			})
	Thread.sleep(2000L)

	// onSubscribeメソッドの途中で処理が始まる例
	Flowable.range(1,3)
			.subscribe(object : Subscriber<Int> {
				override fun onComplete() {
					println("完了")
				}

				override fun onSubscribe(s: Subscription?) {
					println("onSubscribe start")
					s?.request(Long.MAX_VALUE)
					println("onSubscribe: end")
				}

				override fun onNext(t: Int?) {
					println(t)
				}

				override fun onError(t: Throwable?) {
					println("エラー=$t")
				}
			})

	Flowable
		.just(1,2,3)
			.subscribe(object: Subscriber<Int> {
				override fun onSubscribe(s: Subscription?) {
					println("onSubscribe START")
					// request実行すると通知処理が開始される
					s?.request(Long.MAX_VALUE)
					// 本来は後続の処理がない状態でないでrequestを実行する
					// → 期待するタイミングで後続処理が実行される可能性があるため
					println("onSubscribe END")
				}

				override fun onNext(t: Int?) {
					println(t)
				}

				override fun onComplete() {
					println("完了")
				}
				override fun onError(t: Throwable?) {
					println("エラー: $t")
				}
			})
	useDisposableAndUnSubscribe()
	useSubscribeWith()
	useCompositeDisposable()
}

/**
 * Disposableを使って購読を解除するサンプル
 */
fun useDisposableAndUnSubscribe() {
	val disposable = Flowable.just(1,2,3).subscribe { println("data=$it")}
	// 購読を解除する
	disposable.dispose()
}

/**
 * subscribeWithメソッドを使ってDisposableを取得する例
 */
fun useSubscribeWith() {
	// subscribeWithはSubscriber/Observerを引数に取る
	// subscribeWithは内部で引数に渡したsubscribeを実行し、引数をそのまま戻り値として返す。
	// ResourceSubscriberはDisposableを実装しているため、Disposableとして受け取ることができる
	val disposable = Flowable.range(1,3).subscribeWith(object: ResourceSubscriber<Int>() {
		override fun onComplete() {}

		override fun onNext(t: Int?) { println("data=$t")}

		override fun onError(t: Throwable?) {}

	})
}

/**
 * CompositeDisposableを使ったサンプル
 */
fun useCompositeDisposable() {
	// CompositeDisposableは複数のDisposableを同時に操作することができる
	val compositeDisposable = CompositeDisposable()
	compositeDisposable.add(Flowable.range(1, 3)
			// 購読解除された際の処理
			.doOnCancel { println("No.1 canceled") }
			.observeOn(Schedulers.computation())
			.subscribe {
				Thread.sleep(100L)
				println("No.1 : $it")
			})

	compositeDisposable.add(Flowable.range(5, 10)
			.doOnCancel { println("No.2 canceled") }
			// 計算用のThreadで処理を実行する
			.observeOn(Schedulers.computation())
			.subscribe {
				Thread.sleep(100L)
				println("No.2 : $it")
			})

	Thread.sleep(150L)
	compositeDisposable.dispose()
}
