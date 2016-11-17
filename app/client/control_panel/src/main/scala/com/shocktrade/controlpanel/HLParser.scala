package com.shocktrade.controlpanel

import com.shocktrade.controlpanel.Tokenizer.TokenIterator

/**
  * High-Level Parser
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class HLParser(it: TokenIterator) {

  def expect(tok: String) = {
    val actual = nextOption()
    if(actual.isEmpty || !actual.contains(tok)) {
      throw new IllegalArgumentException(s"Expected '$tok' (actual '${actual.getOrElse("")}')")
    }
  }

  def next() = it.next()

  def nextOption() = if(it.hasNext) Option(next()) else None

  def peek = if(it.hasNext) Some(it.peek) else None

  def until(tok: String) = {
    var list: List[String] = Nil
    var found = false
    while(it.hasNext && !found) {
      val item = it.next()
      found = item == tok
      if(!found) list = item :: list
    }
    list.reverse
  }

  def extractParams() = extractSeq("(", ")")

  def extractSeq(startTok: String, endTok: String) = {
    expect(startTok)
    var level = 0
    var list: List[String] = Nil
    var found = false
    while(it.hasNext && !found) {
      val item = it.next()
      found = level == 0 && item == endTok
      if(item == startTok) level += 1
      else if(item == endTok) level -= 1
      if(!found) list = item :: list
    }
    list.reverse
  }

}
