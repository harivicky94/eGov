package org.egov.bpa.master.repository;

import java.util.List;

import org.egov.bpa.master.entity.SlotMapping;
import org.egov.bpa.master.entity.enums.ApplicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlotMappingRepository extends JpaRepository<SlotMapping,Long> {

	List<SlotMapping> findByApplicationType(ApplicationType applicationType);

}
