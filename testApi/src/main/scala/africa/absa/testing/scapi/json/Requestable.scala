package africa.absa.testing.scapi.json

trait Requestable {
  def name: String

  def headers: Set[Header]

  def actions: Set[Action]

  def assertions: Set[Assertion]
}
