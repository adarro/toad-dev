package io.truthencode.toad
import org.scalatest._
import com.typesafe.scalalogging.slf4j.LazyLogging
import java.net.URI
import com.netaporter.uri.dsl._
import org.apache.http._

import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.methods.HttpGet
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class WeaponTest extends FunSpec with Matchers with LazyLogging {

  lazy val fixture = new {
    import Bootstrap._

    lazy val api = s"http://${serverIp}:${serverPort}/api"
    lazy val other = s"http://${serverIp}:${serverPort}/web"
  }

  describe("non-API filter") {
    it("Should reject websocket calls on unathorized paths") {
      val f = fixture
      import f._
      val uri = other / "weapons" ? ("p1" -> "one") & ("p2" -> 2) & ("p3" -> true)
      val client = HttpClients.createDefault()

      val response = client.execute(new HttpGet(uri))
      val entity = response.getEntity()
    }
  }
}