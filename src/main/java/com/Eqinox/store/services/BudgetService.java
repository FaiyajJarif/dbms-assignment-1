package com.Eqinox.store.services;
import com.Eqinox.store.entities.BudgetItem;
import com.Eqinox.store.entities.BudgetPeriod;
import com.Eqinox.store.repositories.BudgetItemRepository;
import com.Eqinox.store.repositories.BudgetPeriodRepository;
import com.Eqinox.store.repositories.CategoryRepository;
import com.Eqinox.store.entities.Category;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Service
public class BudgetService {
    private final BudgetPeriodRepository budgetPeriodRepo;
    private final BudgetItemRepository budgetItemRepo;
    private final CategoryRepository categoryRepo;
    public BudgetService(
            BudgetPeriodRepository budgetPeriodRepo,
            BudgetItemRepository budgetItemRepo,
            CategoryRepository categoryRepo) {
        this.budgetPeriodRepo = budgetPeriodRepo;
        this.budgetItemRepo = budgetItemRepo;
        this.categoryRepo = categoryRepo;
    }
    public BudgetPeriod getOrCreateCurrentBudget(Integer userId) {
        LocalDate now = LocalDate.now();
        return budgetPeriodRepo
                .findByUserIdAndMonthAndYear(
                        userId,
                        now.getMonthValue(),
                        now.getYear()
                )
                .orElseGet(() -> {
                    BudgetPeriod p = new BudgetPeriod();
                    p.setUserId(userId);
                    p.setMonth(now.getMonthValue());
                    p.setYear(now.getYear());
                    return budgetPeriodRepo.save(p);
                });
    }
    public void syncBudgetItems(BudgetPeriod period) {
        List<Category> categories =
                categoryRepo.findByUserId(period.getUserId());
        for (Category c : categories) {
            budgetItemRepo
                    .findByBudgetIdAndCategoryId(
                            period.getBudgetId(),
                            c.getCategoryId()
                    )
                    .orElseGet(() -> {
                        BudgetItem item = new BudgetItem();
                        item.setBudgetId(period.getBudgetId());
                        item.setCategoryId(c.getCategoryId());
                        return budgetItemRepo.save(item);
                    });
        }
    }
    public void planAmount(
            Integer budgetId,
            Integer categoryId,
            BigDecimal amount) {
        BudgetItem item =
                budgetItemRepo
                        .findByBudgetIdAndCategoryId(budgetId, categoryId)
                        .orElseThrow();
        item.setPlannedAmount(amount);
        budgetItemRepo.save(item);
        recalcTotals(budgetId);
    }
    private void recalcTotals(Integer budgetId) {
        BigDecimal planned = budgetItemRepo.sumPlanned(budgetId);
        BigDecimal actual = budgetItemRepo.sumActual(budgetId);
        BudgetPeriod period =
                budgetPeriodRepo.findById(budgetId).orElseThrow();
        period.setTotalPlanned(planned);
        period.setTotalActual(actual);
        budgetPeriodRepo.save(period);
    }
}
