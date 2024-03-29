/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.Logging
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import uk.gov.hmrc.cipemailvalidation.metrics.MetricsService
import uk.gov.hmrc.cipemailvalidation.model.ErrorResponse.Codes.VALIDATION_ERROR
import uk.gov.hmrc.cipemailvalidation.model.ErrorResponse.Messages.INVALID_EMAIL
import uk.gov.hmrc.cipemailvalidation.model.{Email, ErrorResponse}
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton()
class ValidateController @Inject()(cc: ControllerComponents, auth: BackendAuthComponents, metricsService: MetricsService)
  extends BackendController(cc) with Logging {

  val permission: Predicate.Permission = Predicate.Permission(Resource(
    ResourceType("cip-email-validation"),
    ResourceLocation("*")),
    IAAction("*"))

  def validate(): Action[JsValue] = auth.authorizedAction[Unit](permission).compose(Action(parse.json)).async {
    implicit request =>
      withJsonBody[Email] { _ => Future.successful(Ok(request.body)) }
  }

  override protected def withJsonBody[T](f: T => Future[Result])
                                        (implicit request: Request[JsValue], m: Manifest[T], reads: Reads[T]): Future[Result] = {
    Try(request.body.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)
      case Success(_) | Failure(_) =>
        metricsService.recordMetric("email_validation_failure")
        Future.successful(BadRequest(Json.toJson(ErrorResponse(VALIDATION_ERROR, INVALID_EMAIL))))
    }
  }
}
