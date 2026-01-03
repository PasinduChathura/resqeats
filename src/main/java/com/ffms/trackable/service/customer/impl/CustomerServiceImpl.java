package com.ffms.trackable.service.customer.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffms.trackable.common.model.Status;
import com.ffms.trackable.common.service.CommonServiceImpl;
import com.ffms.trackable.enums.customer.CustomerType;
import com.ffms.trackable.exception.common.NotFoundException;
import com.ffms.trackable.exception.customer.CustomerAlreadyExistException;
import com.ffms.trackable.models.customer.Customer;
import com.ffms.trackable.repository.customer.CustomerRepository;
import com.ffms.trackable.service.customer.CustomerService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl extends CommonServiceImpl<Customer, Long, CustomerRepository> implements CustomerService {

    @Override
    public String isValid(Customer customer) {
        return null;
    }

    // find all customers
    @Override
    public List<Customer> findAllCustomers() throws Exception {
        return Optional.ofNullable(this.findAll()).orElse(Collections.emptyList());
    }

    // find all customers by type
    @Override
    public List<Customer> findCustomersByType(CustomerType type) throws Exception {
        return this.repository.findByType(type).orElse(Collections.emptyList());
    }

    // find all customers by status
    @Override
    public List<Customer> findCustomersByStatus(Status status) throws Exception {
        return this.repository.findByStatus(status).orElse(Collections.emptyList());
    }

    // find customers by ID
    @Override
    public Customer findByCustomerId(Long customerId) throws Exception {
        return this.findById(customerId);
    }

    // find customers by ID and define custom error message
    private void findByCustomerId(Long customerId, String message) throws Exception {
        this.findById(customerId, message);
    }

    // find CHILD customers by PARENT ID
    @Override
    public List<Customer> findByParentId(Long customerId) throws Exception {
        return this.repository.findByParentId(customerId);
    }

    // find PARENTS customers by CHILD ID
    @Override
    public Customer findByChildId(Long customerId) throws Exception {
        return this.repository.findParentByChildId(customerId).orElseThrow(() -> new NotFoundException( "parent customer not found for customer ID: " + customerId));
    }

    // find customers by refId
    @Override
    public Optional<Customer> findByRefId(final String refId) {
        return this.repository.findByRefId(refId);
    }

    // find customers by name
    @Override
    public Optional<List<Customer>> findByName(final String name) {
        return this.repository.findByName(name);
    }

    // find customers by address
    @Override
    public Optional<List<Customer>> findByAddress(final String name) {
        return this.repository.findByAddress(name);
    }

    // find customers by name and address
    @Override
    public Optional<Customer> findByNameAndAddress(final String name, final String address) {
        return this.repository.findByNameAndAddress(name, address);
    }

    // create customer
    @Override
    public Customer createCustomer(Customer customer) throws Exception {
        // check if name and address already exists
        if (this.customerNameAndAddressExists(customer.getName(), customer.getAddress())) {
            throw new CustomerAlreadyExistException("there is a customer with entered name: " + customer.getName() + " and address : " + customer.getAddress());
        }

        // check if refId already exists
        if (this.customerRefIdExists(customer.getRefId())) {
            throw new CustomerAlreadyExistException("there is a customer with entered ref ID: " + customer.getRefId());
        }

        // set customer type
        customer.setType(CustomerType.PARENT);
        if (customer.getParent().getId() != null) {
            this.findByCustomerId(customer.getParent().getId(), "parent customer not found for id " + customer.getParent().getId());
            customer.setType(CustomerType.CHILD);
        }

        // create customer
        return this.create(customer);
    }

    // update customer
    @Override
    public Customer updateCustomer(Customer customer, Long customerId) throws Exception {
        // check if a customer is exists for the customer ID
        Customer currentCustomer = this.findByCustomerId(customerId);

        // check if refId already exists
        if (customer.getRefId() != null && customer.getRefId() != null) {
            Optional<Customer> customerExists = this.findByRefId(customer.getRefId());
            if (customerExists.isPresent() && !customerExists.get().getId().equals(customerId)) {
                throw new CustomerAlreadyExistException("there is a customer with entered ref ID: " + customer.getRefId());
            }
        }

        // check if name and address already exists
        if (customer.getName() != null && customer.getAddress() != null) {
            Optional<Customer> customerExists = this.findByNameAndAddress(customer.getName(), customer.getAddress());
            if (customerExists.isPresent() && !customerExists.get().getId().equals(customerId)) {
                throw new CustomerAlreadyExistException("there is a customer with entered name: " + customer.getName() + " and address : " + customer.getAddress());
            }
        }

        // check if name already exists
        if (customer.getName() != null) {
            Optional<Customer> customerExists = this.findByNameAndAddress(customer.getName(), currentCustomer.getAddress());
            if (customerExists.isPresent() && !customerExists.get().getId().equals(customerId)) {
                throw new CustomerAlreadyExistException("there is a customer with that customer name: " + customer.getName());
            }
        }

        // check if address already exists
        if (customer.getAddress() != null) {
            Optional<Customer> customerExists = this.findByNameAndAddress(currentCustomer.getName(), customer.getAddress());
            if (customerExists.isPresent() && !customerExists.get().getId().equals(customerId)) {
                throw new CustomerAlreadyExistException("there is a customer with that customer address: " + customer.getAddress());
            }
        }

        // map dto to customer
        new ObjectMapper().readerForUpdating(currentCustomer).readValue(new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(customer));

        // set customer type
        if (customer.getType().equals(CustomerType.PARENT)) {
            // set parent null
            currentCustomer.setParent(null);
        } else if (customer.getType().equals(CustomerType.CHILD) && customer.getParent().getId() != null) {
            this.findByCustomerId(customer.getParent().getId(), "parent customer not found for id " + customer.getParent().getId());
        }

        // update customer
        return this.update(currentCustomer, customerId);
    }

    // change customer status
    @Override
    public Customer changeCustomerStatus(Status status, Long customerId) throws Exception {
        Customer customer = this.findByCustomerId(customerId);
        customer.setStatus(status);
        return this.update(customer, customerId);
    }

    // find if customer exists by refId
    public boolean customerRefIdExists(final String refId) {
        return this.findByRefId(refId).isPresent();
    }

    // find if customer exists by name
    public boolean customerNameExists(final String name) {
        return this.findByName(name).isPresent();
    }

    // find if customer exists by name
    public boolean customerNameAndAddressExists(final String name, final String address) {
        return this.findByNameAndAddress(name, address).isPresent();
    }
}
