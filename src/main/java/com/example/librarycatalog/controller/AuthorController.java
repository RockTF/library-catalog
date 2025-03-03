package com.example.librarycatalog.controller;

import com.example.librarycatalog.model.Author;
import com.example.librarycatalog.service.DataStore;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/authors")
public class AuthorController {

    @GetMapping
    public ResponseEntity<?> getAuthors(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {

        List<Author> authors = new ArrayList<>(DataStore.authors.values());
        int total = authors.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        if (fromIndex > total) {
            authors = Collections.emptyList();
        } else {
            authors = authors.subList(fromIndex, toIndex);
        }

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
        response.put("data", authors);
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("total", total);
        response.put("links", links);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic());
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAuthor(@PathVariable String id) {
        Author author = DataStore.authors.get(id);
        if (author == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Author not found"));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic());
        return new ResponseEntity<>(author, headers, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createAuthor(@RequestBody Author author) {
        if (author.getName() == null || author.getBiography() == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Missing required fields"));
        }
        author.setId(UUID.randomUUID().toString());
        DataStore.authors.put(author.getId(), author);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                        .buildAndExpand(author.getId()).toUri().toString())
                .body(author);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAuthor(@PathVariable String id, @RequestBody Author updatedAuthor) {
        Author existing = DataStore.authors.get(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Author not found"));
        }
        if (updatedAuthor.getName() != null) existing.setName(updatedAuthor.getName());
        if (updatedAuthor.getBiography() != null) existing.setBiography(updatedAuthor.getBiography());
        DataStore.authors.put(id, existing);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAuthor(@PathVariable String id) {
        Author existing = DataStore.authors.get(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Author not found"));
        }
        DataStore.authors.remove(id);
        return ResponseEntity.noContent().build();
    }
}
