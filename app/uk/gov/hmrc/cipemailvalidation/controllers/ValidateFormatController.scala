/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.cipemailvalidation.controllers

import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.cipemailvalidation.model.{EmailAddress, ErrorResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton()
class ValidateFormatController @Inject()(cc: ControllerComponents)
  extends BackendController(cc) {

  def validateFormat(): Action[JsValue] = Action(parse.json).async { implicit request =>
    withJsonBody[EmailAddress] { _ => Future.successful(Ok) }
  }

  override protected def withJsonBody[T](f: T => Future[Result])(implicit request: Request[JsValue], m: Manifest[T], reads: Reads[T]): Future[Result] = {
    Try(request.body.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)
      case Success(JsError(errs)) =>
        val details = errs.map {
          case (jsPath, errors) =>
            jsPath.toJsonString -> errors.map(err => {
              cc.messagesApi(err.messages, err.args: _*)(cc.langs.availables.head)
            }).mkString("; ")
        }.toMap
        Future.successful(BadRequest(Json.toJson(ErrorResponse("VALIDATION_ERROR", "Payload validation failed", Some(details)))))
      case Failure(e) =>
        Future.successful(BadRequest(Json.toJson(ErrorResponse("VALIDATION_ERROR", e.getMessage))))
    }
  }
}
