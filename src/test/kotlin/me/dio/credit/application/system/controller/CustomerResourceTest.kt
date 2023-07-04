package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.CustomerDto
import me.dio.credit.application.system.dto.CustomerUpdateDto
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CustomerResourceTest {
  @Autowired private lateinit var customerRepo: CustomerRepository
  @Autowired private lateinit var mockMVC: MockMvc
  @Autowired private lateinit var objectMapper: ObjectMapper

  companion object {
    const val URL: String = "/api/customers"
  }

  @BeforeEach fun setup() = customerRepo.deleteAll()

  @AfterEach fun tearDown() = customerRepo.deleteAll()

  @Test
  fun `should create a customer and return 201-CREATED status`() {
    // given
    val customerDto: CustomerDto = buildCustomerDTO()
    val valueAsString: String = objectMapper.writeValueAsString(customerDto)
    // when
    // then
    mockMVC.perform((MockMvcRequestBuilders.post(URL))
      .contentType(MediaType.APPLICATION_JSON)
      .content(valueAsString)
    ).andExpect(MockMvcResultMatchers.status().isCreated)
      .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Layin"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Costa"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("91852114789"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("me@layin.net"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("00101"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Neko Street"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.income").value("1000.0"))
      .andDo(MockMvcResultHandlers.print())
  }

  @Test
  fun `should not create a customer with the same CPF and return 409-Conflict status`() {
    // given
    val customerDto: CustomerDto = buildCustomerDTO()
    val valueAsString: String = objectMapper.writeValueAsString(customerDto)
    customerRepo.save(customerDto.toEntity())
    // when
    // then
    mockMVC.perform((MockMvcRequestBuilders.post(URL))
      .contentType(MediaType.APPLICATION_JSON)
      .content(valueAsString)
    ).andExpect(MockMvcResultMatchers.status().isConflict)
      .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Conflict! Consult the documentation"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
      .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(409))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$.exception")
          .value("class org.springframework.dao.DataIntegrityViolationException")
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
      .andDo(MockMvcResultHandlers.print())

  }

  @Test
  fun `should not create a customer with an empty firstName field and return 400-BadRequest status`() {
    // given
    val customerDto: CustomerDto = buildCustomerDTO(firstName = "")
    val valueAsString: String = objectMapper.writeValueAsString(customerDto)
    // when
    // then
    mockMVC.perform((MockMvcRequestBuilders.post(URL))
      .contentType(MediaType.APPLICATION_JSON)
      .content(valueAsString)
    ).andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
      .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$.exception")
          .value("class org.springframework.web.bind.MethodArgumentNotValidException")
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
      .andDo(MockMvcResultHandlers.print())
  }

  @Test
  fun  `should find customer by ID and return 200-Ok status`() {
    // given
    val customer = customerRepo.save(buildCustomerDTO().toEntity())
    // when
    // then
    mockMVC.perform((MockMvcRequestBuilders.get("$URL/${customer.id}"))
      .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Layin"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Costa"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.cpf").value("91852114789"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("me@layin.net"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("00101"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Neko Street"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.income").value("1000.0"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1))
      .andDo(MockMvcResultHandlers.print())
  }

  @Test
  fun `should not find customer by ID and return 400-BadRequest status`() {
    // given
    val invalidID = 2L
    // when
    // then
    mockMVC.perform((MockMvcRequestBuilders.get("$URL/$invalidID"))
      .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
      .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$.exception")
          .value("class me.dio.credit.application.system.exception.BusinessException")
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
      .andDo(MockMvcResultHandlers.print())
  }

  @Test
  fun `should delete customer by ID`() {
    // given
    val customer: Customer = customerRepo.save(buildCustomerDTO().toEntity())
    // when
    // then
    mockMVC.perform((MockMvcRequestBuilders.delete("$URL/${customer.id}"))
      .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(MockMvcResultMatchers.status().isNoContent)
      .andDo(MockMvcResultHandlers.print())
  }

  @Test
  fun `should not delete customer by ID and return 400-BadRequest status`() {
    // given
    val invalidID = 10L
    // when
    // then
    mockMVC.perform((MockMvcRequestBuilders.delete("$URL/$invalidID"))
      .contentType(MediaType.APPLICATION_JSON)
    ).andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
      .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$.exception")
          .value("class me.dio.credit.application.system.exception.BusinessException")
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
      .andDo(MockMvcResultHandlers.print())
  }

  @Test
  fun `should update customer by id and return 200-Ok status`() {
    // given
    val customer: Customer = customerRepo.save(buildCustomerDTO().toEntity())
    val customerUpdateDto: CustomerUpdateDto = buildCustomerUpdateDTO()
    val valueAsString: String = objectMapper.writeValueAsString(customerUpdateDto)
    // when
    // then
    mockMVC.perform((MockMvcRequestBuilders.patch("$URL?customerId=${customer.id}"))
      .contentType(MediaType.APPLICATION_JSON)
      .content(valueAsString)
    ).andExpect(MockMvcResultMatchers.status().isOk)
      .andExpect(MockMvcResultMatchers.jsonPath("$.firstName").value("Aliny"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.lastName").value("Costta"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.zipCode").value("857452"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.street").value("Inu Street"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.income").value("5000.0"))
      .andDo(MockMvcResultHandlers.print())
  }

  @Test
  fun `should not update customer by id and return 400-BadRequest status`() {
    // given
    val invalidId: Long = 22L
    val customerUpdateDto: CustomerUpdateDto = buildCustomerUpdateDTO()
    val valueAsString: String = objectMapper.writeValueAsString(customerUpdateDto)
    // when
    // then
    mockMVC.perform((MockMvcRequestBuilders.patch("$URL?customerId=${invalidId}"))
      .contentType(MediaType.APPLICATION_JSON)
      .content(valueAsString)
    ).andExpect(MockMvcResultMatchers.status().isBadRequest)
      .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
      .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
      .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
      .andExpect(
        MockMvcResultMatchers.jsonPath("$.exception")
          .value("class me.dio.credit.application.system.exception.BusinessException")
      )
      .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
      .andDo(MockMvcResultHandlers.print())
  }

  private fun buildCustomerDTO(
    firstName: String = "Layin",
    lastName: String = "Costa",
    cpf: String = "91852114789",
    email: String = "me@layin.net",
    password: String = "12345",
    zipCode: String = "00101",
    street: String = "Neko Street",
    income: BigDecimal = BigDecimal.valueOf(1000.0)
  ) = CustomerDto (
    firstName = firstName,
    lastName = lastName,
    cpf = cpf,
    email = email,
    password = password,
    zipCode = zipCode,
    street = street,
    income = income
  )

  private fun buildCustomerUpdateDTO(
    firstName: String = "Aliny",
    lastName: String = "Costta",
    zipCode: String = "857452",
    street: String = "Inu Street",
    income: BigDecimal = BigDecimal.valueOf(5000.0)
  ) = CustomerUpdateDto (
    firstName = firstName,
    lastName = lastName,
    zipCode = zipCode,
    street = street,
    income = income
  )

}