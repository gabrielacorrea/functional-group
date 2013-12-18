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

    val failureTest = (xmlResult \\  "action" \\ "failCount").text
    val skipTest = (xmlResult \\  "action" \\ "skipCount").text
    val totalTest = (xmlResult \\  "action" \\ "totalCount").text
    val user = (xmlResult \\  "action" \\ "cause" \\ "userName").text
    val user2 = (xmlResult \\  "culprit" \\ "fullName").text
    val result = (xmlResult \\  "result").text
    val date = (xmlResult \\  "changeSet" \\ "item" \\ "date").text


    val value =JobDetail(lastBuild, failureTest, skipTest, totalTest, user, result, date, jobSummary(lastBuild,name))
    
    Ok(views.html.showDetail.render(value))
    
    
  } 
  
    def jobSummary(lastJobNumber:String , name:String): String = {
      
      val jobRange = lastJobNumber.toInt - 10 until lastJobNumber.toInt
      
      var sumSuccess = 0
      var sumFailure = 0
      
      
      jobRange.foreach(
          x =>  {  
            val result = verifyJobStatus(name, x)
            		
    	  	result match {
              case "SUCCESS"  	=> sumSuccess = sumSuccess+ 1
              case _ 			=> sumFailure = sumFailure+ 1
            	}
      		}
      )
          
      
      "SUCCESS: " + sumSuccess + " FAILURE: "  + sumFailure
      
  }
    
    
 def verifyJobStatus(jobName:String , jobNumber:Integer) :String = {
   (scala.xml.XML.load(JenkinsService.callService(jobName + "/" + jobNumber).get) \\  "result").text
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
        html.append("<li><h2> <a href ='showDetail/" + keyVal._1 + "'>").append(keyVal._1)

        val currentBuildNumber = (keyVal._2 \\  "lastBuild" \\ "number").text
        val lastFailBuildNumber = (keyVal._2 \\ "lastFailedBuild" \\ "number").text

        html.append(currentBuildNumber match {
          case currentBuildNumber if currentBuildNumber.equals(lastFailBuildNumber) => " - <img src='/assets/images/error.png' />"
          case _ => " - <img src='/assets/images/success.png' />"
        }).append("</a></h2></li>")
       }
      }

     html.append("</ul>")
    
    Ok(views.html.dashboard.render(html))
  }

}
