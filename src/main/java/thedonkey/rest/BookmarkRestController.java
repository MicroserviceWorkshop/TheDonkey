package thedonkey.rest;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import thedonkey.persistence.AccountRepository;
import thedonkey.persistence.Bookmark;
import thedonkey.persistence.BookmarkRepository;

/**
 * The REST service for the bookmarks.
 */
@RestController
@RequestMapping("/bookmarks")
public class BookmarkRestController {

  private final BookmarkRepository bookmarkRepository;

  private final AccountRepository accountRepository;

  @Autowired
  public BookmarkRestController(BookmarkRepository bookmarkRepository, AccountRepository accountRepository) {
    this.bookmarkRepository = bookmarkRepository;
    this.accountRepository = accountRepository;
  }

  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<?> add(@RequestBody Bookmark input) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    this.validateUser(userId);
    return this.accountRepository
        .findByUsername(userId)
        .map(
            account -> {
              Bookmark result = bookmarkRepository.save(new Bookmark(account, input.uri, input.description));

              HttpHeaders httpHeaders = new HttpHeaders();
              httpHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                  .buildAndExpand(result.getId()).toUri());
              return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
            }).get();
  }

  @RequestMapping(value = "/{bookmarkId}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
  public BookmarkResource readBookmark(@PathVariable Long bookmarkId) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    this.validateUser(userId);
    return new BookmarkResource(this.bookmarkRepository.findOne(bookmarkId));
  }


  @RequestMapping(method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
  @RolesAllowed(value = "ROLE_ADMIN")
  public Resources<BookmarkResource> readBookmarks() {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    this.validateUser(userId);

    List<BookmarkResource> bookmarkResourceList =
        bookmarkRepository.findByAccountUsername(userId).stream().map(BookmarkResource::new)
            .collect(Collectors.toList());
    return new Resources<BookmarkResource>(bookmarkResourceList);
  }

  private void validateUser(String userId) {
    this.accountRepository.findByUsername(userId).orElseThrow(() -> new UsernameNotFoundException(userId));
  }
}
