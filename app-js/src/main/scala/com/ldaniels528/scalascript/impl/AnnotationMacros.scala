// -   Project: scalajs-scalascript (https://github.com/jokade/scalajs-scalascript)
// Description: Macro implementations for AnnotatedFunction
//
// Distributed under the MIT License (see included file LICENSE)
package com.ldaniels528.scalascript.impl

import scala.reflect.macros.blackbox

protected[scalascript] class AnnotationMacros(val c: blackbox.Context) extends MacroBase {

  import c.universe._

  // print generated code to console during compilation
  private lazy val logCode = c.settings.contains("com.ldaniels528.scalascript.AnnotationMacros.debug")

  def functionDIArray(f: c.Tree) = {
    val diArray = createFunctionDIArray(f)

    val tree =
      q"""{import scalajs.js
         import com.ldaniels528.scalascript.AnnotatedFunction
         new AnnotatedFunction($diArray)
         }"""

    if (logCode) printCode(tree)
    tree
  }

}
