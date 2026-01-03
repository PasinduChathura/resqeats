package com.ffms.trackable.repository.customer;

import com.ffms.trackable.common.model.Status;
import com.ffms.trackable.enums.customer.CustomerType;
import com.ffms.trackable.models.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findById(Integer id);

    Optional<Customer> findByRefId(String refId);

    Optional<List<Customer>> findByName(String name);

    Optional<List<Customer>> findByAddress(String address);

    Optional<Customer> findByNameAndAddress(String name, String address);

    Optional<List<Customer>> findByType(CustomerType type);

    Optional<List<Customer>> findByStatus(Status status);

    List<Customer> findByParentId(Long parentId);

    @Query("SELECT c.parent FROM Customer c WHERE c.id = :childId")
    Optional<Customer> findParentByChildId(Long childId);

}
