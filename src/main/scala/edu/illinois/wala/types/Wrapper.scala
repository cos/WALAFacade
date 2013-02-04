package edu.illinois.wala.types

import com.ibm.wala.types.TypeReference
import edu.illinois.wala.PrettyPrintable

trait Wrapper {
  implicit def wrapTypeReference(t: TypeReference) = new RichTypeReference(t)
}

class RichTypeReference(val t: TypeReference) extends AnyVal with PrettyPrintable {
  def prettyPrint: String = {
    val packageName = t.getName().getPackage().toString().replace('/', '.')
    packageName + "." + t.getName().getClassName()
  }
}