package com.ffms.trackable.controller.customer;

import com.ffms.trackable.common.dto.StandardResponse;
import com.ffms.trackable.common.model.Status;
import com.ffms.trackable.dto.customer.CustomerCreateDto;
import com.ffms.trackable.dto.customer.CustomerResponseDto;
import com.ffms.trackable.dto.customer.CustomerSimpleResponseDto;
import com.ffms.trackable.dto.customer.CustomerUpdateDto;
import com.ffms.trackable.enums.customer.CustomerType;
import com.ffms.trackable.models.customer.Customer;
import com.ffms.trackable.service.customer.CustomerService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    CustomerService customerService;

    ModelMapper modelMapper;

    public CustomerController() {
        this.modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.createTypeMap(CustomerCreateDto.class, Customer.class).addMapping(CustomerCreateDto::getParentId, (customer, parentId) -> customer.getParent().setId((Long) parentId));
        modelMapper.createTypeMap(CustomerUpdateDto.class, Customer.class).addMapping(CustomerUpdateDto::getParentId, (customer, parentId) -> customer.getParent().setId((Long) parentId));
    }

    // Get all customers
    @GetMapping("")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getAllCustomers() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                customerService.findAllCustomers().stream().map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class)).toList()));
    }

    // Get Customer by id
    @GetMapping("/{customerId}")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getCustomerById(@PathVariable Long customerId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(customerService.findByCustomerId(customerId), CustomerResponseDto.class)));
    }

    // Get all 'ACTIVE' customers
    @GetMapping("/active")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getAllActiveCustomers() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                customerService.findCustomersByStatus(Status.ACTIVE).stream().map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class)).toList()));
    }

    // Get all 'INACTIVE' customers
    @GetMapping("/inactive")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getAllInactiveCustomers() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                customerService.findCustomersByStatus(Status.INACTIVE).stream().map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class)).toList()));
    }

    // Get all 'TERMINATED' customers
    @GetMapping("/terminated")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getAllTerminatedCustomers() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                customerService.findCustomersByStatus(Status.TERMINATED).stream().map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class)).toList()));
    }

    // Get all 'CHILD' customers
    @GetMapping("/child")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getChildCustomers() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                customerService.findCustomersByType(CustomerType.CHILD).stream().map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class)).toList()));
    }

    // Get all 'PARENT' customers
    @GetMapping("/parent")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getParentCustomersByType() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                customerService.findCustomersByType(CustomerType.PARENT).stream().map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class)).toList()));
    }

    // Get 'CHILD' customers of a 'PARENT' customer by id
    @GetMapping("/{customerId}/children")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getChildCustomersByParentId(@PathVariable Long customerId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                customerService.findByParentId(customerId).stream().map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class)).toList()));
    }

    // Get 'PARENT' customer of a 'CHILD' customer by id
    @GetMapping("/{customerId}/parent")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> getParentCustomerByChildId(@PathVariable Long customerId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(customerService.findByChildId(customerId), CustomerResponseDto.class)));
    }

    // Create customer
    @PostMapping("")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.writePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerCreateDto customerDto) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(customerService.createCustomer(modelMapper.map(customerDto, Customer.class)), CustomerResponseDto.class)));
    }

    // Update customer
    @PatchMapping("/{customerId}")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> updateCustomer(@Valid @RequestBody CustomerUpdateDto customerDto, @PathVariable Long customerId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(customerService.updateCustomer(modelMapper.map(customerDto, Customer.class), customerId), CustomerResponseDto.class)));
    }

    // Activate customer
    @PatchMapping("/{customerId}/activate")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> activateCustomer(@PathVariable Long customerId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(customerService.changeCustomerStatus(Status.ACTIVE, customerId), CustomerSimpleResponseDto.class)
        ));
    }

    // Deactivate customer
    @PatchMapping("/{customerId}/deactivate")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.updatePrivilege + ',' + @appUtils.deletePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> deactivateCustomer(@PathVariable Long customerId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(customerService.changeCustomerStatus(Status.INACTIVE, customerId), CustomerSimpleResponseDto.class)
        ));
    }

    // Terminate customer
    @PatchMapping("/{customerId}/terminate")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.updatePrivilege + ',' + @appUtils.deletePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<?> terminateCustomer(@PathVariable Long customerId) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(
                modelMapper.map(customerService.changeCustomerStatus(Status.TERMINATED, customerId), CustomerSimpleResponseDto.class)
        ));
    }
}
