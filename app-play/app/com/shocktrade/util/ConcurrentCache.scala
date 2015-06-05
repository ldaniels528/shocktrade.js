package com.shocktrade.util

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.Duration
import scala.language.implicitConversions

/**
 * Concurrent Cache
 * @author lawrence.daniels@gmail.com
 */
class ConcurrentCache[K, V](defaultTTL: Duration) {
  private val cache = TrieMap[K, Wrapper]()

  def contains(key: K): Boolean = cache.contains(key)

  def evict(key: K): Option[V] = cache.remove(key) flatMap (_.get)

  def get(key: K): Option[V] = cache.get(key) flatMap (_.get)

  def getOrElseUpdate(key: K, op: => V): Option[V] = cache.getOrElseUpdate(key, Wrapper(op, defaultTTL.toMillis)).get

  def put(key: K, value: V, ttl: Duration = defaultTTL) = cache(key) = Wrapper(value, ttl.toMillis)

  def update(key: K, value: V) = put(key, value)

  case class Wrapper(value: V, ttl: Long) {
    private val cachedTime = System.currentTimeMillis()

    def get: Option[V] = if (!isExpired) Some(value) else None

    def isExpired = System.currentTimeMillis() - cachedTime > ttl

  }

}

/**
 * Concurrent Cache Singleton
 * @author lawrence.daniels@gmail.com
 */
object ConcurrentCache {

  def apply[K, V](ttl: Duration) = new ConcurrentCache[K, V](ttl)

}
