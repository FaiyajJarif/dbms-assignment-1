package com.Eqinox.store.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * EXPENSE, GOAL, DEBT, SUBSCRIPTION
     */
    @Column(nullable = false, length = 30)
    private String type;

    public Category() {}

    public Category(Integer userId, String name, String type) {
        this.userId = userId;
        this.name = name;
        this.type = type;
    }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
