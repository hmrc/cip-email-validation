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

package uk.gov.hmrc.cipemailvalidation.model

import play.api.libs.functional.syntax.toApplicativeOps
import play.api.libs.json.{JsPath, Reads}

case class EmailAddress(email: String)

object EmailAddress {
  val MAX_LENGTH = 256

  implicit val locationReads: Reads[EmailAddress] =
    (JsPath \ "email").read[String](Reads.email.keepAnd(Reads.maxLength[String](MAX_LENGTH))).map(EmailAddress.apply)
}