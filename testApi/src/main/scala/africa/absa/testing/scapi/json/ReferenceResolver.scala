/*
 * Copyright 2023 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package africa.absa.testing.scapi.json

import africa.absa.testing.scapi.utils.cache.RuntimeCache
import africa.absa.testing.scapi.{PropertyNotFound, UndefinedConstantsInProperties}

import scala.util.matching.Regex

/**
 * A sealed protected trait that provides functionality for resolving references.
 */
sealed protected trait ReferenceResolver {

  /**
   * Method to resolve multiple references.
   *
   * @param toResolve  A map containing values to resolve.
   * @param references A map containing actual reference values.
   * @return A map containing resolved references.
   */
  protected def getResolved(toResolve: Map[String, String], references: Map[String, String]): Map[String, String] = {
    val (resolvedActions, notResolvedReferences) = resolve(toResolve, references)
    notResolved(notResolvedReferences)
    resolvedActions
  }

  /**
   * Method to resolve a single reference.
   *
   * @param toResolve  A string containing a value to resolve.
   * @param references A map containing actual reference values.
   * @return A string containing the resolved reference.
   */
  protected def getResolved(toResolve: String, references: Map[String, String]): String = {
    val (resolvedActions, notResolvedReferences) = resolve(toResolve, references)
    notResolved(notResolvedReferences)
    resolvedActions
  }

  /**
   * If there are any unresolved references, it throws an exception.
   *
   * @param notResolvedReferences A set of unresolved reference keys.
   * @throws UndefinedConstantsInProperties If there are any unresolved references.
   */
  private def notResolved(notResolvedReferences: Set[String]): Unit =
    if (notResolvedReferences.nonEmpty) throw UndefinedConstantsInProperties(notResolvedReferences, s"'${getClass.getSimpleName}' action.")

  /**
   * Resolve a map of references to their actual values. It iteratively updates the map with resolved values.
   *
   * @param toResolve  A map of references to resolve.
   * @param references A map of actual reference values.
   * @return A tuple of a map of resolved references and a set of unresolved references.
   */
  private def resolve(toResolve: Map[String, String], references: Map[String, String]): (Map[String, String], Set[String]) = {
    toResolve.foldLeft(Map.empty[String, String], Set.empty[String]) {
      case ((accResolved, accNotResolvedReferences), (key, property)) =>
        val (resolvedProperty, notResolvedReferences) = resolve(property, references)
        (accResolved + (key -> resolvedProperty), accNotResolvedReferences ++ notResolvedReferences)
    }
  }

  /**
   * Resolve a single reference to its actual value. It updates the string with resolved value.
   *
   * @param toResolve  A string of references to resolve.
   * @param references A map of actual reference values.
   * @return A tuple of the resolved reference and a set of unresolved references.
   */
  private def resolve(toResolve: String, references: Map[String, String]): (String, Set[String]) = {
    val pattern = """\{\{\s*(.*?)\s*}}""".r
    var collected: Set[String] = Set.empty
    val resultMatch = (matchResult: Regex.Match) => {
      val propertyKey: String = matchResult.group(1).trim
      if (propertyKey.contains("cache.")) {
        matchResult.group(0)
      } else {
        references.getOrElse(propertyKey, {
          collected = collected + propertyKey
          toResolve
        })
      }
    }
    val resolved = pattern.replaceAllIn(toResolve, resultMatch)

    (resolved, collected)
  }
}

/**
 * A case class that represents an environment for reference resolution.
 */
case class Environment private(constants: Map[String, String], properties: Map[String, String]) extends ReferenceResolver {

  /**
   * Method to retrieve the value for the given key from the properties or constants.
   *
   * @param key The key to retrieve the value for.
   * @return The value corresponding to the key.
   */
  def apply(key: String): String = properties.getOrElse(key, constants.getOrElse(key, throw PropertyNotFound(key)))

  /**
   * Method to resolve all the references in the environment's properties.
   *
   * @return An environment with all references resolved.
   */
  def resolveReferences: Environment = Environment(constants, getResolved(properties, constants))

  /**
   * Method to transform the environment into a map.
   *
   * @param prefix The prefix to append to all keys in the map.
   * @return A map representation of the environment.
   */
  def asMap(prefix: String = "env."): Map[String, String] = (constants ++ properties).map { case (key, value) => s"$prefix$key" -> value }
}

