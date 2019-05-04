package forex.services.rates.interpreters.live

import forex.domain.Pair
import org.http4s.{ QueryParamEncoder, QueryParameterValue }

object QueryParams {

  implicit def setQueryParam[T](implicit encoder: QueryParamEncoder[T]): QueryParamEncoder[Set[T]] =
    new QueryParamEncoder[Set[T]] {
      override def encode(set: Set[T]): QueryParameterValue =
        QueryParameterValue(set.map(encoder.encode(_).value).mkString(","))
    }

  implicit val pairQueryParam: QueryParamEncoder[Pair] =
    new QueryParamEncoder[Pair] {
      override def encode(pair: Pair): QueryParameterValue =
        QueryParameterValue(s"${pair.from.entryName}${pair.to.entryName}")
    }

}
