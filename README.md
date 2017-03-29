# WALAFacade

**WALAFacade** is a [facade](http://en.wikipedia.org/wiki/Facade_pattern) for making [WALA](http://wala.sourceforge.net) easier to use from **Scala**.
It is mostly syntactic sugar over the WALA API to make its use more concise. It allows bypassing boilerplate code required because of Java's less flexible type system.

The implementation is working well but it is not, by far, comprehensive. Features have been added when needed. The discrete WALAFacade project appeared when we needed to share code between projects.
We intent to evolve it in the same demand-driven manner from now on. If you cannot find something you need, feel free to add it, or post an feature request issue.

## Features

- short aliases for commonly used types (e.g., `PutI` instead of `SSAPutInstruction`)

- richer API for many WALA types (e.g., get all instructions `put`ing to a `LocalP`(`LocalPointerKey`): `P(cgNode, ssaValue).puts`). 
    Rich types are implemented as [Scala value classes](http://docs.scala-lang.org/sips/pending/value-classes.html) so the performance impact should be negligible

- alternative `CallGraphBuilder` and related classes for fast bootstrapping (integrated with [typesafe/config](https://github.com/typesafehub/config))

- implicit two-way conversion between WALA types and rich types or Scala alternatives (e.g., can use a `Function1[T, Boolean]` instead of a `Predicate[T]`)

- uses [implicit parameters](http://www.scala-lang.org/node/114) to *piggyback* outer information (e.g., can find out the `IField`(i.e., `F`) for a `PutI` by `putI.f` while having an implicit `IClassHierarchy` in scope)

- wraps primitives in value classes for better type safety (e.g., `i.uses` returns an `Iterable[V]` where `V` is a values class that wraps an `Int` that has SSA value number semantics)

Design principles:
- keep the facade API close to the original API. A fair amount of thought has been put into the WALA API so there is no point in moving away from it when the gain is not significant. 

- favor usability, but keep performance in mind

## Getting started

If you use sbt, the easiest way to get the library is from maven central:
```
libraryDependencies += "edu.illinois.wala" %% "walafacade" % "0.2"
```

Still, since the library is young you will likely want to get the code so you can modify it easily instead of using it as an external dependency.

There are also ready-made example projects you can simply clone:
 - for using WALAFacade from Java: https://github.com/cos/WALAFacade-java-start
 - for using WALAFacade from Scala: (soon)

### Steps

##### Clone WALA (optional)

This step is not required anymore as WALA is available from Maven Central now. Still, if you would like to also hack on the WALA source code, clone WALA and install it to your local Maven repo:

 - `export JAVA_HOME="<java's home on your system>"` (on OS X: `/usr/libexec/java_home`) Also, you might want to add this line to your system loading profile
 - `git clone https://github.com/wala/WALA.git` (or use my fork `https://github.com/cos/wala/tree/for-iterace` instead. It makes dispatch more precise. See the discussion about cilib results in the ISSTA paper)
 - `cd WALA`
 - `mvn clean install -DskipTests=true` 
    
##### Clone WALAFacade 
 - `git clone https://github.com/cos/WALAFacade.git`
 - `cd WALAFacade`
 - `sbt publishLocal`

##### Generate an Eclipse project or import into IntelliJ

To generate an Eclipse project:

1. Generate an .project file by running `sbt eclipse` in the WALAFacade directory.
2. Import the project into an Eclipse. WALA dependencies will be linked from the Ivy repository.

Alternatively, import into IntelliJ as an sbt project.

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
  jre-lib-path = "/Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home/jre/lib/rt.jar"
  dependencies.binary += "target/scala-2.11/classes"
  dependencies.jar += "andthis.jar"
  exclusions += ".*BesidesDefaultsIgnoreAlsoThisPattern.*"
  entry {
    signature-pattern = ".*Foo.*main.*"
  }
}
```

And a program that finds in all the code reachable from methods named `bar` all written fields and the names of the variables written to each.

```scala
// remember that 
// type N = CGNode // call graph nodes
// type PutI = SSAPutInstruction
// type LocalP = LocalPointerKey

import com.ibm.wala.ipa.callgraph.impl.ContextInsensitiveSelector
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFAContextSelector
import com.ibm.wala.util.graph.traverse.DFS

import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import edu.illinois.wala.ipa.callgraph.propagation.P

import com.typesafe.config.ConfigFactory

// convenience object that activates all implicit converters
import edu.illinois.wala.Facade._

import scala.collection.JavaConversions._

object Test extends App {
  implicit val config = ConfigFactory.load()

  // creates a new pointer analysis with a special context selector
  // implicitly uses the above config file
  val pa = new FlexibleCallGraphBuilder() {
    override def cs = new nCFAContextSelector(2, new ContextInsensitiveSelector());
  }

  // make cg, heap, etc. available in scope

  import pa._

  // more verbose to each understanding
  val startNodes = cg filter { n: N => n.m.name == "bar" }
  val reachableNodes = DFS.getReachableNodes(cg, startNodes)
  val foo = reachableNodes flatMap { n =>
    n.instructions collect {
      case i: PutI =>
        val p: LocalP = P(n, i.v)
        val variableNames: Iterable[String] = p.variableNames()
        val fieldName: F = i.f.get
        (fieldName, variableNames)
    }
  }

  // and a 3-liner doing exactly the name thing
  DFS
    .getReachableNodes(cg, cg filter { _.m.name == "bar" })
    .flatMap { n => n.instructions collect { case i: PutI => (i.f.get, P(n, i.v).variableNames()) } }
}
```

## Finding your way around

The code has little comments and documentation but much of it should be self-explenatory. 

### Conventions

- the package structure mirrors WALA's. E.g., classes wrapping/pertaining to `com.ibm.wala.ssa` are found in `edu.illinois.wala.ssa`

- many packages contain a trait called `Wrapper` which does most of the implicit magic. The `Wrapper`s are arranged in a tree structure, with each package's `Wrapper` extending all the `Wrapper`s of its subpackages.  

- the wrapper for the outermost package, i.e. `edu.illinois.wala`, is actually called `Facade` and imports all other `Wrapper`s

- all type aliases are in `edu.illinois.wala/TypeAliases`

## Related projects

[cuplv/walautil](https://github.com/cuplv/walautil) is a collection of WALA utility functions for Scala projects you may want to check out.
