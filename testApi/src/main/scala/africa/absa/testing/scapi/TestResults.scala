package africa.absa.testing.scapi

case class TestResults(suite: String, test: String, status: String, duration: Option[Long], errMessage: Option[String] = None, categories: String = "")

object TestResults {
  val Success: String = "Success"
  val Failure: String = "Failure"

  def success(suiteName: String, testName: String, duration: Option[Long], category: String = ""): TestResults =
    TestResults(suite = suiteName, test = testName, status = Success, duration = duration, categories = category)

  def failure(suiteName: String, testName: String, duration: Option[Long], categories: String = "", errorMessage: String): TestResults =
    TestResults(suite = suiteName, test = testName, status = Failure, duration = duration, categories = categories, errMessage = Some(errorMessage))
}
