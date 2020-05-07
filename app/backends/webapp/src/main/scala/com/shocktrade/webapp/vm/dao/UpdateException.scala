package com.shocktrade.webapp.vm.dao

case class UpdateException(message: String, count: Int) extends RuntimeException(s"$message (count = $count)")