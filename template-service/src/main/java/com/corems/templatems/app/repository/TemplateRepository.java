package com.corems.templatems.app.repository;

import com.corems.common.utils.db.repo.SearchableRepository;
import com.corems.templatems.app.entity.TemplateEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateRepository extends SearchableRepository<TemplateEntity, Long> {

    Optional<TemplateEntity> findByTemplateIdAndIsDeletedFalse(String templateId);
    
    Optional<TemplateEntity> findByUuid(UUID uuid);

    @Override
    default List<String> getSearchFields() {
        return List.of("name", "description", "templateId");
    }

    @Override
    default List<String> getAllowedFilterFields() {
        return List.of("category", "isDeleted");
    }

    @Override
    default List<String> getAllowedSortFields() {
        return List.of("name", "category", "createdAt", "updatedAt", "templateId");
    }
}
