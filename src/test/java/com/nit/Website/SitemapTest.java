package com.nit.Website;

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
class SitemapTest {


@Autowired
private MockMvc mockMvc;

@Test
void shouldLoadSitemapIndex()
        throws Exception {

    mockMvc.perform(
            get("/sitemap.xml")
    )
    .andExpect(
            status().isOk()
    )
    .andExpect(
            content().contentTypeCompatibleWith(
                    "application/xml"
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "<sitemapindex"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "website-sitemap.xml"
                    )
            )
    );
}

@Test
void shouldLoadWebsiteSitemap()
        throws Exception {

    mockMvc.perform(
            get("/website-sitemap.xml")
    )
    .andExpect(
            status().isOk()
    )
    .andExpect(
            content().contentTypeCompatibleWith(
                    "application/xml"
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "<urlset"
                    )
            )
    )
    .andExpect(
            content().string(
                    org.hamcrest.Matchers.containsString(
                            "/index.html"
                    )
            )
    );
}
}
