package edu.illinois.wala.ipa.slicer

import edu.illinois.wala.S
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex

trait Wrapper {
  implicit def makeSFromStatement(s: StatementWithInstructionIndex) = S(s.getNode(), s.getInstruction())
}