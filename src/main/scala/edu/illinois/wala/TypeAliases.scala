package edu.illinois.wala
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey
import com.ibm.wala.ssa.SSAInstruction
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.ssa.SSAFieldAccessInstruction
import com.ibm.wala.ssa.SSAArrayReferenceInstruction
import com.ibm.wala.ssa.SSAArrayStoreInstruction
import com.ibm.wala.classLoader.IField
import com.ibm.wala.ssa.SSAInvokeInstruction
import com.ibm.wala.ssa.SSAMonitorInstruction
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.ipa.callgraph.propagation.PointerKey
import com.ibm.wala.ssa.ISSABasicBlock

trait TypeAliases {
  type SS = BasicBlockInContext[IExplodedBasicBlock]
  type N = CGNode
  
  type BB = ISSABasicBlock
  
  type LocalP = LocalPointerKey
  
  type P = PointerKey

  type O = InstanceKey

  type F = IField

  type WithReference = { def ref: Int }
  type ReferenceI = I with WithReference

  type I = SSAInstruction
  type PutI = SSAPutInstruction
  type GetI = SSAGetInstruction
  type AccessI = SSAFieldAccessInstruction

  type ArrayStoreI = SSAArrayStoreInstruction
  type ArrayReferenceI = SSAArrayReferenceInstruction
  type InvokeI = SSAInvokeInstruction

  type MonitorI = SSAMonitorInstruction

  type G = CallGraph
  type C = IClass
  type M = IMethod
  
  type ProgramCounter = com.ibm.wala.classLoader.ProgramCounter
}