package com.books.book.book;

import com.books.book.common.PageResponse;
import com.books.book.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public Integer save(BookRequest request, Authentication currentUser){
        User user = (User) currentUser.getPrincipal();
        Book book = bookMapper.toBook(request);
        System.out.println("setting user");
        book.setOwner(user);
        System.out.println("book: " + book.toString());

        return bookRepository.save(book).getId();

    }

    public BookResponse findById(Integer bookId) {
        return bookRepository.findById(bookId).map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find book in database with id " + bookId));

    }


    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication connectedUser) {
        User currentUser = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> books = bookRepository.findAllDisplayableBooks(pageable, currentUser.getId());
        List<BookResponse> bookResponse = books.stream().map(bookMapper::toBookResponse).toList();
        return PageResponse.<BookResponse>builder()
                .content(bookResponse)
                .first(books.isFirst())
                .last(books.isLast())
                .number(books.getNumber())
                .size(books.getSize())
                .totalElements(books.getTotalElements())
                .totalPages(books.getTotalPages())
                .build();
    }
}
