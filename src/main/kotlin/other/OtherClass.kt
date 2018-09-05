package other

class Counter {
	@Volatile
	var count: Int = 0
		private set

	fun increment() {
		count++
	}
}