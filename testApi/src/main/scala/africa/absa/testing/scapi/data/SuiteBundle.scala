package africa.absa.testing.scapi.data

import africa.absa.testing.scapi.json.{Suite, SuiteAfter, SuiteBefore}

case class SuiteBundle(suite: Suite, suiteBefore: Option[SuiteBefore] = None, suiteAfter: Option[SuiteAfter] = None)
