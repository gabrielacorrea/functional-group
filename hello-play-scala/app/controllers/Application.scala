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
import play.api.data.Form
import model.JobDetail

object Application extends Controller {

    
  def index = Action {

    val url = "Cortellis-Services-Retrieve-build"
    var resp = JenkinsService.callService(url)

    var xmlResult = scala.xml.XML.load(resp.get)

    val lastBuild = (xmlResult \\ "mavenModuleSet" \\ "lastBuild" \\ "number").text

    resp = JenkinsService.callService(url + "/" + lastBuild)
    xmlResult = scala.xml.XML.load(resp.get)

    val failureTest = (xmlResult \\ "mavenModuleSetBuild" \\ "action" \\ "failCount").text
    val skipTest = (xmlResult \\ "mavenModuleSetBuild" \\ "action" \\ "skipCount").text
    val totalTest = (xmlResult \\ "mavenModuleSetBuild" \\ "action" \\ "totalCount").text
    val user = (xmlResult \\ "mavenModuleSetBuild" \\ "action" \\ "participant" \\ "fullName").text
    val user2 = (xmlResult \\ "mavenModuleSetBuild" \\ "culprit" \\ "fullName").text
    val result = (xmlResult \\ "mavenModuleSetBuild" \\ "result").text
    val date = (xmlResult \\ "mavenModuleSetBuild" \\ "changeSet" \\ "item" \\ "date").text

    val buf = new StringBuilder
    buf.append("Last Number = " + lastBuild)
    buf.append("Failure Test = " + failureTest)
    buf.append("Skip Test = " + skipTest)
    buf.append("Count Test = " + totalTest)
    buf.append("User Name  = " + user + " / " + user2)
    buf.append("Result  = " + result)
    buf.append("Date  = " + date)

    val value =JobDetail(lastBuild, failureTest, skipTest, totalTest, user, result, date)
    
    Ok(views.html.index.render(buf.toString,value))
  }

  def dashboard = Action {

    val resp = JenkinsService.callService("Cortellis-Services-Alert-SEDA-build")

    val xml = scala.xml.XML.load(resp.get)

    Ok(views.html.index.render(xml.toString(),null ))
  }

}