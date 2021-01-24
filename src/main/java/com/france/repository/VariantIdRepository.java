package com.france.repository;

import com.france.domain.Variant;
import com.france.domain.VariantId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantIdRepository extends JpaRepository<VariantId, Long> {
}
