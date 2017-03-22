package com.knoldus

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

class MobileStoreSpec extends TestKit(ActorSystem("test-system")) with WordSpecLike
  with BeforeAndAfterAll with MustMatchers {

  override protected def afterAll(): Unit = {
    system.terminate()
  }

  "PurchaseRequestHandler" must {
    "Respond when user is asking for more than one item " in {
      val ref = system.actorOf(Props(classOf[PurchaseRequestHandler], testActor))
      ref tell((PersonDetails("Anmol", "Delhi", 1234567890, 9999950386l), 2), testActor)

      expectMsgPF() {
        case errorMsg: String =>
          errorMsg must be("Cannot Purchase more than one item.")
      }
    }

    "Respond when user is asking for one item " in {
      val ref = system.actorOf(Props(classOf[PurchaseRequestHandler], testActor))
      ref tell((PersonDetails("Anmol", "Delhi", 1234567890, 9999950386l), 1), testActor)

      expectMsgPF() {
        case (customer: PersonDetails, itemCount: Int) =>
          (customer: PersonDetails, itemCount: Int) must be(PersonDetails("Anmol", "Delhi", 1234567890, 9999950386l), 1)
      }
    }
  }

  "ValidationActor" must {
    "Respond when there is no item left in store " in {
      val ref = system.actorOf(Props(classOf[ValidationActor], testActor))
      ref tell((PersonDetails("Anmol", "Delhi", 1234567890, 9999950386l), 1001), testActor)

      expectMsgPF() {
        case errorMsg: String =>
          errorMsg must be("Sorry we are out of stock")
      }
    }

    "Respond when there is enough item to sell in store " in {
      val ref = system.actorOf(Props(classOf[ValidationActor], testActor))
      ref tell((PersonDetails("Anmol", "Delhi", 1234567890, 9999950386l), 1), testActor)

      expectMsgPF() {
        case customer: PersonDetails =>
          customer must be(PersonDetails("Anmol", "Delhi", 1234567890, 9999950386l))
      }
    }
  }

  "PurchaseActor" must {
    "Respond when Payment is receive" in {
      val ref = system.actorOf(Props[PurchaseActor])
      ref tell(PersonDetails("Anmol", "Delhi", 1234567890, 9999950386l), testActor)

      expectMsgPF() {
        case msg: String =>
          msg must be("Thanks For Purchasing!! Your GALAXY-S8 will be delivered soon!! ")
      }
    }
  }

}
