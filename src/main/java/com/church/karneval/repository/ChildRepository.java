package com.church.karneval.repository;

import com.church.karneval.model.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.church.karneval.enums.AgeGroup;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChildRepository extends JpaRepository<Child, UUID> {
    List<Child> findByTeamId(UUID teamId);

    long countByTeamId(UUID teamId);

    // عدد الأطفال حسب الفئة العمرية في فريق
    long countByTeamIdAndAgeGroup(UUID teamId, AgeGroup ageGroup);

    // عدد الأطفال الكلي حسب الفئة العمرية (للـ Super Admin)
    long countByAgeGroup(AgeGroup ageGroup);

    // كل الأطفال في اليوم (للـ Super Admin)
    // موجودة أصلاً في JpaRepository كـ findAll()
}
