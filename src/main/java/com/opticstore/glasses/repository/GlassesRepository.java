package com.opticstore.glasses.repository;


import com.opticstore.glasses.model.Glasses;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface GlassesRepository extends JpaRepository<Glasses, Long> {

    List<Glasses> findByCategory_Slug(String slug);

    List<Glasses> findByBrand_NameIgnoreCase(String name);
}