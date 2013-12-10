package controllers

import java.io.InputStream
import scala.language.postfixOps
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.ws.WS
import scala.xml._
import service.JenkinsService

object Application extends Controller {

  val host = "https://ci.thomsonreuterslifesciences.com/jenkins/job/Cortellis-Services-Retrieve-build/api/xml"

  def index = Action {

    val resp = JenkinsService.callService(host)
    
    val xml = scala.xml.XML.load(resp.get)  
    
    Ok(views.html.index.render(xml.toString()))
  }


  def parseJson(): String = {

    val json: JsValue = Json.parse("""
{
  "user": {
    "name" : "toto",
    "age" : 25,
    "email" : "toto@jmail.com",
    "isAlive" : true,
    "friend" : {
      "name" : "tata",
      "age" : 20,
      "email" : "tata@coldmail.com"
    }
  }
}
""")

    val name: String = (json \ "user" \ "name").as[String]

    name
  }
}