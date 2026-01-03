package com.Eqinox.store.repositories;

import com.Eqinox.store.entities.BudgetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface BudgetItemRepository
        extends JpaRepository<BudgetItem, Integer> {

    Optional<BudgetItem> findByBudgetIdAndCategoryId(
            Integer budgetId,
            Integer categoryId
    );

    @Query("SELECT COALESCE(SUM(b.plannedAmount), 0) FROM BudgetItem b WHERE b.budgetId = :budgetId")
    BigDecimal sumPlanned(Integer budgetId);

    @Query("SELECT COALESCE(SUM(b.actualAmount), 0) FROM BudgetItem b WHERE b.budgetId = :budgetId")
    BigDecimal sumActual(Integer budgetId);
}
