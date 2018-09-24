package chaptor4

import io.reactivex.observers.DisposableSingleObserver

class DebugSingleObserver<T>(): DisposableSingleObserver<T>() {
    private var label: String? = null

    constructor(label: String) : this() {
        this.label = label
    }

    override fun onSuccess(data: T) {
        val output: String = if (label == null) {
            " "
        } else {
            "$label:"
        }

        println("${ Thread.currentThread().name }: $output $data")
    }

    override fun onError(throwable: Throwable) {
        val output: String = if (label == null) {
            " "
        } else {
            "$label:"
        }

        println("${ Thread.currentThread().name }: $output $throwable")    }
}