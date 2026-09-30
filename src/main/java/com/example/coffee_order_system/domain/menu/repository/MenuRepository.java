
package com.example.coffee_order_system.domain.menu.repository;

import com.example.coffee_order_system.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByOrderByIdAsc();
}