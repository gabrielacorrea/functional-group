package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import play.api.Logger

object Application extends Controller {
  def index = Action {  
    Ok(views.html.index.render(parseJson()))
  }
  
  
  def parseJson () : String = {


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
}}