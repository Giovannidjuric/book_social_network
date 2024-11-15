package com.books.book.book;

import com.books.book.common.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "books")
public class BookController {

    private final BookService service;
    private final BookService bookService;

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
      return ResponseEntity.ok(bookService.findById(bookId));
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponse<BookResponse>> findAllBooks(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ){
        return ResponseEntity.ok(bookService.findAllBooks(page, size, connectedUser));
    }



}
