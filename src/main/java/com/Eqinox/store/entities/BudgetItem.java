package com.Eqinox.store.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budget_items")
public class BudgetItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "budget_id", nullable = false)
    private Integer budgetId;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "planned_amount")
    private BigDecimal plannedAmount = BigDecimal.ZERO;

    @Column(name = "actual_amount")
    private BigDecimal actualAmount = BigDecimal.ZERO;

    // Getters & Setters
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public Integer getBudgetId() { return budgetId; }
    public void setBudgetId(Integer budgetId) { this.budgetId = budgetId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public BigDecimal getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(BigDecimal plannedAmount) { this.plannedAmount = plannedAmount; }

    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
}
