package basic

import scala.concurrent.duration._
import scala.collection.mutable
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import akka.actor.{ Actor, ActorSystem, Props, DeadLetter }
import akka.util.Timeout
import akka.testkit.{ TestKit, ImplicitSender }
import akka.actor.DeadLetter
import org.scalatest.matchers.BePropertyMatchResult
import org.scalatest.matchers.BePropertyMatcher
import akka.actor.UnhandledMessage
import org.mockito.Mockito
import org.mockito.verification.VerificationMode
import org.slf4j.LoggerFactory

import scala.reflect.ClassTag

trait MovioSpec extends FunSpecLike
  with Matchers
  with MovioMatchers
  with MockitoSugar
  with MockitoWrapper
  with BeforeAndAfter
  with BeforeAndAfterAll

abstract class AkkaSpec extends TestKit(ActorSystem("AkkaTestSystem"))
    with ImplicitSender
    with MovioSpec {
  val log = LoggerFactory.getLogger(getClass)
  override def afterAll() { system.shutdown() }
  implicit val timeout = Timeout(10 seconds)

  val listener = system.actorOf(Props(new Actor {
    def receive = {
      case m: DeadLetter ⇒
        log.warn("Received a dead letter: " + m)
      case m: UnhandledMessage ⇒
        log.warn("Some message wasn't delivered: check that your actor's receive methods handle all messages you need: " + m)
    }
  }))
  system.eventStream.subscribe(listener, classOf[DeadLetter])
  system.eventStream.subscribe(listener, classOf[UnhandledMessage])

  def expectMsgAllOfIgnoreOthers[T](max: Duration, expected: T*) {
    val outstanding = mutable.Set(expected: _*)
    fishForMessage(max) {
      case msg: T if outstanding.contains(msg) ⇒
        outstanding.remove(msg)
        outstanding.isEmpty
      case _ ⇒ false
    }
  }

  def expectMsgAllOfIgnoreOthers[T](expected: T*) {
    expectMsgAllOfIgnoreOthers(3 seconds, expected: _*)
  }

  def time(func: ⇒ Unit): Duration = {
    val start = System.currentTimeMillis
    func
    val millis = System.currentTimeMillis - start
    Duration(millis, MILLISECONDS)
  }

  def maxMillis(maxMillis: Long)(func: ⇒ Unit) {
    val start = System.currentTimeMillis
    func
    val millis = System.currentTimeMillis - start
    millis should be < (maxMillis)
  }
}

trait MovioMatchers {
  def anInstanceOf[T](implicit ct: ClassTag[T]) = {
    val clazz = ct.runtimeClass.asInstanceOf[Class[T]]
    new BePropertyMatcher[AnyRef] {
      def apply(left: AnyRef) =
        BePropertyMatchResult(left.getClass.isAssignableFrom(clazz), "an instance of " + clazz.getName)
    }
  }
}

trait MockitoWrapper {
  def verify[T](mock: T) = Mockito.verify(mock)
  def verify[T](mock: T, mode: VerificationMode) = Mockito.verify(mock, mode)
  def when[T](methodCall: T) = Mockito.when(methodCall)
  def never = Mockito.never
  def times(wantedNumberOfInvocations: Int) = Mockito.times(wantedNumberOfInvocations)
}

