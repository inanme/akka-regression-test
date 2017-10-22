package com.example

import akka.actor._
import akka.cluster._
import akka.cluster.ClusterEvent._
import scala.concurrent._
import scala.concurrent.duration._

package fdfjdafdsasflds23 {
  class ClusterDomainEventListener extends Actor with ActorLogging {
    Cluster(context.system).subscribe(self, classOf[ClusterDomainEvent])

    def receive: Receive = {
      case MemberUp(member) => log.info(s"------$member UP.")
      case MemberExited(member) => log.info(s"------$member EXITED.")
      case MemberRemoved(m, previousState) =>
        if (previousState == MemberStatus.Exiting) {
          log.info(s"------Member $m gracefully exited, REMOVED.")
        } else {
          log.info(s"------$m downed after unreachable, REMOVED.")
        }
      case UnreachableMember(m) => log.info(s"------$m UNREACHABLE")
      case ReachableMember(m) => log.info(s"------$m REACHABLE")
      case s: CurrentClusterState => log.info(s"------cluster state: $s")
      case it ⇒ log.info(s"-----What is this $it")
    }

    override def postStop(): Unit = {
      Cluster(context.system).unsubscribe(self)
      super.postStop()
    }
  }
  object MyCluster1 extends App with MyCluster {
    swallow(system.actorOf(Props[ClusterDomainEventListener]))
    Await.result(system.whenTerminated, Duration.Inf)
  }
  object MyCluster2 extends App with MyCluster {
    swallow(system.actorOf(Props[ClusterDomainEventListener]))
    Await.result(system.whenTerminated, Duration.Inf)
  }
  object MyCluster3 extends App with MyCluster {
    swallow(system.actorOf(Props[ClusterDomainEventListener]))
    Await.result(system.whenTerminated, Duration.Inf)
  }
}

