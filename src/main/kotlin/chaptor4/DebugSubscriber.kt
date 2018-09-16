package chaptor4

import io.reactivex.subscribers.DisposableSubscriber

/**
 * デバッグ用のSubscribe
 */
class DebugSubscriber<T>(): DisposableSubscriber<T>() {
    private var label: String? = null

    constructor(label: String) : this() {
        this.label = label
    }

    override fun onComplete() {
        val output: String = if (label == null) {
            " "
        } else {
            "$label:"
        }

        println("${ Thread.currentThread().name }: $output 完了")
    }

    override fun onNext(data: T) {
        val output: String = if (label == null) {
            " "
        } else {
            "$label:"
        }

        println("${ Thread.currentThread().name }: $output $data")
    }

    override fun onError(throwable: Throwable?) {
        val output: String = if (label == null) {
            " "
        } else {
            "$label:"
        }

        println("${ Thread.currentThread().name }: $output $throwable")
    }
}