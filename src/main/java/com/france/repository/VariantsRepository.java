package com.france.repository;

import com.france.domain.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantsRepository extends JpaRepository<Variant, Long> {
}
