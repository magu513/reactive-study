package chaptor1

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.time.DayOfWeek
import java.time.LocalDate

fun main(args: Array<String>) {
	useSingle()
	useMaybe()
	useCompletable()
}

fun useSingle() {
	// Singleはデータを1件のみ通知する
	val single = Single.create<DayOfWeek> {
		// onNext,onCompleteではなくonSuccessを実行する
		// 通知するデータを指定
		it.onSuccess(LocalDate.now().dayOfWeek)
	}

	single.observeOn(Schedulers.newThread()).subscribeBy(onSuccess = ::println,
			onError = { println("エラー=$it") })
}

fun useMaybe() {
	// データを1件だけ通知するか、1件も通知せず完了する
	val maybe: Maybe<DayOfWeek> = Maybe.create {
		// 通知するデータを設定する
		it.onSuccess(LocalDate.now().dayOfWeek)
	}
	maybe.subscribeBy(onSuccess = ::println,
			onComplete = { println("完了") },
			onError = { println("エラー=$it") })
}

fun useCompletable() {
	// Completableはデータを通知することなく完了する
	// これはCompletable内で何らかの副作用が発生する処理を行う
	val completable = Completable.create {
		it.onComplete()
	}

	completable
			// Completableを非同期で実行する
			.subscribeOn(Schedulers.computation())
			.subscribeBy(onComplete = { println("完了") },
					onError = { println("エラー=$it") })
	Thread.sleep(100L)
}