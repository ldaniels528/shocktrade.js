package com.ldaniels528.javascript.angularjs.core

import scala.scalajs.js

/**
 * Represents an Angular.js Module
 * @author lawrence.daniels@gmail.com
 * @see [[https://docs.angularjs.org/api/ng/type/angular.Module]]
 */
trait Module extends js.Object {

  /**
   * The name of the module
   */
  def name: String = js.native

  /**
   * Defines an animation hook that can be later used with the \$animate service and directives that use this service.
   *
   * @note animations take effect only if the ngAnimate module is loaded
   *
   * @param name animation name
   * @param animationFactory Array with the names of the dependencies to be injected.
   *                         The last element in this array must be the factory function
   *
   * @see [[https://docs.angularjs.org/api/ng/type/angular.Module#animate]]
   */
  def animation(name: String, animationFactory: js.Array[Any]): Module = js.native

  /**
   * Use this method to register work which needs to be performed on module loading.
   *
   * @param configFn Array with the names of the dependencies to be injected.
   *                 The last element in this array must be the function to be called on module load
   *
   * @see [[https://docs.angularjs.org/api/ng/type/angular.Module#config]]
   */
  def config(configFn: js.Array[Any]): Module = js.native

  /**
   * Register a constant service, such as a string, number, array ...
   *
   * @param name The name of the constant
   * @param value The constant value
   *
   * @see [[https://docs.angularjs.org/api/ng/type/angular.Module#constant]]
   */
  def constant(name: String, value: js.Any): Module = js.native

  /**
   * Registers a controller.
   *
   * @param name The name of the controller
   * @param constructor Array containing the names of the dependencies to be injected and
   *                    the constructor function as last element
   *
   * @see [[https://docs.angularjs.org/api/ng/type/angular.Module#controller]]
   */
  def controller(name: String, constructor: js.Array[Any]): Module = js.native

  /**
   * Register a new directive with the compiler.
   *
   * @param name Name of the directive in camel-case (ie `ngBind`)
   * @param directiveFactory Array containing the names of the dependencies to be injected and
   *                         the constructor function as last element
   *
   * @see [[https://docs.angularjs.org/api/ng/type/angular.Module#directive]]
   */
  def directive(name: String, directiveFactory: js.Array[Any]): Module = js.native

  /**
   * Register a new directive with the compiler.
   *
   * @param name Name of the directive in camel-case (ie `ngBind`)
   * @param directiveFactory Function that returns the directive definition object (DDO) when called
   *
   * @see [[https://docs.angularjs.org/api/ng/type/angular.Module#directive]]
   */
  def directive(name: String, directiveFactory: js.Function): Module = js.native

  /**
   * Register a service factory.
   *
   * @param name The name of the service
   * @param constructor Array containing the names of the dependencies to be injected and
   *                    the constructor function as last element
   *
   * @see [[https://docs.angularjs.org/api/ng/type/angular.Module#factory]]
   */
  def factory(name: String, constructor: js.Array[Any]): Module = js.native

  /**
   * Register a filter factory.
   *
   * @param name The name of the filter
   * @param filterFactory Array containing the names of the dependencies to be injected and
   *                      the constructor function as last element
   *
   * @see [[https://docs.angularjs.org/api/ng/type/angular.Module#filter]]
   */
  def filter(name: String, filterFactory: js.Array[Any]): Module = js.native

  /**
   * Register a provider function with the \$injector.
   *
   * @param name The name of the instance. NOTE: the provider will be available under name + 'Provider' key.
   * @param constructor Array containing the names of the dependencies to be injected and
   *                    the constructor function as last element
   *
   * @see [[https://docs.angularjs.org/api/ng/type/angular.Module#provider]]
   */
  def provider(name: String, constructor: js.Array[Any]): Module = js.native

  /**
   * Use this method to register work which should be performed when the injector is done loading all modules.
   *
   * @param initializationFn Array containing the names of the dependencies to be injected and
   *                         the initialization function as last element
   *
   * @see [[https://docs.angularjs.org/api/ng/type/angular.Module#run]]
   */
  def run(initializationFn: js.Array[Any]): Module = js.native

  /**
   * Register a service constructor which will be invoked with `new` to create the service instance.
   *
   * @param name The name of the service
   * @param constructor A class constructor function
   *
   * @see [[https://docs.angularjs.org/api/ng/type/angular.Module#service]]
   */
  def service(name: String, constructor: js.Array[Any]): Module = js.native

}
