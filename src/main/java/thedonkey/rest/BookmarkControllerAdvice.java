package thedonkey.rest;

import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Maps the {@link UsernameNotFoundException} to a {@link VndErrors} object.
 */
@ControllerAdvice
public class BookmarkControllerAdvice {

  @ResponseBody
  @ExceptionHandler(UsernameNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  VndErrors userNotFoundExceptionHandler(UsernameNotFoundException ex) {
    return new VndErrors("error", ex.getMessage());
  }
}
