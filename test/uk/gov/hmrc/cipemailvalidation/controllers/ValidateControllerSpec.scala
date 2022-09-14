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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.{Json, OWrites}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import uk.gov.hmrc.cipemailvalidation.model.EmailAddress

import scala.util.Random

class ValidateControllerSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  private val fakeRequest = FakeRequest()
  private lazy val controller = app.injector.instanceOf[ValidateController]
  private implicit val writes: OWrites[EmailAddress] = Json.writes[EmailAddress]

  "POST /" should {
    "return 200 with valid email address" in {
      val result = controller.validate()(
        fakeRequest.withBody(Json.toJson(EmailAddress("test@test.com"))))
      status(result) shouldBe OK
    }

    "return 400 with email with no @" in {
      val result = controller.validate()(
        fakeRequest.withBody(Json.toJson(EmailAddress("invalid.email"))))
      status(result) shouldBe BAD_REQUEST
      (contentAsJson(result) \ "message" ).as[String] shouldBe "Enter a valid email address"
    }

    "return 400 with email address too long" in {
      val local = s"${Random.alphanumeric.take(248).mkString}"
      val domain = "test"
      val topLevelDomain = "com"
      val email = s"${local}@${domain}.${topLevelDomain}"
      val result = controller.validate()(
        fakeRequest.withBody(Json.toJson(EmailAddress(email))))
      status(result) shouldBe BAD_REQUEST
      (contentAsJson(result) \ "message" ).as[String] shouldBe "Enter a valid email address"
    }

    "return 400 with email address with spaces" in {
      val result = controller.validate()(
        fakeRequest.withBody(Json.toJson(EmailAddress("invalid email"))))
      status(result) shouldBe BAD_REQUEST
      (contentAsJson(result) \ "message" ).as[String] shouldBe "Enter a valid email address"
    }

    "return 400 with blank email" in {
      val result = controller.validate()(
        fakeRequest.withBody(Json.toJson(EmailAddress(""))))
      status(result) shouldBe BAD_REQUEST
      (contentAsJson(result) \ "message" ).as[String] shouldBe "Enter a valid email address"
    }

    "return 400 with blank email with spaces" in {
      val result = controller.validate()(
        fakeRequest.withBody(Json.toJson(EmailAddress(" "))))
      status(result) shouldBe BAD_REQUEST
      (contentAsJson(result) \ "message" ).as[String] shouldBe "Enter a valid email address"
    }
  }
}
