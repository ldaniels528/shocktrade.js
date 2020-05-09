package com.shocktrade.webapp.vm.dao

case class InsufficientQuantityException(actual: Double, expected: Double)
  extends VirtualMachineException(f"Insufficient quantity (have: $actual%.0f, need: $expected%.0f)")