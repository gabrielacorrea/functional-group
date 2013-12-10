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

  def index = Action {

    val resp = JenkinsService.callService("Cortellis-Services-Alert-SEDA-build")
    
    val xml = scala.xml.XML.load(resp.get)  
    
    Ok(views.html.index.render(xml.toString()))
  }

  def dashboard = Action {

    val resp = JenkinsService.callService("Cortellis-Services-Alert-SEDA-build")

    val xml = scala.xml.XML.load(resp.get)

    Ok(views.html.index.render(xml.toString()))
  }

}