package edu.illinois.wala.util

import com.ibm.wala.util.Predicate
import com.ibm.wala.util.intset.IntSetAction
import com.ibm.wala.util.intset.IntSet
import com.ibm.wala.util.intset.SparseIntSet
import com.ibm.wala.util.collections.Filter
import collection.JavaConverters._

trait Wrapper {
  implicit def makePredicateFromFunction[T](f: Function1[T, Boolean]) = new Predicate[T] {
    def test(t: T) = f(t)
  }

  // is actually deprecated
  implicit def makeFilterFromFunction[T](f: Function1[T, Boolean]) = new Filter[T] {
    def accepts(t: T) = f(t)
  }

  implicit def makeIntSetActionFromFunction(f: Function1[Int, Unit]) = new IntSetAction {
    def act(t: Int) = f(t)
  }

  implicit def intsetSet(s: IntSet) = new WrappedIntSet(s)

  import com.ibm.wala.util.graph.Graph
  implicit class RichGraph[T](g: Graph[T]) {
    import com.ibm.wala.util.graph._
    import impl.GraphInverter
    import traverse.DFS

    def dfs(startNode: T): Stream[T] = {
      DFS.iterateDiscoverTime(g, startNode).asInstanceOf[java.util.Iterator[T]].asScala.toStream
    }
    def invert: Graph[T] = GraphInverter.invert(g)
  }
}

class WrappedIntSet(s: IntSet) extends Set[Int] {
  def contains(key: Int) = s.contains(key)
  def iterator: Iterator[Int] = {
    val it = s.intIterator()
    new Iterator[Int] {
      def hasNext = it.hasNext()
      def next = it.next()
    }
  }
  def +(elem: Int) = new WrappedIntSet(s.union(SparseIntSet.singleton(elem)))
  def -(elem: Int) = throw new Exception("unsupported, implement this if you need it")

  def |(other: IntSet) = s.union(other)
  def &(other: IntSet) = s.intersection(other)
  def intersects(other: IntSet) = s.containsAny(other)

  override def foreach[U](f: Int => U) = {
    s.foreach(new IntSetAction() {
      override def act(x: Int) = f(x)
    })
  }
}