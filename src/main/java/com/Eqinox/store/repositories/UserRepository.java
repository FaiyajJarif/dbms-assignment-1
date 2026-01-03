package com.Eqinox.store.repositories;

import com.Eqinox.store.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    // ✅ COUNT query (DBMS requirement)
    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

    // ✅ STARTS WITH search + manual pagination
    @Query("""
                SELECT u FROM User u
                WHERE LOWER(u.name) LIKE LOWER(CONCAT(:keyword, '%'))
                ORDER BY u.name
            """)
    List<User> searchUsersStartingWith(
            @Param("keyword") String keyword);

    // ✅ SAME query but paginated (LIMIT + OFFSET)
    @Query("""
                SELECT u FROM User u
                WHERE LOWER(u.name) LIKE LOWER(CONCAT(:keyword, '%'))
                ORDER BY u.name
            """)
    List<User> searchUsersStartingWithPaged(
            @Param("keyword") String keyword,
            org.springframework.data.domain.Pageable pageable);

    @Query("""
                SELECT u FROM User u
                ORDER BY u.createdAt ASC
            """)
    List<User> findFirst15ByRegistrationDateAsc(
            org.springframework.data.domain.Pageable pageable);

    @Query(value = """
            SELECT
                TO_CHAR(created_at, 'Mon') AS month,
                COUNT(*) AS total
            FROM users
            GROUP BY month
            ORDER BY MIN(created_at)
            """, nativeQuery = true)
    List<Object[]> getMonthlyUserGrowth();

    @Query(value = """
            SELECT
                TO_CHAR(created_at, 'YYYY-MM-DD') AS day,
                COUNT(*) AS total
            FROM users
            GROUP BY day
            ORDER BY day
            """, nativeQuery = true)
    List<Object[]> getDailyUserGrowth();

    @Query(value = """
            SELECT
                TO_CHAR(DATE_TRUNC('week', created_at), 'YYYY-MM-DD') AS week,
                COUNT(*) AS total
            FROM users
            GROUP BY week
            ORDER BY week
            """, nativeQuery = true)
    List<Object[]> getWeeklyUserGrowth();

    @Query(value = """
                SELECT DATE(u.created_at) AS day, COUNT(c.category_id)
                FROM users u
                JOIN categories c ON c.user_id = u.user_id
                GROUP BY day
                ORDER BY day
            """, nativeQuery = true)
    List<Object[]> getDailyCategoryGrowthViaUsers();

}
