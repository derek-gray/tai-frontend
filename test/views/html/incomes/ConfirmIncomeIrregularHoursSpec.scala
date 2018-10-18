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

package views.html.incomes

import org.scalatest.mock.MockitoSugar
import play.twirl.api.Html
import uk.gov.hmrc.tai.util.ViewModelHelper.currentTaxYearRangeHtmlNonBreak
import uk.gov.hmrc.tai.util.viewHelpers.TaiViewSpec

class ConfirmIncomeIrregularHoursSpec extends TaiViewSpec with MockitoSugar {

  private val employerName = "employerName"
  private val estimatedAmount = 1000
  private val employmentId = 1

  "Edit income Irregular Hours view" should {
    behave like pageWithBackLink
    behave like pageWithTitle("Confirm")
    behave like pageWithCombinedHeader(
      messages("tai.irregular.confirm.mainHeading", currentTaxYearRangeHtmlNonBreak),
      messages("tai.payPeriod.preHeading", employerName))

    "display the users current estimated income" in {
      doc(view) must haveParagraphWithText(messages("tai.irregular.confirm.estimatedIncome", estimatedAmount))
    }

    "display a message explaining the results of changing the estimated pay" in {
      doc(view) must haveParagraphWithText(messages("tai.irregular.confirm.effectOfChange"))
    }

    "display a confirm and send button" in {
      doc(view) must haveLinkWithText(messages("tai.confirmAndSend"))
    }

    "display a cancel link" in {
      doc(view) must haveLinkWithText(messages("tai.cancel.noSave"))

      val cancelUrl = controllers.routes.IncomeSourceSummaryController.onPageLoad(employmentId).url.toString
      doc(view) must haveCancelLinkWithUrl(cancelUrl)
    }
  }


  override def view: Html = views.html.incomes.confirmIncomeIrregularHours("lol")
}
