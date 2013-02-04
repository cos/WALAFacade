package edu.illinois.wala.ssa

import com.ibm.wala.ipa.cha.IClassHierarchy
import com.ibm.wala.ssa.SSAFieldAccessInstruction
import wala.WALAConversions._
import wala.extra.ArrayContents

class RichPutI(val i: PutI) extends AnyVal {
  def v = V(i.getVal())
}

class RichGetI(val i: GetI) extends AnyVal {
  def d = V(i.getDef())
}

class RichInvokeI(val i: InvokeI) extends AnyVal {
  def m(implicit cha: IClassHierarchy) = cha.resolveMethod(i.getDeclaredTarget())
}

trait IWithField extends Any {
  def f(implicit cha:IClassHierarchy): Option[F]
} 

class RichAccessI(val i: AccessI) extends AnyVal with IWithField {
  /**
   * Returns None when the cha cannot resolve the field.
   */
  def f(implicit cha: IClassHierarchy) = Option(cha.resolveField(i.getDeclaredField()))
}

class RichArrayReferenceI(val i: ArrayReferenceI) extends AnyVal with IWithField {
  def f(implicit cha: IClassHierarchy): Some[F] = Some(ArrayContents)
}

class RichI(val i: I) extends AnyVal {
  def uses: Stream[V] = Stream.range(0, i.getNumberOfUses()).map(index => { V(i.getUse(index)) })
  def f(implicit cha: IClassHierarchy): Option[F] = i match {
    case i: AccessI => i.f
    case i: ArrayReferenceI => i.f
    case _ => None
  }
}