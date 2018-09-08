package chaptor3

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    causeMissingBackpressureException()
}

fun causeMissingBackpressureException() {
    // Exceptionを回避する方法としては、BackpressureStrategy(onBackspressureBufferなど)を指定する
    Flowable
            .interval(10L, TimeUnit.MILLISECONDS)
            // doOnNext実行前に処理する
            // データがバッファリングされる場合も含む
            .doOnNext { println("emit: $it") }
            .observeOn(Schedulers.computation())
            .subscribe(object : Subscriber<Long>{
                override fun onComplete() {
                    println("完了")
                }

                override fun onSubscribe(s: Subscription?) {
                    s?.request(Long.MAX_VALUE)
                }

                override fun onNext(t: Long?) {
                    try {
                        println("waiting.......")
                        Thread.sleep(1000L)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    println("received: $t")
                }

                override fun onError(t: Throwable?) {
                    println("エラー:$t")
                }
            })
    Thread.sleep(5000L)
}