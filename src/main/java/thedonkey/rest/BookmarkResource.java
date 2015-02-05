package thedonkey.rest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import thedonkey.persistence.Bookmark;

/**
 * Wraps a {@link Bookmark} into a HATEOAS conform representation.
 */
public class BookmarkResource extends ResourceSupport {

  private final Bookmark bookmark;

  public BookmarkResource(Bookmark bookmark) {
    this.bookmark = bookmark;
    this.add(new Link(bookmark.getUri(), "bookmark-uri"));
    this.add(linkTo(BookmarkRestController.class).withRel("bookmarks"));
    this.add(linkTo(methodOn(BookmarkRestController.class).readBookmark(bookmark.getId())).withSelfRel());
  }

  public Bookmark getBookmark() {
    return bookmark;
  }
}
