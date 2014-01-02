package service

import java.io.InputStream

object JenkinsService {
  val host = "https://ci.thomsonreuterslifesciences.com/jenkins/job/"
  val api = "/api/xml"

  def callService(job: String): Option[InputStream] = {
    val conn = connection(host + job + api)
    try {
      Some(conn.getInputStream())
    } catch {
      case e: Exception =>
        error("post:" + scala.io.Source.fromInputStream(conn.getErrorStream).mkString)
        None
    }
  }

  private def connection(host: String): java.net.HttpURLConnection = {
    println(host)
    val url = new java.net.URL(host)
    val conn = url.openConnection.asInstanceOf[java.net.HttpURLConnection]
    conn.setRequestMethod("GET")
    conn.setDoOutput(true)
    conn.setRequestProperty("Content-Type", "application/json")
    conn.getOutputStream.close
    conn
  }

}
