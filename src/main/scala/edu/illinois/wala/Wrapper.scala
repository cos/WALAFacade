package edu.illinois.wala

import com.ibm.wala.ipa.slicer.Statement

trait Wrapper {
  implicit def statementToS(s: Statement) = new {
    def n = s.getNode
  }
}