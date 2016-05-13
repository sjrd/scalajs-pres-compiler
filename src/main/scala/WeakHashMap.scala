package java.util

class WeakHashMap[K, V] extends AbstractMap[K, V] with Map[K, V] {



	private class WeakEntry
	def get = null
	def entrySet = null
}

object WeakHashMap {
	private val defaultInitialCapacity = 16
	private val defaultLoadFactor = 0.75
}