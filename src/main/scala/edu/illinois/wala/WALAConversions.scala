package edu.illinois.wala
import scala.collection.JavaConversions._
import scala.collection._
import edu.illinois.wala._
import edu.illinois.wala.TypeAliases

class Facade extends TypeAliases
  with edu.illinois.wala.util.Wrapper
  with ipa.slicer.Wrapper
  with classLoader.Wrapper
  with types.Wrapper
  with ssa.Wrapper
  with ipa.callgraph.Wrapper
  with Wrapper {

  // Union Types - see http://www.chuusai.com/2011/06/09/scala-union-types-curry-howard/ 
  type NOT[A] = A => Nothing
  type INNEROR[T, U] = NOT[NOT[T] with NOT[U]]
  type NOTNOT[A] = NOT[NOT[A]]
  type OR[T, U] = { type LAMBDA[X] = NOTNOT[X] <:< (T INNEROR U) }

  val mainMethod = "main([Ljava/lang/String;)V";
}

object Facade extends Facade