// -   Project: scalajs-scalascript (https://github.com/jokade/scalajs-scalascript)
// Description: Defines scalascript-specific annotations
//
// Distributed under the MIT License (see included file LICENSE)
package com.ldaniels528.scalascript

import acyclic.file
import scala.annotation.StaticAnnotation
import scala.scalajs.js

/**
 * Annotation for explicitly setting the name of a service to be injected.
 *
 * @param name
 */
@annotation.meta.param
case class named(name: String) extends StaticAnnotation

/**
 * Creates a Component definition to be used with the [[Component]] annotation.
 *
 * @param selector HTML tag name for this component
 * @param template
 * @param templateUrl
 * @param bind
 */
case class ComponentDef(selector: String,
                        template: String = null,
                        templateUrl: String = null,
                        bind: js.Dictionary[String] = null)

/**
 *
 * @param cd
 */
case class Component(cd: ComponentDef) extends StaticAnnotation


