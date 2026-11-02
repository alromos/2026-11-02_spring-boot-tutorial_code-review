package com.tutorial.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tutorial.application.port.in.CreateCustomerUseCase;
import com.tutorial.application.port.in.DeleteCustomerUseCase;
import com.tutorial.application.port.in.GetCustomerUseCase;
import com.tutorial.application.port.in.UpdateCustomerUseCase;
import com.tutorial.domain.Customer;
import com.tutorial.domain.CustomerNotFoundException;
import com.tutorial.domain.PageQuery;
import com.tutorial.domain.PageResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-layer slice test: only the controller, JSON mapping, validation and the exception
 * handler are loaded. The use cases are mocks, so no database is involved.
 * (Role rules and JWT are covered by SecurityTest, which runs the full application.)
 */
@WebMvcTest(CustomerController.class)
class CustomerControllerWebMvcTest {

    private static final String JSON = "{\"name\":\"%s\",\"email\":\"%s\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateCustomerUseCase createCustomer;
    @MockitoBean
    private GetCustomerUseCase getCustomer;
    @MockitoBean
    private UpdateCustomerUseCase updateCustomer;
    @MockitoBean
    private DeleteCustomerUseCase deleteCustomer;

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        when(createCustomer.create("Ann", "ann@example.com"))
                .thenReturn(new Customer(7L, "Ann", "ann@example.com"));

        mockMvc.perform(post("/api/customers").with(user("u")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JSON.formatted("Ann", "ann@example.com")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/customers/7")))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Ann"));
    }

    @Test
    void createWithInvalidBodyReturns400AndDoesNotCallUseCase() throws Exception {
        mockMvc.perform(post("/api/customers").with(user("u")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JSON.formatted("", "not-an-email")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createCustomer);
    }

    @Test
    void getByIdReturnsCustomer() throws Exception {
        when(getCustomer.getById(1L)).thenReturn(new Customer(1L, "Ann", "ann@example.com"));

        mockMvc.perform(get("/api/customers/1").with(user("u")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ann@example.com"));
    }

    @Test
    void getByIdReturns404ProblemWhenNotFound() throws Exception {
        when(getCustomer.getById(99L)).thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(get("/api/customers/99").with(user("u")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Customer with id 99 not found"));
    }

    @Test
    void listUsesDefaultPagingAndMapsPageResponse() throws Exception {
        when(getCustomer.getAll(new PageQuery(0, 20))).thenReturn(
                new PageResult<>(List.of(new Customer(1L, "Ann", "ann@example.com")), 0, 20, 1, 1));

        mockMvc.perform(get("/api/customers").with(user("u")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Ann"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void listPassesRequestedPageAndSizeToUseCase() throws Exception {
        when(getCustomer.getAll(new PageQuery(2, 5))).thenReturn(new PageResult<>(List.of(), 2, 5, 0, 0));

        mockMvc.perform(get("/api/customers").param("page", "2").param("size", "5").with(user("u")))
                .andExpect(status().isOk());

        verify(getCustomer).getAll(new PageQuery(2, 5));
    }

    @Test
    void listRejectsOutOfRangePagingParameters() throws Exception {
        mockMvc.perform(get("/api/customers").param("size", "101").with(user("u"))).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/customers").param("page", "-1").with(user("u"))).andExpect(status().isBadRequest());

        verify(getCustomer, never()).getAll(any());
    }

    @Test
    void updateReturnsUpdatedCustomer() throws Exception {
        when(updateCustomer.update(eq(1L), eq("Anna"), eq("anna@example.com")))
                .thenReturn(new Customer(1L, "Anna", "anna@example.com"));

        mockMvc.perform(put("/api/customers/1").with(user("u")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(JSON.formatted("Anna", "anna@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anna"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/customers/1").with(user("u")).with(csrf()))
                .andExpect(status().isNoContent());

        verify(deleteCustomer).delete(1L);
    }

    @Test
    void deleteReturns404WhenNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new CustomerNotFoundException(99L)).when(deleteCustomer).delete(99L);

        mockMvc.perform(delete("/api/customers/99").with(user("u")).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
