# WALAFacade

**WALAFacade** is a [facade](http://en.wikipedia.org/wiki/Facade_pattern) for making [WALA](http://wala.sourceforge.net) easier to use from **Scala**.
It is mostly syntactic sugar over the WALA API to make its use more concise. It allows bypassing boilerplate code required because of Java's less flexible type system.

The implementation is working well but it is not, by far, comprehensive. Features have been added when needed. The discrete WALAFacade project appeared when we needed to share code between projects.
We intent to evolve it in the same demand-driven manner from now on. If you cannot find something you need, feel free to add it, or post an feature request issue.

## Features

- short aliases for commonly used types (e.g., `PutI` instead of `SSAPutInstruction`)

-   richer API for many WALA types (e.g., get all instructions `put`ing to a `LocalP`(`LocalPointerKey`): `P(cgNode, ssaValue).puts`). 
    Rich types are implemented as [Scala value classes](http://docs.scala-lang.org/sips/pending/value-classes.html) so the performance impact should be negligible

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

### Configuring the analysis

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
  entry {
   class = "my/test/MainClass"
   signature-pattern = ".*methodSignaturesMatchingThisRegexWillAlsoBeEntrypoints.*"
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
 
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import com.typesafe.config.ConfigFactory
import com.ibm.wala.util.graph.traverse.DFS
import scala.collection.JavaConversions._
import edu.illinois.wala.ipa.callgraph.propagation.P

object Test extends App {
  implicit val config = ConfigFactory.load() // loads the above config file
  val pa = FlexibleCallGraphBuilder() // does the pointer analysis

  import pa._ // make cg, heap, etc. available in scope

  val startNodes = cg filter { n: N => n.m.name == "foo" }
  val interestingFilter = { n: N => n.m.name matches ".*bar.*" }
  val reachableNodes = DFS.getReachableNodes(cg, startNodes, interestingFilter)
  val writtenPointers: Iterable[LocalP] = reachableNodes flatMap { n =>
    n.instructions collect { case i: PutI => P(n, i.v) }
  }
}
```

## Finding your way around

The code has little comments and documentation but much of it should be self-explenatory. 

### Conventions

- the package structure mirrors WALA's. E.g., classes wrapping/pertaining to `com.ibm.wala.ssa` are found in `edu.illinois.wala.ssa`

- many packages contain a trait called `Wrapper` which does most of the implicit magic. The `Wrapper`s are arranged in a tree structure, with each package's `Wrapper` extending all the `Wrapper`s of its subpackages.  

- the wrapper for the outermost package, i.e. `edu.illinois.wala`, is actually called `Facade` and imports all other `Wrapper`s

- all type aliases are in `edu.illinois.wala/TypeAliases`
