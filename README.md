## About

**WALAFacade** is a [facade](http://en.wikipedia.org/wiki/Facade_pattern) for making [WALA](http://wala.sourceforge.net) easier to use from **Scala**.
It is mostly syntactic sugar over the WALA API to make its use more concise. It allows bypassing boilerplate code required because of Java's less flexible type system.

The implementation is working well but it is not, by far, comprehensive.

Features:
- short aliases for commonly used types (e.g., `PutI` instead of `SSAPutInstruction`)

-   richer API for many WALA types (e.g., get all instructions `put`ing to a `LocalP`(`LocalPointerKey`): `P(cgNode, ssaValue).puts`). Rich types implemented as [Scala value classes](http://docs.scala-lang.org/sips/pending/value-classes.html) so the performance impact should be negligible

- alternative `CallGraphBuilder` and related classes for fast bootstrapping (integrated with [typesafe/config](https://github.com/typesafehub/config))

- implicit two-way conversion between WALA types and rich types or Scala alternatives (e.g., can use a `Function1[T, Boolean]` instead of a `Predicate[T]`)

- uses [implicit parameters](http://www.scala-lang.org/node/114) to *piggyback* outer information (e.g., can find out the `IField`(i.e., `F`) for a `PutI` by `putI.f` while having an implicit `IClassHierarchy` in scope)

- wraps primitives in value classes for better type safety (e.g., `i.uses` returns an `Iterable[V]` where `V` is a values class that wraps an `Int` that has SSA value number semantics)

Design principles:
- keep the facade API close to the original API. A fair amount of thought has been put into the WALA API so there is no point in moving away from it when the gain is not significant. 

- favor usability, but think about the performance impact at each step.

