
import akka.actor._
import akka.pattern._
import akka.routing.BalancingPool
import akka.util.Timeout
import org.apache.log4j.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt


case class PersonDetails(name: String, address: String, cardNumber: Long, phoneNumber: Long)

class PurchaseRequestHandler(ref: ActorRef) extends Actor with ActorLogging {

  def receive: Receive = {
    case (customer: PersonDetails, itemCount: Int) =>
      log.info("Inside PurchaseRequestHandler Actor\n")
      if (itemCount == 1) ref forward(customer, itemCount)
      else sender() ! "Cannot Purchase more than one item."

  }
}

class ValidationActor(ref: ActorRef) extends Actor with ActorLogging {

  var totalItemLeft = 1000

  def receive: Receive = {
    case (customer: PersonDetails, itemRequested: Int) =>
      log.info("Inside Validation Actor")
      if (itemRequested <= totalItemLeft) {
        log.info("Your Galaxy s8 is of 50000 rs.")
        log.info("Forwarding to PaymentGateway")

        totalItemLeft -= 1
        log.info(s"$totalItemLeft")
        ref forward customer
      }
      else sender ! "Sorry we are out of stock"
  }
}

class PurchaseActor extends Actor with ActorLogging {

  def receive: Receive = {
    case customer: PersonDetails =>
      log.info("Inside PurchaseActor Actor")
      log.info("Payment Received!!")
      log.info("Sending Galaxy S8 To Customer with details :-")
      log.info(s"Name = ${customer.name}")
      log.info(s"Address = ${customer.address} ")
      log.info(s"Card Number = ${customer.cardNumber}")
      log.info(s"Phone Number = ${customer.phoneNumber} \n")

      sender ! "Thanks For Purchasing!! Your GALAXY-S8 will be delivered soon!! "


  }
}

object MobileStore extends App {


  val log = Logger.getLogger(this.getClass)

  val system = ActorSystem("PhonePurchase")
  val paymentGateway = system.actorOf(BalancingPool(5).props(Props[PurchaseActor]))
  val validation = system.actorOf(Props(classOf[ValidationActor], paymentGateway), "ValidationActor")

  val purchaseRequest1 = system.actorOf(Props(classOf[PurchaseRequestHandler], validation), "purchaseRequest-1")
  val purchaseRequest2 = system.actorOf(Props(classOf[PurchaseRequestHandler], validation), "purchaseRequest-2")


  //val validation = system.actorOf(Props[ValidationActor], "ValidationActor")

  implicit val timeout = Timeout(15 seconds)

  val result = purchaseRequest1 ?(PersonDetails("Anmol", "Delhi", 1234567890, 9999950386l), 1)
  val result1 = purchaseRequest1 ?(PersonDetails("Nitin", "GGN", 1234567890, 9999950386l), 1)
  val result3 = purchaseRequest2 ?(PersonDetails("Vandana", "Noida", 1234567890, 9999950386l), 1)
  result.map { res =>
    log.info(res)
  }
  result1.map { res =>
    log.info(res)
  }
  result3.map { res =>
    log.info(res)
  }


}
