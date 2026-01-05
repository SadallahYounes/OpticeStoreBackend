package com.opticstore.category.controller;

import com.opticstore.category.dto.CategoryResponse;
import com.opticstore.category.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoryResponse> getAll() {
        return service.getAll();
    }
}