/**
 * Case class that represents SuiteConstants.
 * This class is used to hold constants used in a test suite.
 * It implements the `ReferenceResolver` trait to support resolution of reference constants.
 *
 * @constructor create a new SuiteConstants with a map of constants.
 * @param constants a map of constants where key is the constant name and value is the constant value.
 */
case class SuiteConstants private(constants: Map[String, String]) extends ReferenceResolver {

  /**
   * Method to resolve references.
   *
   * @param properties the map of properties that may be used to resolve references in the constants.
   * @return a new SuiteConstants instance with resolved references.
   */
  def resolveReferences(properties: Map[String, String]): SuiteConstants =
    this.copy(constants = getResolved(constants, properties)
      .map { case (key, value) => s"constants.$key" -> value })
}

/**
 * Case class that represents one Header option.
 * It implements the `ReferenceResolver` trait to support resolution of reference constants.
 *
 * @constructor create a new Header with a name and value.
 * @param name the name of the header option.
 * @param value the value of the header option.
 */
case class Header private(name: String, value: String) extends ReferenceResolver {

  /**
   * Method to resolve references.
   *
   * @param references the map of references that may be used to resolve references in the value.
   * @return a new Header option instance with resolved references.
   */
  def resolveReferences(references: Map[String, String]): Header = this.copy(value = getResolved(value, references))
}

/**
 * Case class that represents Action.
 * This class is used to hold test actions.
 * It implements the `ReferenceResolver` trait to support resolution of reference constants.
 *
 * @constructor create a new Action with a name and value.
 * @param methodName the name of the action.
 * @param url the value of the action.
 * @param body the body of the action - optional.
 */
case class Action private(methodName: String, url: String, body: Option[String] = None, params: Option[Set[Param]] = None) extends ReferenceResolver {

  /**
   * Method to resolve references.
   *
   * @param references the map of references that may be used to resolve references in the value.
   * @return a new Action instance with resolved references.
   */
  def resolveReferences(references: Map[String, String]): Action = this.copy(
    url = getResolved(url, references),
    body = body.map(b => getResolved(b, references)),
    params = params.map(_.map(param => param.resolveReferences(references)))
  )
}

/**
 * Case class that represents ResponseAction.
 * This class is used to hold test response action.
 * It implements the `ReferenceResolver` trait to support resolution of reference constants.
 *
 * @constructor create a new ResponseAction with a name and value.
 * @param name the name of the response action.
 * @param params the map containing the parameters of the response action. Each key-value pair in the map
 * represents a parameter name and its corresponding value.
 */
case class ResponseAction private(group: String,
                                  name: String,
                                  params: Map[String, String]) extends ReferenceResolver {

  /**
   * Method to resolve references.
   *
   * @param references the map of references that may be used to resolve references in the value.
   * @return a new ResponseAction instance with resolved references.
   */
  def resolveReferences(references: Map[String, String]): ResponseAction = this.copy(
    params = this.params.map { case (k, v) => k -> getResolved(v, references) }
  )

  /**
   * Method to resolve references using Runtime Cache. This method is used when the resolution of a reference is not possible at compile-time.
   *
   * @return A new ResponseAction instance with resolved references.
   */
  def resolveByRuntimeCache(): ResponseAction = this.copy(
    params = this.params.map { case (k, v) => k -> RuntimeCache.resolve(v) }
  )
}

/**
 * Case class that represents one Header option.
 * It implements the `ReferenceResolver` trait to support resolution of reference constants.
 *
 * @constructor create a new Header with a name and value.
 * @param name the name of the header option.
 * @param value the value of the header option.
 */
case class Param private(name: String, value: String) extends ReferenceResolver {

  /**
   * Method to resolve references.
   *
   * @param references the map of params that may be used to resolve references in the value.
   * @return a new Param option instance with resolved references.
   */
  def resolveReferences(references: Map[String, String]): Param = this.copy(value = getResolved(value, references))
}
