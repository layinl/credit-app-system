package me.dio.credit.application.system.repository

import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CreditRepositoryTest {
  @Autowired lateinit var creditRepo: CreditRepository
  @Autowired lateinit var testEntityManager: TestEntityManager
  private lateinit var customer: Customer
  private lateinit var credit1: Credit
  private lateinit var credit2: Credit

  @BeforeEach fun setup() {
    customer = testEntityManager.persist(buildCustomer())
    credit1 = testEntityManager.persist(buildCredit(customer = customer))
    credit2 = testEntityManager.persist(buildCredit(customer = customer))
  }

  @Test
  fun `should find credit by credit code`() {
    // given
    val creditCode1 = UUID.fromString("4b7a4d62-7b0c-4760-9f00-fb22fad9aa71")
    val creditCode2 = UUID.fromString("9cede36a-de95-4ecd-84c4-3df6df9c523d")
    credit1.creditCode = creditCode1
    credit2.creditCode = creditCode2

    // when
    val fakeCredit1: Credit = creditRepo.findByCreditCode(creditCode1)!!
    val fakeCredit2: Credit = creditRepo.findByCreditCode(creditCode2)!!

    // then
    Assertions.assertThat(fakeCredit1).isNotNull
    Assertions.assertThat(fakeCredit2).isNotNull
    Assertions.assertThat(fakeCredit1).isSameAs(credit1)
    Assertions.assertThat(fakeCredit2).isSameAs(credit2)
  }

  @Test
  fun `should find all credits by customer id`() {
    // given
    val customerId: Long = 1L
    // when
    val creditList: List<Credit> = creditRepo.findAllByCustomerId(customerId)
    // then
    Assertions.assertThat(creditList).isNotEmpty
    Assertions.assertThat(creditList.size).isEqualTo(2)
    Assertions.assertThat(creditList).contains(credit1, credit2)
  }

  private fun buildCredit(
    creditValue: BigDecimal = BigDecimal.valueOf(5000.0),
    dayFirstInstallment: LocalDate = LocalDate.now().plusDays(30),
    numberOfInstallments: Int = 24,
    customer: Customer
  ) = Credit (
    creditValue = creditValue,
    dayFirstInstallment = dayFirstInstallment,
    numberOfInstallments = numberOfInstallments,
    customer = customer
  )

  private fun buildCustomer(
    firstName: String = "Layin",
    lastName: String = "Costa",
    cpf: String = "91852114789",
    email: String = "me@layin.net",
    password: String = "12345",
    zipCode: String = "00101",
    street: String = "Neko Street",
    income: BigDecimal = BigDecimal.valueOf(1000.0),
  ) = Customer(
    firstName = firstName,
    lastName = lastName,
    cpf = cpf,
    email = email,
    password = password,
    address = Address(
      zipCode = zipCode,
      street = street,
    ),
    income = income,
  )
}