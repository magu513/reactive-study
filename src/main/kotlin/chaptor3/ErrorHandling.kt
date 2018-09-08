package chaptor3

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

fun main(args: Array<String>) {
    retry()
    substituteData()
}

/**
 * リトライ処理のサンプル
 */
fun retry() {
    Flowable.create<Int>({
        println("Flowableの処理開始")

        (1..3).forEach { i ->
            if (i == 2) throw Exception("例外発生")

            it.onNext(i)
        }

        it.onComplete()
        println("Flowableの処理終了")
    },BackpressureStrategy.BUFFER)
            // subscribeが実行された際に実行する処理
            .doOnSubscribe { println("flowable: doOnSubscribe") }
            // エラーが発生したら2回まで再実行する
            .retry(2)
            .subscribe(object : Subscriber<Int> {
                override fun onComplete() {
                    println("完了")
                }

                override fun onSubscribe(s: Subscription?) {
                    println("subsccriber: onSubscribe")
                    s?.request(Long.MAX_VALUE)
                }

                override fun onNext(t: Int?) {
                    println("data: $t")
                }

                override fun onError(t: Throwable?) {
                    println("エラー=$t")
                }

            })
}

/**
 * エラー時に代替データを通知する場合
 */
fun substituteData() {
    Flowable
            .just(1, 3, 5, 0, 2, 4)
            .map { 100 / it }
            // エラーが発生したら0を通知する
            .onErrorReturnItem(0)
            .subscribe({ println("data=$it") }, { println("error=$it") }, { println("完了") })
}