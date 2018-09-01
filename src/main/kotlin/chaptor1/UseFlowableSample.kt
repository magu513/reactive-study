package chaptor1

import io.reactivex.BackpressureStrategy
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Flowables
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

fun main(args: Array<String>) {
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
}