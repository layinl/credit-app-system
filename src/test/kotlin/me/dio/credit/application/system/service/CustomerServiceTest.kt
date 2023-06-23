package me.dio.credit.application.system.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.service.impl.CustomerService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.util.*


@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class CustomerServiceTest {

  @MockK lateinit var customerRepo: CustomerRepository
  @InjectMockKs lateinit var customerService: CustomerService

  @Test
  fun `should create user`() {
    // given
    val mockedCustomer: Customer = buildCustomer()

    every { customerRepo.save(any()) } returns mockedCustomer

    // when
    val actual: Customer = customerService.save(mockedCustomer)

    // then
    Assertions.assertThat(actual).isNotNull
    Assertions.assertThat(actual).isSameAs(mockedCustomer)
    verify (exactly = 1) { customerRepo.save(mockedCustomer) }
  }

  @Test
  fun `should find customer by id`() {
    // given
    val mockedId: Long = java.util.Random().nextLong()
    val mockedCustomer: Customer = buildCustomer(id = mockedId)

    every { customerRepo.findById(mockedId) } returns Optional.of(mockedCustomer)

    // when
    val actual: Customer = customerService.findById(mockedId)

    // then
    Assertions.assertThat(actual).isNotNull
    Assertions.assertThat(actual).isExactlyInstanceOf(Customer::class.java)
    Assertions.assertThat(actual).isSameAs(mockedCustomer)
    verify(exactly = 1) { customerRepo.findById(mockedId) }
  }

  @Test
  fun `should not find customer by nonexistent id and throw BusinessException`() {
    // given
    val mockedId: Long = java.util.Random().nextLong()

    every { customerRepo.findById(mockedId) } returns Optional.empty()

    // when
    // then
    Assertions.assertThatExceptionOfType(BusinessException::class.java)
      .isThrownBy { customerService.findById(mockedId) }
      .withMessage("Id $mockedId not found")
    verify (exactly = 1) { customerRepo.findById(mockedId) }
  }

  @Test
  fun `should delete customer by id`() {
    // given
    val mockedId: Long = java.util.Random().nextLong()
    val mockedCustomer: Customer = buildCustomer(id = mockedId)

    every { customerRepo.findById(mockedId) } returns Optional.of(mockedCustomer)
    every { customerRepo.delete(mockedCustomer) } just runs

    // when
    customerService.delete(mockedId)

    // then
    verify (exactly = 1) { customerRepo.findById(mockedId) }
    verify (exactly = 1) { customerRepo.delete(mockedCustomer) }

  }

  companion object {
    fun buildCustomer(
      firstName: String = "Layin",
      lastName: String = "Costa",
      cpf: String = "91852114789",
      email: String = "me@layin.net",
      password: String = "12345",
      zipCode: String = "00101",
      street: String = "Neko Street",
      income: BigDecimal = BigDecimal.valueOf(1000.0),
      id: Long = 1L
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
      id = id
    )
  }

}