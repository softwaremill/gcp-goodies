package services

import javax.inject.Inject
import scala.concurrent.Future
import scala.collection.mutable.Set

/*

We will create instances of this class faster than the GC call finalize which will cause
a nice memory leak.

 */
class Foo() {
  val anEmptySet: Set[Int] = Set()
  def bar(ints: Traversable[Int]): Unit = {}
  override def finalize() {
    bar(anEmptySet)
    super.finalize()
  }
}


trait ProfilerService {
  def executeMemoryIntensiveOperation(items: Long): Unit
}

class ProfilerServiceImpl @Inject()() extends ProfilerService {

  def executeMemoryIntensiveOperation(items: Long): Unit = {
    for (i <- 0L to items) {
      val f = new Foo()
    }
  }
}