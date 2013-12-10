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

  val host = "https://ci.thomsonreuterslifesciences.com/jenkins/job/Cortellis-Services-Retrieve-build/836/api/xml"

  def index = Action {

    val resp = JenkinsService.callService(host)

    val xmlResult = scala.xml.XML.load(resp.get)

    val failureTest = (xmlResult \\ "mavenModuleSetBuild" \\ "action" \\ "failCount").text
    val skipTest = (xmlResult \\ "mavenModuleSetBuild" \\ "action" \\ "skipCount").text
    val totalTest = (xmlResult \\ "mavenModuleSetBuild" \\ "action" \\ "totalCount").text
    val user = (xmlResult \\ "mavenModuleSetBuild" \\ "action" \\ "participant" \\"fullName").text
    val result  = (xmlResult \\ "mavenModuleSetBuild" \\ "result").text
    val date = (xmlResult \\ "mavenModuleSetBuild" \\ "changeSet"\\"item"\\"date").text
    
    val buf = new StringBuilder
    buf.append("Failure Test = " + failureTest )
    buf.append("Skip Test = " + skipTest  )
    buf.append("Count Test = "+ totalTest)
    buf.append("User Name  = "+ user)
    buf.append("Resul  = "+ result)
    buf.append("Date  = "+ date)
    

    Ok(views.html.index.render(buf.toString))
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