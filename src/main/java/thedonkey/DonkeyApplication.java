package thedonkey;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import thedonkey.persistence.Account;
import thedonkey.persistence.AccountRepository;
import thedonkey.persistence.Bookmark;
import thedonkey.persistence.BookmarkRepository;
import thedonkey.util.PasswordHash;

/**
 * Initializes and starts the application.
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class DonkeyApplication {
  @Bean
  CommandLineRunner init(AccountRepository accountRepository, BookmarkRepository bookmarkRepository) {
    return (evt) -> Arrays.asList("jhoeller,dsyer,pwebb,ogierke,rwinch,mfisher,mpollack,jlong".split(",")).forEach(
        a -> {
          Account account = accountRepository.save(new Account(a, PasswordHash.computeHash("password")));
          bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/1/" + a, "A description"));
          bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/2/" + a, "A description"));
        });
  }

  // CORS
  @Bean
  FilterRegistrationBean corsFilter(@Value("${tagit.origin:http://localhost:9000}") String origin) {
    return new FilterRegistrationBean(new Filter() {
      @Override
      public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
          ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String method = request.getMethod();
        // this origin value could just as easily have come from a database
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE");
        response.setHeader("Access-Control-Max-Age", Long.toString(60 * 60));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response
            .setHeader(
                "Access-Control-Allow-Headers",
                "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization");
        if ("OPTIONS".equals(method)) {
          response.setStatus(HttpStatus.OK.value());
        } else {
          chain.doFilter(req, res);
        }
      }

      @Override
      public void init(FilterConfig filterConfig) {}

      @Override
      public void destroy() {}
    });
  }

  public static void main(String[] args) {
    SpringApplication.run(DonkeyApplication.class, args);
  }
}
