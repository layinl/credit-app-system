package me.dio.credit.application.system.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import jakarta.persistence.*
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.enummeration.Status
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.service.CustomerServiceTest.Companion.buildCustomer
import me.dio.credit.application.system.service.impl.CreditService
import me.dio.credit.application.system.service.impl.CustomerService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*


@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class CreditServiceTest {

  @MockK lateinit var creditRepo: CreditRepository
  @MockK lateinit var customerService: CustomerService
  @InjectMockKs lateinit var creditService: CreditService

  @Test
  fun `should create credit`() {
    // given
    val mockedCustomerId: Long = Random().nextLong()
    val mockedCreditId: Long = Random().nextLong()
    val mockedCustomer: Customer = buildCustomer(id = mockedCustomerId)
    val mockedCredit: Credit = buildCredit(id = mockedCreditId, customer = mockedCustomer)

    every { customerService.findById(mockedCustomerId) } returns mockedCustomer
    every { creditRepo.save(mockedCredit) } returns mockedCredit
    // when
    val actual = creditService.save(mockedCredit)
    // then
    Assertions.assertThat(actual).isNotNull
    Assertions.assertThat(actual).isSameAs(mockedCredit)
    verify(exactly = 1) { customerService.findById(mockedCustomerId) }
    verify(exactly = 1) { creditRepo.save(mockedCredit) }
  }

  @Test
  fun `should find all by customer id`() {
    // given
    val mockedCustomerId: Long = Random().nextLong()
    val mockedCreditId: Long = Random().nextLong()
    val mockedCustomer: Customer = buildCustomer(id = mockedCustomerId)
    val mockedCredit: Credit = buildCredit(id = mockedCreditId, customer = mockedCustomer)
    val mockedCreditList: List<Credit> = listOf(mockedCredit)

    every { creditRepo.findAllByCustomerId(mockedCustomerId) } returns mockedCreditList

    // when
    val actual: List<Credit> = creditService.findAllByCustomer(mockedCustomerId)

    // then
    Assertions.assertThat(actual).isNotNull
    Assertions.assertThat(actual).isExactlyInstanceOf(mockedCreditList::class.java)
    Assertions.assertThat(actual).isSameAs(mockedCreditList)
    verify(exactly = 1) { creditRepo.findAllByCustomerId(mockedCustomerId) }
  }

  @Test
  fun `should find credit by credit code`() {
    // given
    val mockedCustomerId: Long = Random().nextLong()
    val mockedCustomer: Customer = buildCustomer(id = mockedCustomerId)
    val mockedCreditCode: UUID = UUID.randomUUID()
    val mockedCredit: Credit = buildCredit(creditCode = mockedCreditCode, customer = mockedCustomer)

    every { creditRepo.findByCreditCode(mockedCreditCode) } returns mockedCredit

    // when
    val actual: Credit = creditService.findByCreditCode(mockedCustomerId, mockedCreditCode)

    // then
    Assertions.assertThat(actual).isNotNull
    Assertions.assertThat(actual).isExactlyInstanceOf(Credit::class.java)
    Assertions.assertThat(actual).isSameAs(mockedCredit)
    verify(exactly = 1) { creditRepo.findByCreditCode(mockedCreditCode) }
  }

  @Test
  fun `should not find credit by credit code and throw BusinessException`() {
    // given
    val mockedCustomerId: Long = Random().nextLong()
    val mockedCreditCode: UUID = UUID.randomUUID()

    every { creditRepo.findByCreditCode(mockedCreditCode) } returns null
    // when
    // then
    Assertions.assertThatExceptionOfType(BusinessException::class.java)
      .isThrownBy{ creditService.findByCreditCode(mockedCustomerId, mockedCreditCode) }
      .withMessage("Creditcode $mockedCreditCode not found")
    verify(exactly = 1) { creditRepo.findByCreditCode(mockedCreditCode) }
  }

  companion object {
    fun buildCredit(
      creditCode: UUID = UUID.randomUUID(),
      creditValue: BigDecimal = BigDecimal.valueOf(5000.0),
      dayFirstInstallment: LocalDate = LocalDate.now().plusDays(30),
      numberOfInstallments: Int = 24,
      status: Status = Status.IN_PROGRESS,
      customer: Customer? = buildCustomer(id = 1L),
      id: Long? = 1L
    ) = Credit (
      creditCode = creditCode,
      creditValue= creditValue,
      dayFirstInstallment = dayFirstInstallment,
      numberOfInstallments = numberOfInstallments,
      status = status,
      customer = customer,
      id = id
    )
  }

}