package com.ffms.resqeats.service.customer;

import com.ffms.resqeats.common.model.Status;
import com.ffms.resqeats.common.service.CommonService;
import com.ffms.resqeats.enums.customer.CustomerType;
import com.ffms.resqeats.models.customer.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerService extends CommonService<Customer, Long> {
    List<Customer> findAllCustomers() throws Exception;

    List<Customer> findCustomersByType(CustomerType type) throws Exception;

    List<Customer> findCustomersByStatus(Status status) throws Exception;

    Customer findByCustomerId(Long customerId) throws Exception;

    List<Customer> findByParentId(Long customerId) throws Exception;

    Customer findByChildId(Long customerId) throws Exception;

    Customer createCustomer(Customer customer) throws Exception;

    Customer updateCustomer(Customer customer, Long customerId) throws Exception;

    Optional<Customer> findByRefId(final String refId) throws Exception;

    Optional<List<Customer>> findByName(final String name) throws Exception;

    Optional<List<Customer>> findByAddress(final String address) throws Exception;

    Optional<Customer> findByNameAndAddress(final String name, final String address) throws Exception;

    Customer changeCustomerStatus(Status status, Long customerId) throws Exception;

    boolean customerNameExists(final String customerName) throws Exception;
}