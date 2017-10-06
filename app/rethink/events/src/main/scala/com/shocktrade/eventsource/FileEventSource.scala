package com.shocktrade.eventsource

import io.scalajs.JSON
import io.scalajs.nodejs.console
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.readline.{Readline, ReadlineOptions}

/**
  * File Event Source
  * @author lawrence.daniels@gmail.com
  */
class FileEventSource(outputPath: String) extends EventSource {
  private val out = Fs.createWriteStream(outputPath)

  override def replay(handler: SourcedEvent => Unit): Unit = {
    val iface = Readline.createInterface(new ReadlineOptions(
      input = Fs.createReadStream(outputPath)
    ))
    iface.onLine { line =>
      val jsObject = JSON.parseAs[SourcedEvent](line)
      val event_? = for {
        _ <- jsObject.name
        _ <- jsObject.uuid
      } yield jsObject
      event_?.toOption match {
        case Some(event) => handler(event)
        case None =>
          console.warn(s"Event not recognized: $line")
      }
    }
    ()
  }

  override def persistEvent(event: SourcedEvent): Boolean = {
    out.write(JSON.stringify(event) + "\n", encoding = "UTF8")
  }

}
