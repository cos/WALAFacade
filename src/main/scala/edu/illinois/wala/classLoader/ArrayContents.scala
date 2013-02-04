package edu.illinois.wala.classLoader

import com.ibm.wala.classLoader.IClass
import com.ibm.wala.classLoader.IField
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.types.FieldReference
import com.ibm.wala.types.TypeReference
import com.ibm.wala.util.debug.UnimplementedError
import com.ibm.wala.util.strings.Atom
import java.util.Collections

/**
 *
 * Derived from ArrayContents in demandpa package
 *
 */

object ArrayContents extends IField {

  override def getFieldTypeReference: TypeReference = throw new UnimplementedError()

  override def isFinal: Boolean = throw new UnimplementedError()

  override def isPrivate: Boolean = throw new UnimplementedError()

  override def isProtected: Boolean = throw new UnimplementedError()

  override def isPublic: Boolean = throw new UnimplementedError()

  override def isStatic: Boolean = throw new UnimplementedError()

  override def getDeclaringClass: IClass = throw new UnimplementedError()

  override def getName: Atom = Atom.findOrCreateUnicodeAtom("[*]");

  override def toString = "[*]"

  override def isVolatile: Boolean = false

  override def getClassHierarchy: ClassHierarchy = throw new UnimplementedError()

  override def getReference: FieldReference = throw new UnimplementedError()
  
  override def getAnnotations() = Collections.emptySet()
}