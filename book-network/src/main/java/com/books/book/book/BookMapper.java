package com.books.book.book;

import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book toBook(BookRequest request){
        return Book.builder()
                .id(request.id())
                .title(request.title())
                .isbn(request.isbn())
                .author(request.authorName())
                .synopsis(request.synopsis())
                .archived(false)
                .shareable(request.shareable())
                .build();
    }

    public BookResponse toBookResponse(Book book){
        return BookResponse
                .builder()
                .title(book.getTitle())
                .authorName(book.getAuthor())
                .isbn(book.getIsbn())
                .synopsis(book.getSynopsis())
                .owner(book.getOwner().fullName())
                .rate(book.getRate())
                .archived(book.getArchived())
                .shareable(book.getShareable())
                // TODO: implement cover function
                .build();
    }
}
