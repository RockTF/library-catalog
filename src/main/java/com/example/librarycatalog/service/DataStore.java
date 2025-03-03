package com.example.librarycatalog.service;

import com.example.librarycatalog.model.Book;
import com.example.librarycatalog.model.Author;
import com.example.librarycatalog.model.Category;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    public static Map<String, Book> books = new ConcurrentHashMap<>();
    public static Map<String, Author> authors = new ConcurrentHashMap<>();
    public static Map<String, Category> categories = new ConcurrentHashMap<>();

    static {
        // Создание примеров авторов
        Author author1 = new Author();
        author1.setId(UUID.randomUUID().toString());
        author1.setName("John Doe");
        author1.setBiography("An accomplished author.");
        authors.put(author1.getId(), author1);

        Author author2 = new Author();
        author2.setId(UUID.randomUUID().toString());
        author2.setName("Jane Smith");
        author2.setBiography("A renowned writer.");
        authors.put(author2.getId(), author2);

        // Создание примеров категорий
        Category category1 = new Category();
        category1.setId(UUID.randomUUID().toString());
        category1.setName("Fiction");
        category1.setDescription("Fiction books.");
        categories.put(category1.getId(), category1);

        Category category2 = new Category();
        category2.setId(UUID.randomUUID().toString());
        category2.setName("Science");
        category2.setDescription("Science-related books.");
        categories.put(category2.getId(), category2);

        // Создание примеров книг
        Book book1 = new Book();
        book1.setId(UUID.randomUUID().toString());
        book1.setTitle("A Journey Through Time");
        book1.setIsbn("1234567890");
        book1.setPublicationDate("2020-01-01");
        book1.setSummary("A fascinating journey.");
        book1.setAuthorId(author1.getId());
        book1.setCategoryId(category1.getId());
        books.put(book1.getId(), book1);

        Book book2 = new Book();
        book2.setId(UUID.randomUUID().toString());
        book2.setTitle("The Science of Everything");
        book2.setIsbn("0987654321");
        book2.setPublicationDate("2021-06-15");
        book2.setSummary("Exploring scientific wonders.");
        book2.setAuthorId(author2.getId());
        book2.setCategoryId(category2.getId());
        books.put(book2.getId(), book2);
    }
}
