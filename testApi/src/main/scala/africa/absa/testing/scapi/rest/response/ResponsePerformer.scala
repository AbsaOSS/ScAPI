package africa.absa.testing.scapi.rest.response

import africa.absa.testing.scapi.json.Assertion

trait ResponsePerformer {
  def validateContent(assertion: Assertion): Unit
  def performAssertions(response: Response, assertion: Assertion): Unit
}
