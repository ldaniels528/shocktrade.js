package com.shocktrade.webapp.vm.dao

case class InsufficientFundsException(actual: Double, expected: Double)
  extends VirtualMachineException(f"Insufficient funds (have: $actual%.5f, need: $expected%.5f)")