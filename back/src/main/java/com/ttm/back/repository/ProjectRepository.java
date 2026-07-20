package com.ttm.back.repository;

import com.ttm.back.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query(value = "SELECT COUNT(*) FROM project_members WHERE project_id = :projectId", nativeQuery = true)
    long countMembers(@Param("projectId") Long projectId);

    @Query(value = "SELECT COUNT(*) FROM tasks WHERE project_id = :projectId AND status NOT IN ('done', 'canceled')", nativeQuery = true)
    long countOpenTasks(@Param("projectId") Long projectId);
}
