package chaptor1

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import sun.rmi.runtime.Log
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
	// SubscribeはReactive Streamに対応
	// 挨拶の言葉を通知するFlowableの生成
	val flowable = Flowables.create<String>(BackpressureStrategy.BUFFER) { emmiter ->
		val datas = listOf("Hello, World", "こんにちは、世界!")

		for (data in datas) {
			// 購読が解除された場合は処理をやめる
			if (emmiter.isCancelled) {
				return@create
			}
			// データ通知
			emmiter.onNext(data)
		}

		// 完了通知
		emmiter.onComplete()
	}

	flowable.observeOn(Schedulers.computation())
			.subscribe(object : Subscriber<String> {
				private lateinit var subscription: Subscription

				override fun onSubscribe(s: Subscription?) {
					this.subscription = s as Subscription
					this.subscription.request(1L)
				}

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
			.subscribe(object: Subscriber<Long> {
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
				override fun onComplete() { }
				override fun onError(t: Throwable?) { }

			})
	Thread.sleep(2000L)
}