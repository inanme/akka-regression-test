package next

import akka.http.scaladsl.server.{ Directive1, Directives }

object App extends Directives {

  def routes: Directive1[Int] = get & provide(1)

}
