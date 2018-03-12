/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.tai.connectors

import play.Logger
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json, Writes}
import uk.gov.hmrc.http.{CoreDelete, CoreGet, CorePost, CorePut}
import uk.gov.hmrc.play.http._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpException, HttpReads, HttpResponse, InternalServerException, LockedException, NotFoundException}
import uk.gov.hmrc.tai.config.WSHttp

trait HttpHandler {

  def http: CoreGet with CorePut with CorePost with CoreDelete

  def getFromApi(url: String)(implicit hc: HeaderCarrier): Future[JsValue] = {

    val futureResponse = http.GET[HttpResponse](url)

    futureResponse.flatMap {
      httpResponse =>
        httpResponse.status match {

          case OK =>
            Future.successful(httpResponse.json)

          case NOT_FOUND =>
            Logger.warn(s"HttpHandler - No data can be found")
            Future.failed(new NotFoundException(httpResponse.body))

          case INTERNAL_SERVER_ERROR =>
            Logger.warn(s"HttpHandler - Internal Server Error received")
            Future.failed(new InternalServerException(httpResponse.body))

          case BAD_REQUEST =>
            Logger.warn(s"HttpHandler - Bad Request received")
            Future.failed(new BadRequestException(httpResponse.body))

          case LOCKED =>
            Logger.warn(s"HttpHandler - Locked received")
            Future.failed(new LockedException(httpResponse.body))

          case _ =>
            Logger.warn(s"HttpHandler - Server error received")
            Future.failed(new HttpException(httpResponse.body, httpResponse.status))
        }
    }
  }

  def putToApi[I](url: String, data: I)(implicit hc: HeaderCarrier, rds: HttpReads[I], writes: Writes[I]): Future[HttpResponse] = {
    http.PUT[I, HttpResponse](url, data).flatMap { httpResponse =>
      httpResponse.status match {

        case OK =>
          Future.successful(httpResponse)

        case NOT_FOUND =>
          Logger.warn(s"HttpHandler - No data can be found")
          Future.failed(new NotFoundException(httpResponse.body))

        case INTERNAL_SERVER_ERROR =>
          Logger.warn(s"HttpHandler - Internal Server Error received")
          Future.failed(new InternalServerException(httpResponse.body))

        case BAD_REQUEST =>
          Logger.warn(s"HttpHandler - Bad Request received")
          Future.failed(new BadRequestException(httpResponse.body))

        case _ =>
          Logger.warn(s"HttpHandler - Server error received")
          Future.failed(new HttpException(httpResponse.body, httpResponse.status))
      }
    }
  }

  def postToApi[I](url: String, data: I)(implicit hc: HeaderCarrier, rds: HttpReads[I], writes: Writes[I]): Future[HttpResponse] = {
    http.POST[I, HttpResponse](url, data) flatMap { httpResponse =>
      httpResponse status match {
        case OK | CREATED =>
          Future.successful(httpResponse)

        case _ =>
          Logger.warn(s"HttpHandler - Error received with status: ${httpResponse.status} and body: ${httpResponse.body}")
          Future.failed(new HttpException(httpResponse.body, httpResponse.status))
      }
    }
  }

  def deleteFromApi(url: String)(implicit hc: HeaderCarrier, rds: HttpReads[HttpResponse]): Future[HttpResponse] = {
    http.DELETE[HttpResponse](url) flatMap { httpResponse =>
      httpResponse status match {
        case OK | NO_CONTENT | ACCEPTED =>
          Future.successful(httpResponse)

        case _ =>
          Logger.warn(s"HttpHandler - Error received with status: ${httpResponse.status} and body: ${httpResponse.body}")
          Future.failed(new HttpException(httpResponse.body, httpResponse.status))
      }
    }
  }

}

object HttpHandler extends HttpHandler {
  override val http = WSHttp
}
