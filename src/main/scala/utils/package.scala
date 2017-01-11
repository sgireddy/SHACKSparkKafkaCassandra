import net.liftweb.json.{DefaultFormats, parse}
import scala.reflect.runtime.universe._
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

package object utils {
  def errorHandler[T](tv: Try[T]) : Option[T] = {
    tv match {
      case Success(v) => Some(v)
      case Failure(ex) => {
        println(s"${ex.getMessage} ${System.lineSeparator()} ${ex.getStackTrace}")
        None
      }
    }
  }

  def tryParse[T](line: String ) (implicit m: TypeTag[T]) : Option[T] = {
    implicit val formats = DefaultFormats
    implicit val cl = ClassTag[T](m.mirror.runtimeClass(m.tpe))
    for {
      jv <- errorHandler(Try(parse(line)))
      v <- errorHandler(Try(jv.extract[T]))
    } yield v
  }
}
