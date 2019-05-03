package forex.http.rates

import forex.domain.Currency
import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

object QueryParams {

  implicit private[http] val currencyQueryParam: QueryParamDecoder[Currency] =
    QueryParamDecoder[String].map(Currency.withName)

  object FromQueryParam extends OptionalQueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends OptionalQueryParamDecoderMatcher[Currency]("to")

}
