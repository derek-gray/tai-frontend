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

package controllers.auth

import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, PayeAccount}
import org.mockito.Mockito._

class TaiRegimeSpec extends PlaySpec with MockitoSugar {

  "isAuthorised" should {
    "return true" when {
      "paye is defined" in  {
        when(mockAccount.paye).thenReturn(Some(mockPayeAccount))
        TaiRegime.isAuthorised(mockAccount) mustBe true
      }
    }

    "return false" when {
      " aye is not defined" in {
        when(mockAccount.paye).thenReturn(None)
        TaiRegime.isAuthorised(mockAccount) mustBe false
      }
    }
  }

  val mockAccount: Accounts = mock[Accounts]
  val mockPayeAccount: PayeAccount = mock[PayeAccount]
}
