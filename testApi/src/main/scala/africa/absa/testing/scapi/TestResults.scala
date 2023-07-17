package africa.absa.testing.scapi

case class TestResults(suite: String, test: String, status: String, duration: Option[Long], errMessage: Option[String] = None)

object TestResults {
  val Success: String = "Success"
  val Failure: String = "Failure"

  def success(suiteName: String, testName: String, duration: Option[Long]): TestResults =
    TestResults(suite = suiteName, test = testName, status = Success, duration = duration)

  def failure(suiteName: String, testName: String, duration: Option[Long], errorMessage: String): TestResults =
    TestResults(suite = suiteName, test = testName, status = Failure, duration = duration, errMessage = Some(errorMessage))
}
