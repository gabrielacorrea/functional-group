package controllers

import java.io.InputStream
import scala.language.postfixOps
import scala.xml._
import model.JobDetail
import play.api.libs.json.JsValue
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.ws.WS
import scala.xml._
import scala.collection.mutable.Map
import service.JenkinsService
import play.api.data.Forms._

object Application extends Controller {

    
  def index = Action{
    Ok("Hello Scala")
  }

  def showDetail(name:String) = Action {
	val url = name
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
    
    Ok(views.html.showDetail.render(buf.toString,value))
  }

  def dashboard = Action {

    val jobs : Seq[String] = Seq("Cortellis-Services-Alert-SEDA-build", "Cortellis-Services-Export-build", "Cortellis-Services-Retrieve-Regulatory-CI")

    //
    // traverse each of the jobs
    //
    var results = Map[String, scala.xml.Elem]()
    jobs.map(job =>  results += job -> scala.xml.XML.load(JenkinsService.callService(job).get))

    //
    // prepare the content
    //
    var html :StringBuilder  = new StringBuilder

    html.append("<ul>")
    
    results.foreach{
      keyVal => {
        html.append("<li><h2>").append(keyVal._1)

        val currentBuildNumber = (keyVal._2 \\  "lastBuild" \\ "number").text
        val lastFailBuildNumber = (keyVal._2 \\ "lastFailedBuild" \\ "number").text

        html.append(currentBuildNumber match {
          case currentBuildNumber if currentBuildNumber.equals(lastFailBuildNumber) => " - <img src='/assets/images/error.png' />"
          case _ => " - <img src='/assets/images/success.png' />"
        }).append("</h2></li>")
       }
      }

     html.append("</ul>")
    
    Ok(views.html.dashboard.render(html))
  }

}
