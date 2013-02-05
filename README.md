# WALAFacade

**WALAFacade** is a [facade](http://en.wikipedia.org/wiki/Facade_pattern) for making [WALA](http://wala.sourceforge.net) easier to use from **Scala**.
It is mostly syntactic sugar over the WALA API to make its use more concise. It allows bypassing boilerplate code required because of Java's less flexible type system.

The implementation is working well but it is not, by far, comprehensive.

## Features

- short aliases for commonly used types (e.g., `PutI` instead of `SSAPutInstruction`)

-   richer API for many WALA types (e.g., get all instructions `put`ing to a `LocalP`(`LocalPointerKey`): `P(cgNode, ssaValue).puts`). Rich types implemented as [Scala value classes](http://docs.scala-lang.org/sips/pending/value-classes.html) so the performance impact should be negligible

- alternative `CallGraphBuilder` and related classes for fast bootstrapping (integrated with [typesafe/config](https://github.com/typesafehub/config))

- implicit two-way conversion between WALA types and rich types or Scala alternatives (e.g., can use a `Function1[T, Boolean]` instead of a `Predicate[T]`)

- uses [implicit parameters](http://www.scala-lang.org/node/114) to *piggyback* outer information (e.g., can find out the `IField`(i.e., `F`) for a `PutI` by `putI.f` while having an implicit `IClassHierarchy` in scope)

- wraps primitives in value classes for better type safety (e.g., `i.uses` returns an `Iterable[V]` where `V` is a values class that wraps an `Int` that has SSA value number semantics)

Design principles:
- keep the facade API close to the original API. A fair amount of thought has been put into the WALA API so there is no point in moving away from it when the gain is not significant. 

- favor usability, but keep performance in mind

## Getting started

Since the library is young you will likely want to get the code so you can modify it easily instead of using it as an external dependency.

### Using SBT

See 
 - http://www.scala-sbt.org/release/docs/Getting-Started/Multi-Project.html
 - http://stackoverflow.com/questions/7550376/how-can-sbt-pull-dependency-artifacts-from-git

Also, for an example of how to make WALA compile with SBT, see https://github.com/cos/wala/blob/master/com.ibm.wala.core/build.sbt

### Using eclipse

Simply import the project into an Eclipse workspace that also has wala.shrike, wala.core, and wala.util. 

You will also need to fix the names for the dependent projects. The Eclipse project is generated automatically by sbt-eclipse and currently sbt-eclipse doesn't allow periods (i.e., `.`) in project names. 
Thus, for example, the project is dependent on the project `walaCore` instead of `com.ibm.wala.core`. You will have to adjust the project build path to use the appropriate names.

## Basic usage

### Initial setup

This guide provides a good introduction to setting up WALA: http://wala.sourceforge.net/wiki/index.php/UserGuide:Getting_Started

Alternatively, WALAFacade allows you to use a [typesafe/config](https://github.com/typesafehub/config) file instead of wala.properties. Steps:

1. Create a file called `application.conf` (or [alternatives](https://github.com/typesafehub/config#standard-behavior)) and put it on the project's classpath. 
2. Load an implicit config in your scope `Config conf = ConfigFactory.load()`
3. `val pa = FlexibleCallGraphBuilder()` - the pointer analysis will run on instantiation
4. Use the results. E.g., `pa.cg` is the call graph, `pa.heap` is the heap graph

The analysis can be customized by overriding the following default methods of the `FlexibleCallGraph` class:
```scala
def policy = { import ZeroXInstanceKeys._;  ALLOCATIONS }
def cs = new ContextInsensitiveSelector()
def contextInterpreter: RTAContextInterpreter
def contextInterpreter = new DefaultSSAInterpreter(...)
def instanceKeys = new ZeroXInstanceKeys(...)
```

### Example

A basic config file - needs to be in your classpath. No other configuration necessary.
```
wala {
  jre-lib-path = "/Library/Java/JavaVirtualMachines/jdk1.7.0_10.jdk/Contents/Home/jre/lib/rt.jar"
  dependencies.binary += "myProject/bin"
  wala.entry {
   class = "my/test/MainClass"
  }
}
```

And a program that finds, in all methods matching `bar.*` and reachable from methods named `foo`, all written field pointers (i.e. `LocalPointerKey`s):

```scala
// remember that 
// type N = CGNode // call graph nodes
// type PutI = SSAPutInstruction
// type LocalP = LocalPointerKey
  
import edu.illinois.wala.Facade._ // convenience object that activates all implicit converters
 
object Example extends App {
  implicit config = ConfigFactory.load() // loads the above config file
  val pa = FlexibleCallGraphBuilder() // does the pointer analysis
  
  import pa._ // make cg, heap, etc. available in scope
  
  val startNodes = cg filter { _.m.name == "foo" }
  val interestingFilter = { n: N => n.m.name matches ".*bar.*" }
  val reachableNodes = DFS.getReachableNodes(cg, startNodes, interestingFilter)
  val writtenPointers: Iterable[LocalP]  = reachableNodes flatMap { n =>
    n.instructions collect { case i: PutI => P(n, i.v) }
  }
}
```
