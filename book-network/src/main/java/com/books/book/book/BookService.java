package com.books.book.book;

import com.books.book.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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


}
