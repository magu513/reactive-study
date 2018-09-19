package chaptor4

import io.reactivex.Flowable
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("mm:ss.SSS")

fun main(args: Array<String>) {
    just()
    fromArray()
    fromIterable()
    fromCallable()
    range()
    interval()
    timer()
    defer()
    empty()
}

fun just() {
    wrapper {
        // justは引数の左から順に通知する
        Flowable
                // 引数は最大10まで受け付ける
                .just("A", "B", "C", "D", "E")
                .subscribe(DebugSubscriber())

    }
}

fun fromArray() {
    wrapper {
        // 配列/リストから要素を取得して通知する
        Flowable
                // fromArrayは可変長引数なので、引数を複数記述することができる
                // kotlinで配列を渡したい場合は *array で展開する必要がある
                .fromArray("A", "B", "C", "D", "E")
                // .fromArray(arrayOf("A", "B", "C", "D", "E"))
                .subscribe(DebugSubscriber())
    }
}

fun fromIterable() {
    wrapper {
        Flowable
                .fromIterable(listOf("A", "B", "C", "D", "E"))
                .subscribe(DebugSubscriber())
    }
}

fun fromCallable() {
    wrapper {
        // Callableの戻り値をデータとして通知する。
        // (-> Callableは通知するデータを生成する)
        Flowable
                .fromCallable { System.currentTimeMillis() }
                .subscribe(DebugSubscriber())
    }
}

fun range() {
    wrapper {
        // range はInt型のみ
        // rangeLong は関数名のごとくLong型を扱える
        Flowable
                // 10から順に3件まで通知する
                .range(10, 3)
                .subscribe(DebugSubscriber())
    }
}

fun interval() {
    wrapper {
        // intervalは指定した通知間隔ごとに0から順にデータを通知する
        val flowable = Flowable.interval(1000L, TimeUnit.MILLISECONDS)
        println("開始時間: ${now()}")

        flowable.subscribe { println("${Thread.currentThread().name}: ${now()}: data=$it") }
        Thread.sleep(5000L)
    }
}

fun timer() {
    wrapper {
        // timerは指定時間後に0Lを通知する
        println("開始時間: ${now()}")
        Flowable
                .timer(1000L, TimeUnit.MILLISECONDS)
                .subscribe({
                    println("${Thread.currentThread().name}: ${now()}: data=$it")
                }, {
                    println("エラー:$it")
                }, {
                    println("完了")
                })
        Thread.sleep(1500L)
    }
}

fun defer() {
    wrapper {
        // deferは購読されるたびに新しいFlowable/Observableを生成する
        // 呼び出されたタイミングでデータを生成する必要がある場合に使われる

        // この場合は型を明示しないと、２度目の呼び出し時にエラーが発生する
        val flowable: Flowable<LocalTime> = Flowable.defer { Flowable.just(LocalTime.now()) }
        flowable.subscribe(DebugSubscriber("No.1"))
        Thread.sleep(2000L)
        flowable.subscribe(DebugSubscriber("No.2"))
    }
}

fun empty() {
    wrapper {
        // 空のFlowable/Observableを生成する
        Flowable
                // ジェネリクス未指定だと怒られるので、とりあえずVoidを指定
                // 完了のみ通知する
                .empty<Void>()
                .subscribe(DebugSubscriber())
    }
}



fun wrapper(f: () -> Unit) {
    f()
    separate()
}

fun separate() {
    println("---------------------------")
}

fun now(): String = LocalTime.now().format(formatter)