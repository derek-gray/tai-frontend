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

package controllers

import builders.{AuthBuilder, RequestBuilder}
import mocks.{MockPartialRetriever, MockTemplateRenderer}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.frontend.auth.connectors.{AuthConnector, DelegationConnector}
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.renderer.TemplateRenderer
import uk.gov.hmrc.tai.connectors.responses.TaiSuccessResponseWithPayload
import uk.gov.hmrc.tai.model.domain.tax.{IncomeCategory, NonSavingsIncomeCategory, TaxBand, TotalTax}
import uk.gov.hmrc.tai.service._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UnderPaymentFromPreviousYearControllerSpec extends PlaySpec
  with OneAppPerSuite
  with MockitoSugar
  with FakeTaiPlayApplication {

  override lazy val app = new GuiceApplicationBuilder().build()
  def injector = app.injector

  val nino = new Generator().nextNino

  val taxBand = TaxBand("B", "BR", 16500, 1000, Some(0), Some(16500), 20)
  val incomeCatergories = IncomeCategory(NonSavingsIncomeCategory, 1000, 5000, 16500, Seq(taxBand))
  val totalTax : TotalTax = TotalTax(1000, Seq(incomeCatergories), None, None, None)

  "UnderPaymentFromPreviousYearController" should {
    "respond with OK" when {
      "underpaymentExplanation is called" in {
        val controller = new SUT
        val result = controller.underpaymentExplanation()(RequestBuilder.buildFakeRequestWithAuth("GET"))
        status(result) mustBe OK
        contentAsString(result) must include("What is a previous year underpayment?")
      }
    }

    "respond with UNAUTHORIZED and redirect to unauthorised page" when {
      "not authorized" in {
        val controller = new SUT
        val res = controller.underpaymentExplanation()(FakeRequest(method = "GET", path = ""))

        status(res) mustEqual SEE_OTHER
        redirectLocation(res).get must include("/gg/sign-in")
      }
    }
  }

  private class SUT() extends UnderpaymentFromPreviousYearController {
    override val personService: PersonService = mock[PersonService]
    override val auditService: AuditService = mock[AuditService]
    override val employmentService: EmploymentService = mock[EmploymentService]
    override val taxAccountService: TaxAccountService = mock[TaxAccountService]
    override val codingComponentService: CodingComponentService = mock[CodingComponentService]
    override val auditConnector: AuditConnector = mock[AuditConnector]
    override val authConnector: AuthConnector = mock[AuthConnector]
    override val delegationConnector: DelegationConnector = mock[DelegationConnector]

    override implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer
    override implicit val partialRetriever: FormPartialRetriever = MockPartialRetriever

    when(authConnector.currentAuthority(any(), any())).thenReturn(AuthBuilder.createFakeAuthData(nino))
    when(personService.personDetails(any())(any())).thenReturn(Future.successful(fakePerson(nino)))
    when(employmentService.employments(any(), any())(any())).thenReturn(Future.successful(Seq.empty))
    when(taxAccountService.totalTax(any(), any())(any())).thenReturn(Future(TaiSuccessResponseWithPayload(totalTax)))
    when(codingComponentService.taxFreeAmountComponents(any(), any())(any())).thenReturn(Future.successful(Seq.empty))

  }

}
