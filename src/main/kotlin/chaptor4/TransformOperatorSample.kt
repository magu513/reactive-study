package chaptor4

import io.reactivex.Flowable
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {
    map()
    flatmap()
    concatMap()
    concatMapEager()
}

fun map() {
    wrapper {
        Flowable
                .just("A", "B", "C", "D", "E")
                .map(String::toLowerCase)
                .subscribe(DebugSubscriber())
    }
}

fun flatmap() {
    wrapper {
        Flowable
                .just("A", "", "B", "", "C")
                .flatMap {
                    // 空文字を覗きかつ小文字へ変換
                    if (it == "") {
                        Flowable.empty()
                    } else {
                        Flowable.just(it.toLowerCase())
                    }
                }
                .subscribe(DebugSubscriber())
    }

    wrapper {
        Flowable
                .range(1, 3)
                .flatMap({
                    //  データを受け取ったら新しいFlowableを生成
                    Flowable.interval(100L, TimeUnit.MILLISECONDS)
                            // 三件まで
                            .take(3)
                },
                        // 元のデータと変換したデータを使って新たな通知をするデータを作成(最終的に通知されるデータ)
                        { sourceData, newData -> "[$sourceData] $newData" })
                .subscribe(DebugSubscriber())
        Thread.sleep(1000L)
    }

    wrapper {
        Flowable
                .just(1, 2, 0, 4, 5)
                // 0がくると例外を発生させる
                .map { 10 / it }
                .flatMap(
                        // 通常時のデータ
                        { Flowable.just(it) },
                        // エラー時のデータ
                        { Flowable.just(-1) },
                        // 完了時のデータ
                        { Flowable.just(100) })
                .subscribe(DebugSubscriber())
    }
}

fun concatMap() {
    wrapper {
        Flowable
                .range(10, 3)
                .concatMap { sourceData ->
                    // 通知するデータを保つFlowableを生成
                    Flowable
                            .interval(500L, TimeUnit.MILLISECONDS)
                            .take(2)
                            // 元の通知データと結果のデータを合わせて文字列に
                            .map { "${System.currentTimeMillis()}ms: [$sourceData] $it" }
                }
                .subscribe(DebugSubscriber())
        Thread.sleep(4000L)
    }
}

fun concatMapEager() {
    wrapper {
        Flowable
                .range(10, 3)
                // 受け取ったデータを元にFlowableを生成し実行するが、通知は順にする
                .concatMapEager { sourceData ->
                    Flowable
                            .interval(500L, TimeUnit.MILLISECONDS)
                            .take(2)
                            .map {
                                "${System.currentTimeMillis()}ms: [$sourceData] $it "
                            }
                }
                .subscribe(DebugSubscriber())
        Thread.sleep(4000L)
    }

    wrapper {
        Flowable
                .range(20, 3)
                .concatMapEagerDelayError({ sourceData ->
                    Flowable
                            .interval(500L, TimeUnit.MILLISECONDS)
                            .take(3)
                            .doOnNext { if (sourceData == 1 && it == 1L) throw Exception("例外発生") }
                            .map { "[$sourceData] $it" }
                }, true)
                .subscribe(DebugSubscriber())
        Thread.sleep(4000L)
    }
}