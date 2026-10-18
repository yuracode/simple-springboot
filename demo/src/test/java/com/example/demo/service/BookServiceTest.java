package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import com.example.demo.exception.BookNotFoundException;
import com.example.demo.model.Book;
import com.example.demo.repository.BookRepository;

// レッスン15：コンストラクタインジェクションなら、Spring も DB もなしでテストできる
// レッスン16・18：JPA 版・例外に合わせて書き換え
class BookServiceTest {

    private BookRepository repository;
    private BookService service;

    @BeforeEach
    void setUp() {
        repository = mock(BookRepository.class);   // 本物の代わりの「にせもの」
        service = new BookService(repository);     // コンストラクタで渡すだけ
    }

    @Test
    void キーワードが空なら全件を返す() {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(new Book(1, "A", "B", 100)));

        List<Book> result = service.search("  ");

        assertEquals(1, result.size());
        verify(repository, never()).findByTitleContainingOrAuthorContainingOrderById(any(), any());
    }

    @Test
    void キーワードの前後の空白は取り除いて検索する() {
        service.search(" Java ");

        verify(repository).findByTitleContainingOrAuthorContainingOrderById("Java", "Java");
    }

    @Test
    void 存在しないIDなら例外になる() {
        when(repository.findById(999)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> service.getById(999));
    }
}
