package com.france.repository;

import com.france.domain.Option;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionsRepository extends JpaRepository<Option, Long> {
}
