package com.shocktrade.server.common

import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.crypto.Crypto

/**
  * Service Signatures
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class Signatures(passPhrase: String) {

  def encodeSignature(uri: String, ts: Double): String = {
    val md5 = Crypto.createHash("md5")
    md5.update(Buffer.from(passPhrase))
    md5.update(Buffer.from(uri))
    md5.update(Buffer.from(ts.toString))
    md5.digest("UTF8")
  }

  def verifySignature(uri: String, ts: Double, sig: String): Boolean = {
    sig == encodeSignature(uri, ts)
  }

}
