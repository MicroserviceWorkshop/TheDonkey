package thedonkey.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import thedonkey.DonkeyApplication;
import thedonkey.persistence.Account;
import thedonkey.persistence.AccountRepository;
import thedonkey.persistence.Bookmark;
import thedonkey.persistence.BookmarkRepository;

/**
 * Test for the REST service.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DonkeyApplication.class)
@WebAppConfiguration
public class BookmarkRestControllerTest {

  private MediaType contentType = MediaType.APPLICATION_JSON;

  private MockMvc mockMvc;

  private String userName = "bdussault";

  private HttpMessageConverter mappingJackson2HttpMessageConverter;

  private Account account;

  private List<Bookmark> bookmarkList = new ArrayList<>();

  @Autowired
  private BookmarkRepository bookmarkRepository;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  void setConverters(HttpMessageConverter<?>[] converters) {

    this.mappingJackson2HttpMessageConverter =
        Arrays.asList(converters).stream().filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny()
            .get();

    Assert.assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
  }

  @Before
  public void setup() throws Exception {
    this.mockMvc = webAppContextSetup(webApplicationContext).build();

    this.bookmarkRepository.deleteAllInBatch();
    this.accountRepository.deleteAllInBatch();

    this.account = accountRepository.save(new Account(userName, "password"));
    this.bookmarkList.add(bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/1/" + userName,
        "A description")));
    this.bookmarkList.add(bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/2/" + userName,
        "A description")));

    UsernamePasswordAuthenticationToken principal = this.getPrincipal(userName);
    SecurityContextHolder.getContext().setAuthentication(principal);
  }

  @Test
  public void userNotFound() throws Exception {
    UsernamePasswordAuthenticationToken principal = this.getPrincipal("doesNotExist");
    SecurityContextHolder.getContext().setAuthentication(principal);

    mockMvc.perform(post("/bookmarks/").content(this.json(new Bookmark())).contentType(contentType)).andExpect(
        status().isNotFound());
  }

  @Test
  public void readSingleBookmark() throws Exception {
    String basicDigestHeaderValue = "Basic " + Base64.getEncoder().encodeToString(("jhoeller:password").getBytes());
    mockMvc
        .perform(get("/bookmarks/" + this.bookmarkList.get(0).getId()).header("Authorization", basicDigestHeaderValue))
        .andExpect(status().isOk()).andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$.bookmark.id", is(this.bookmarkList.get(0).getId().intValue())))
        .andExpect(jsonPath("$.bookmark.uri", is("http://bookmark.com/1/" + userName)))
        .andExpect(jsonPath("$.bookmark.description", is("A description")))
        .andExpect(jsonPath("$._links.self.href", containsString("/bookmarks/" + this.bookmarkList.get(0).getId())));
  }

  @Test
  public void readBookmarks() throws Exception {

    mockMvc
        .perform(get("/bookmarks"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(contentType))
        .andExpect(jsonPath("$._embedded.bookmarkResourceList", hasSize(2)))
        .andExpect(
            jsonPath("$._embedded.bookmarkResourceList[0].bookmark.id", is(this.bookmarkList.get(0).getId().intValue())))
        .andExpect(
            jsonPath("$._embedded.bookmarkResourceList[0].bookmark.uri", is("http://bookmark.com/1/" + userName)))
        .andExpect(jsonPath("$._embedded.bookmarkResourceList[0].bookmark.description", is("A description")))
        .andExpect(
            jsonPath("$._embedded.bookmarkResourceList[1].bookmark.id", is(this.bookmarkList.get(1).getId().intValue())))
        .andExpect(
            jsonPath("$._embedded.bookmarkResourceList[1].bookmark.uri", is("http://bookmark.com/2/" + userName)))
        .andExpect(jsonPath("$._embedded.bookmarkResourceList[1].bookmark.description", is("A description")));
  }

  @Test
  public void createBookmark() throws Exception {
    String bookmarkJson =
        json(new Bookmark(this.account, "http://spring.io",
            "a bookmark to the best resource for Spring news and information"));
    this.mockMvc.perform(post("/bookmarks").contentType(contentType).content(bookmarkJson)).andExpect(
        status().isCreated());
  }

  protected String json(Object o) throws IOException {
    MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
    this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
    return mockHttpOutputMessage.getBodyAsString();
  }

  protected UsernamePasswordAuthenticationToken getPrincipal(String userName) {

    Collection<? extends GrantedAuthority> authorities = Arrays.asList(() -> "ROLE_ADMIN", () -> "ROLE_BASIC");
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userName, "password", authorities);

    return authentication;
  }
}
