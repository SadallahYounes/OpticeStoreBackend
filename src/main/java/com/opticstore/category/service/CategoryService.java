package com.opticstore.category.service;

import com.opticstore.category.dto.CategoryResponse;
import com.opticstore.category.model.Category;
import com.opticstore.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    public List<CategoryResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(c -> new CategoryResponse(
                        c.getId(),
                        c.getName(),
                        c.getSlug()
                ))
                .toList();
    }
}
