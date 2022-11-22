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

import org.mockito.IdiomaticMockito
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.cipemailvalidation.metrics.MetricsService
import uk.gov.hmrc.cipemailvalidation.model.Email
import uk.gov.hmrc.internalauth.client.Predicate.Permission
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.internalauth.client.test.{BackendAuthComponentsStub, StubBehaviour}

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future
import scala.util.Random

class ValidateControllerSpec extends AnyWordSpec
  with Matchers
  with IdiomaticMockito {
  "validate" should {
    "return 200 with valid email address" in new SetUp {
      private val result = controller.validate()(
        fakeRequest.withBody(Json.toJson(Email("test@test.com"))))
      mockMetricsService wasNever called
      status(result) shouldBe OK
    }

    "return 400 with email with no @" in new SetUp {
      private val result = controller.validate()(
        fakeRequest.withBody(Json.toJson(Email("invalid.email"))))
      mockMetricsService wasNever called
      status(result) shouldBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] shouldBe "Enter a valid email"
    }

    "return 400 with email address too long" in new SetUp {
      private val local = s"${Random.alphanumeric.take(248).mkString}"
      private val domain = "test"
      private val topLevelDomain = "com"
      private val email = s"${local}@${domain}.${topLevelDomain}"
      private val result = controller.validate()(
        fakeRequest.withBody(Json.toJson(Email(email))))
      status(result) shouldBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] shouldBe "Enter a valid email"
    }

    "return 400 with email address with spaces" in new SetUp {
      private val result = controller.validate()(
        fakeRequest.withBody(Json.toJson(Email("invalid email"))))
      mockMetricsService wasNever called
      status(result) shouldBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] shouldBe "Enter a valid email"
    }

    "return 400 with blank email" in new SetUp {
      private val result = controller.validate()(
        fakeRequest.withBody(Json.toJson(Email(""))))
      mockMetricsService.recordMetric("email_validation_failure") was called
      status(result) shouldBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] shouldBe "Enter a valid email"
    }

    "return 400 with blank email with spaces" in new SetUp {
      private val result = controller.validate()(
        fakeRequest.withBody(Json.toJson(Email(" "))))
      mockMetricsService.recordMetric("email_validation_failure") was called
      status(result) shouldBe BAD_REQUEST
      (contentAsJson(result) \ "message").as[String] shouldBe "Enter a valid email"
    }
  }

  trait SetUp {
    protected val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("Authorization" -> "fake-token")
    private val expectedPredicate = {
      Permission(Resource(ResourceType("cip-email-validation"), ResourceLocation("*")), IAAction("*"))
    }
    private val mockStubBehaviour = mock[StubBehaviour]
    val mockMetricsService = mock[MetricsService]
    mockStubBehaviour.stubAuth(Some(expectedPredicate), Retrieval.EmptyRetrieval).returns(Future.unit)
    private val backendAuthComponentsStub = BackendAuthComponentsStub(mockStubBehaviour)(Helpers.stubControllerComponents(), Implicits.global)
    protected lazy val controller = new ValidateController(Helpers.stubControllerComponents(), backendAuthComponentsStub, mockMetricsService)
    protected implicit val writes: OWrites[Email] = Json.writes[Email]
  }
}
