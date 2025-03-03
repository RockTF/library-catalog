package com.example.librarycatalog.controller;

import com.example.librarycatalog.model.Book;
import com.example.librarycatalog.service.DataStore;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/books")
public class BookController {

    @GetMapping
    public ResponseEntity<?> getBooks(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "authorId", required = false) String authorId,
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @RequestParam(value = "sort", required = false) String sort) {

        List<Book> books = new ArrayList<>(DataStore.books.values());

        if (authorId != null) {
            books = books.stream().filter(b -> b.getAuthorId().equals(authorId)).collect(Collectors.toList());
        }
        if (categoryId != null) {
            books = books.stream().filter(b -> b.getCategoryId().equals(categoryId)).collect(Collectors.toList());
        }

        if (sort != null) {
            String[] sortParams = sort.split(",");
            String sortField = sortParams[0];
            String sortOrder = sortParams.length > 1 ? sortParams[1] : "asc";
            Comparator<Book> comparator = Comparator.comparing(b -> {
                if ("title".equalsIgnoreCase(sortField)) return b.getTitle();
                if ("publicationDate".equalsIgnoreCase(sortField)) return b.getPublicationDate();
                return b.getTitle();
            });
            if ("desc".equalsIgnoreCase(sortOrder)) {
                comparator = comparator.reversed();
            }
            books.sort(comparator);
        }

        int total = books.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        if (fromIndex > total) {
            books = Collections.emptyList();
        } else {
            books = books.subList(fromIndex, toIndex);
        }

        books.forEach(b -> {});

        Map<String, String> links = new HashMap<>();
        links.put("self", ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString());
        if (toIndex < total) {
            links.put("next", ServletUriComponentsBuilder.fromCurrentRequest()
                    .replaceQueryParam("page", page + 1)
                    .build().toUriString());
        }
        if (page > 1) {
            links.put("prev", ServletUriComponentsBuilder.fromCurrentRequest()
                    .replaceQueryParam("page", page - 1)
                    .build().toUriString());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", books);
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("total", total);
        response.put("links", links);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic());
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBook(@PathVariable String id) {
        Book book = DataStore.books.get(id);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Book not found"));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic());
        return new ResponseEntity<>(book, headers, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createBook(@RequestBody Book book) {
        if (book.getTitle() == null || book.getIsbn() == null || book.getPublicationDate() == null ||
                book.getSummary() == null || book.getAuthorId() == null || book.getCategoryId() == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Missing required fields"));
        }
        book.setId(UUID.randomUUID().toString());
        DataStore.books.put(book.getId(), book);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                        .buildAndExpand(book.getId()).toUri().toString())
                .body(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable String id, @RequestBody Book updatedBook) {
        Book existing = DataStore.books.get(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Book not found"));
        }
        if (updatedBook.getTitle() != null) existing.setTitle(updatedBook.getTitle());
        if (updatedBook.getIsbn() != null) existing.setIsbn(updatedBook.getIsbn());
        if (updatedBook.getPublicationDate() != null) existing.setPublicationDate(updatedBook.getPublicationDate());
        if (updatedBook.getSummary() != null) existing.setSummary(updatedBook.getSummary());
        if (updatedBook.getAuthorId() != null) existing.setAuthorId(updatedBook.getAuthorId());
        if (updatedBook.getCategoryId() != null) existing.setCategoryId(updatedBook.getCategoryId());
        DataStore.books.put(id, existing);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable String id) {
        Book existing = DataStore.books.get(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Book not found"));
        }
        DataStore.books.remove(id);
        return ResponseEntity.noContent().build();
    }
}
