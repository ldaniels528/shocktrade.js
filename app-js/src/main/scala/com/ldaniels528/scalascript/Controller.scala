// -   Project: scalajs-scalascript (https://github.com/jokade/scalajs-scalascript)
// Description: Defines marker traits for scalascript controllers
//
// Distributed under the MIT License (see included file LICENSE)
package com.ldaniels528.scalascript

import acyclic.file
import com.ldaniels528.scalascript.core.{Attributes, JQLite}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportAll, JSExport, JSName}

sealed trait NGController

/**
 * Marks a class as AngularJS controller, to be used with the "controllerAs" syntax.
 *
 * All public `val`s, `var`s and `def`s defined in the controller class will be exported
 * to the controller scope.
 */
trait Controller extends NGController


trait ScopeController extends NGController

