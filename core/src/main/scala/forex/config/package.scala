package forex

import org.http4s.Uri
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

package object config {

  implicit val urlConfigReader: ConfigReader[Uri] = ConfigReader[String].emap { urlAsString =>
    Uri.fromString(urlAsString).left.map { failure =>
      CannotConvert(value = urlAsString, toType = "Uri", because = failure.sanitized)
    }
  }

}
