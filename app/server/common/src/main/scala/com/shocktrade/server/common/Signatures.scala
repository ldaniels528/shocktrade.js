package com.shocktrade.server.common

import org.scalajs.nodejs.NodeRequire
import org.scalajs.nodejs.buffer.Buffer
import org.scalajs.nodejs.crypto.Crypto

import scala.language.postfixOps

/**
  * Service Signatures
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class Signatures(passPhrase: String)(implicit require: NodeRequire) {
  private val crypto = Crypto()

  def encodeSignature(uri: String, ts: Double): String = {
    val md5 = crypto.createHash("md5")
    md5.update(Buffer.from(passPhrase))
    md5.update(Buffer.from(uri))
    md5.update(Buffer.from(ts.toString))
    md5.digest("UTF8")
  }

  def verifySignature(uri: String, ts: Double, sig: String): Boolean = {
    sig == encodeSignature(uri, ts)
  }

}
