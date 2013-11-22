import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.mvc.Controller
import controllers._

import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class ApplicationTest extends Specification {
    
    val app = Application.parseJson()
    
    app must endWith("toto2")
    
}



