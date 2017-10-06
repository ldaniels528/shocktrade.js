package com.shocktrade.controlpanel.runtime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Represents a Typed Value
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
sealed trait TypedValue extends Evaluatable {

  def value: Any

  def +(value: TypedValue): TypedValue

  def -(value: TypedValue): TypedValue

  def *(value: TypedValue): TypedValue

  def /(value: TypedValue): TypedValue

  override def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext) = Future.successful(this)

  override def toString = value.toString

}

/**
  * Represents an array value
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
case class ArrayValue(value: Seq[Any]) extends TypedValue {

  override def +(tv: TypedValue) = tv match {
    case ArrayValue(values) => ArrayValue(value ++ values)
    case array => ArrayValue(value ++ Seq(array.value))
  }

  override def -(tv: TypedValue) = this

  override def *(tv: TypedValue) = this

  override def /(tv: TypedValue) = this

  override def toString = s"[${value.mkString(", ")}]"

}

/**
  * Represents a Null value
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object Null extends TypedValue {

  override def value: Any = null

  override def +(value: TypedValue) = this

  override def -(value: TypedValue) = this

  override def *(value: TypedValue) = this

  override def /(value: TypedValue) = this

  override def toString = "null"

}

/**
  * Represents a Numeric Value
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
case class NumericValue(value: Double) extends TypedValue {

  override def +(tv: TypedValue) = tv match {
    case ArrayValue(items) => ArrayValue(Seq(value) ++ items)
    case Null => Null
    case NumericValue(value1) => NumericValue(value + value1)
    case TextValue(value1) => NumericValue(value + value1.toDouble)
    case other => throw new IllegalStateException(s"Unsupported operation: ${other.value} + $value")
  }

  override def -(tv: TypedValue) = tv match {
    case Null => Null
    case NumericValue(value1) => NumericValue(value - value1)
    case TextValue(value1) => NumericValue(value - value1.toDouble)
    case other => throw new IllegalStateException(s"Unsupported operation: ${other.value} - $value")
  }

  override def *(tv: TypedValue) = tv match {
    case Null => Null
    case NumericValue(value1) => NumericValue(value * value1)
    case TextValue(value1) => NumericValue(value * value1.toDouble)
    case other => throw new IllegalStateException(s"Unsupported operation: ${other.value} * $value")
  }

  override def /(tv: TypedValue) = tv match {
    case Null => Null
    case NumericValue(value1) => NumericValue(value / value1)
    case TextValue(value1) => NumericValue(value / value1.toDouble)
    case other => throw new IllegalStateException(s"Unsupported operation: ${other.value} / $value")
  }

}

/**
  * Represents a String Value
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
case class TextValue(value: String) extends TypedValue {

  override def +(tv: TypedValue) = tv match {
    case ArrayValue(items) => ArrayValue(Seq(value) ++ items)
    case Null => Null
    case NumericValue(value1) => TextValue(value + value1)
    case TextValue(value1) => TextValue(value + value1)
    case other => throw new IllegalStateException(s"Unsupported operation: ${other.value} + $value")
  }

  override def -(tv: TypedValue) = tv match {
    case Null => Null
    case NumericValue(value1) => TextValue((value.toDouble - value1).toString)
    case TextValue(value1) => TextValue((value.toDouble - value1.toDouble).toString)
    case other => throw new IllegalStateException(s"Unsupported operation: ${other.value} - $value")
  }

  override def *(tv: TypedValue) = tv match {
    case Null => Null
    case NumericValue(value1) => TextValue((value.toDouble * value1.toDouble).toString)
    case TextValue(value1) => TextValue((value.toDouble * value1.toDouble).toString)
    case other => throw new IllegalStateException(s"Unsupported operation: ${other.value} * $value")
  }

  override def /(tv: TypedValue) = tv match {
    case Null => Null
    case NumericValue(value1) => TextValue((value.toDouble / value1).toString)
    case TextValue(value1) => TextValue((value.toDouble / value1.toDouble).toString)
    case other => throw new IllegalStateException(s"Unsupported operation: ${other.value} / $value")
  }

}