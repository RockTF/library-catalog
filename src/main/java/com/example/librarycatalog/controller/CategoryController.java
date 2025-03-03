package com.example.librarycatalog.controller;

import com.example.librarycatalog.model.Category;
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
@RequestMapping("/categories")
public class CategoryController {

    @GetMapping
    public ResponseEntity<?> getCategories(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {

        List<Category> categories = new ArrayList<>(DataStore.categories.values());
        int total = categories.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        if (fromIndex > total) {
            categories = Collections.emptyList();
        } else {
            categories = categories.subList(fromIndex, toIndex);
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
        response.put("data", categories);
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("total", total);
        response.put("links", links);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic());
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategory(@PathVariable String id) {
        Category category = DataStore.categories.get(id);
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Category not found"));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic());
        return new ResponseEntity<>(category, headers, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        if (category.getName() == null || category.getDescription() == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Missing required fields"));
        }
        category.setId(UUID.randomUUID().toString());
        DataStore.categories.put(category.getId(), category);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                        .buildAndExpand(category.getId()).toUri().toString())
                .body(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable String id, @RequestBody Category updatedCategory) {
        Category existing = DataStore.categories.get(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Category not found"));
        }
        if (updatedCategory.getName() != null) existing.setName(updatedCategory.getName());
        if (updatedCategory.getDescription() != null) existing.setDescription(updatedCategory.getDescription());
        DataStore.categories.put(id, existing);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable String id) {
        Category existing = DataStore.categories.get(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Category not found"));
        }
        DataStore.categories.remove(id);
        return ResponseEntity.noContent().build();
    }
}
