package com.opticstore.brand.controller;

import com.opticstore.brand.dto.BrandResponse;
import com.opticstore.brand.service.BrandService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@CrossOrigin
public class BrandController {

    private final BrandService service;

    public BrandController(BrandService service) {
        this.service = service;
    }

    @GetMapping
    public List<BrandResponse> getAll() {
        return service.getAll();
    }
}
