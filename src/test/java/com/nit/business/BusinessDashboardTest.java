package com.nit.business;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BusinessDashboardTest {


@Autowired
private MockMvc mockMvc;

@Test
void shouldLoadBusinessDashboard()
        throws Exception {

    mockMvc.perform(
            get("/business-dashboard.html")
    )
    .andExpect(
            status().isOk()
    );
}

@Test
void shouldContainDashboardElements()
        throws Exception {

    mockMvc.perform(
            get("/business-dashboard.html")
    )
    .andExpect(
            status().isOk()
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "Business Dashboard"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "Average Rating"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "Total Reviews"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "Response Rate"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "Rating Trend"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "Customer Reviews"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "js/business-dashboard.js"
                    )
            )
    );
}


}
