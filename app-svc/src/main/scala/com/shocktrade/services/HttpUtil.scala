package com.shocktrade.services

import java.io.ByteArrayOutputStream
import java.net._

import com.shocktrade.services.util.ResourceUtilities._
import org.apache.commons.io.IOUtils

/**
 * HTTP Utilities
 * @author lawrence.daniels@gmail.com
 */
trait HttpUtil {

  def getResource(urlString: String, headers: (String, String)*): Array[Byte] = {
    new URL(urlString).openConnection().asInstanceOf[HttpURLConnection] use { conn =>
      conn.setRequestMethod("GET")
      conn.setInstanceFollowRedirects(true)
      headers foreach { case (key, value) => conn.addRequestProperty(key, value) }
      conn.setDoInput(true)

      // get the input
      conn.getInputStream use { in =>
        val out = new ByteArrayOutputStream(8192)
        IOUtils.copy(in, out)
        out.toByteArray
      }
    }
  }

  def postResource(urlString: String, message: String): Array[Byte] = {
    new URL(urlString).openConnection().asInstanceOf[HttpURLConnection] use { conn =>
      conn.setRequestMethod("POST")
      conn.setInstanceFollowRedirects(true)
      conn.setRequestProperty("Content-Type", "application/json")
      conn.setDoOutput(true)

      // write the output
      conn.getOutputStream use { out =>
        out.write(message.getBytes("UTF8"))
      }

      val statusCode = conn.getResponseCode
      if (statusCode != HttpURLConnection.HTTP_OK)
        throw new IllegalStateException(s"Server returned HTTP/$statusCode")

      // get the input
      conn.getInputStream use { in =>
        val out = new ByteArrayOutputStream(8192)
        IOUtils.copy(in, out)
        out.toByteArray
      }
    }
  }

}