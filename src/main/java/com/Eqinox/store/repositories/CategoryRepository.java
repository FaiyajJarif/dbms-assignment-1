package com.Eqinox.store.repositories;

import com.Eqinox.store.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findByUserId(Integer userId);

    boolean existsByUserIdAndName(Integer userId, String name);
}
