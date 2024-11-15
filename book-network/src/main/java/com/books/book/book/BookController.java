package com.books.book.book;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "books")
public class BookController {

    private final BookService service;

    @PostMapping("")
    public ResponseEntity<Integer> saveBook (
            @RequestBody @Valid BookRequest request, Authentication connectedUser
    ) {
        System.out.println("starting book post controller");
        Integer id = service.save(request, connectedUser);
        return ResponseEntity.ok(id);
    }

    @GetMapping("{book-id}")
    public ResponseEntity<BookResponse> getBook(
            @PathVariable("book-id") Integer bookId
    ){
      return ResponseEntity.ok(BookResponse);
    }



}
