package controllers

import scala.collection.mutable.Map
import scala.language.postfixOps
import model.JobDetail
import play.api.mvc.Action
import play.api.mvc.Controller
import service.JenkinsService
import scala.collection.mutable.ListBuffer
import java.util.Calendar
import java.text.SimpleDateFormat

object Application extends Controller {

  def index = Action {
    Ok("Hello Scala")
  }

  def showDetail(name: String) = Action {
    val url = name
    var resp = JenkinsService.callService(url)

    var xmlResult = scala.xml.XML.load(resp.get)

    val lastBuild = (xmlResult \\ "mavenModuleSet" \\ "lastBuild" \\ "number").text
    val firstBuild = (xmlResult \\ "mavenModuleSet" \\ "firstBuild" \\ "number").text
    
    resp = JenkinsService.callService(url + "/" + lastBuild)
    xmlResult = scala.xml.XML.load(resp.get)

    val failureTest = (xmlResult \\ "action" \\ "failCount").text
    val skipTest = (xmlResult \\ "action" \\ "skipCount").text
    val totalTest = (xmlResult \\ "action" \\ "totalCount").text
    val user = (xmlResult \\ "action" \\ "cause" \\ "userName").text
    val result = (xmlResult \\ "result").text
    val date = (xmlResult \\ "changeSet" \\ "item" \\ "date").text
    val summary = jobSummary(firstBuild, lastBuild, name)
    
    val value = JobDetail(lastBuild, failureTest, skipTest, totalTest, user, result, date, summary, name)

    Ok(views.html.showDetail.render(value))

  }

  def jobSummary(firstBuild: String, lastJobNumber: String, name: String): String = {
    var sumLastWeekSucess = 0
    var sumLastWeekFailure = 0
    var sumLastMounthSucess = 0
    var sumLastMounthFailure = 0
    var sumSuccess = 0
	var sumFailure = 0
	var success: StringBuilder = new StringBuilder
	var failure: StringBuilder = new StringBuilder
	val usersSucess = new ListBuffer[String]
    val usersFailure = new ListBuffer[String]
	
    val jobRange = firstBuild.toInt until lastJobNumber.toInt + 1
    
    jobRange.foreach(
      x => {
        val xmlResult  = scala.xml.XML.load(JenkinsService.callService(name + "/" + x).get)
        
        (xmlResult \\ "result").text match {
          case "SUCCESS" => { 	sumSuccess = sumSuccess + 1; 
        	  					usersSucess += (xmlResult \\ "action" \\ "cause" \\ "userName").text ; 
        	  					sumLastWeekSucess += calculateDiffDays((xmlResult \\ "id").text, 7); 
        	  					sumLastMounthSucess+= calculateDiffDays((xmlResult \\ "id").text, 30); 
          } 
          case _ => { sumFailure = sumFailure + 1; 
          			  usersFailure += (xmlResult \\ "action" \\ "cause" \\ "userName").text; 
          			  sumLastWeekFailure += calculateDiffDays((xmlResult \\ "id").text, 7) ; 
          			  sumLastMounthFailure+= calculateDiffDays((xmlResult \\ "id").text, 30); 
          }
        }
    })

     usersSucess.filterNot(_.isEmpty()).groupBy(l => l).map(t => (t._1, t._2.length)).toSeq.sortBy(_._2).reverse.foreach((e: (String, Int)) => { 
    	success.append(e._1).append(" = ").append(e._2).append("</br>")
     })  
      
     usersFailure.filterNot(_.isEmpty()).groupBy(l => l).map(t => (t._1, t._2.length)).toSeq.sortBy(_._2).reverse.foreach((e: (String, Int)) => { 
    	 failure.append(e._1).append(" = ").append(e._2).append("</br>")
     }) 
     
     "<b>- TOTAL: " + (sumSuccess + sumFailure) + "</b>" + 
     "</br>- SUCCESS: " + sumSuccess + 
     "</br>- FAILURE: " + sumFailure + 
     "</br></br><b>- TOTAL Last Week: " + (sumLastWeekSucess + sumLastWeekFailure) + "</b>" +
     "</br>- Last week with SUCESS: " + sumLastWeekSucess + 
     "</br>- Last week with FAILURE: " + sumLastWeekFailure + 
     "</br></br><b>- TOTAL Last Mounth: " + (sumLastMounthSucess + sumLastMounthFailure) + "</b>" + 
     "</br>- Last mounth with SUCESS: " + sumLastMounthSucess +
     "</br>- Last mounth with FAILURE: " + sumLastMounthFailure + 
     "</br></br><b>- User/SUCESS: " + "</b>" +  
     "</br>" + success.toString +
     "</br></br><b>- User/FAILURE: " + "</b>" +  
     "</br>" + failure.toString
     
  }

  def calculateDiffDays(day: String, qtdDay: Integer): Integer = {
    val lastWeek = Calendar.getInstance()
    val cal   = Calendar.getInstance()
    var result = 0

    lastWeek.add(Calendar.DAY_OF_MONTH, - qtdDay);
    cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(day));
	
	lastWeek.getTime().before(cal.getTime()) match {
          case true => result = 1
          case _ => result = 0;
    }
	
    result.toInt;
  }
  
  def dashboard = Action {

    val jobs: Seq[String] = Seq("Cortellis-Services-Alert-SEDA-build", 
    							"Cortellis-Services-Export-build", 
    							"Cortellis-Services-Search-build",
    							"Cortellis-Services-Ogre-build",
    							"Cortellis-Services-Retrieve-build")

    var results = Map[String, scala.xml.Elem]()
    jobs.map(job => results += job -> scala.xml.XML.load(JenkinsService.callService(job).get))

    var html: StringBuilder = new StringBuilder

    html.append("<ul>")

    results.foreach {
      keyVal =>
        {
          html.append("<li><h2> <a href ='showDetail/" + keyVal._1 + "'>").append(keyVal._1)

          val currentBuildNumber = (keyVal._2 \\ "lastBuild" \\ "number").text
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
