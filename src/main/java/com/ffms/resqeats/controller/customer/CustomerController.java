package com.ffms.resqeats.controller.customer;

import com.ffms.resqeats.common.dto.ApiResponse;
import com.ffms.resqeats.common.model.Status;
import com.ffms.resqeats.dto.customer.CustomerCreateDto;
import com.ffms.resqeats.dto.customer.CustomerResponseDto;
import com.ffms.resqeats.dto.customer.CustomerSimpleResponseDto;
import com.ffms.resqeats.dto.customer.CustomerUpdateDto;
import com.ffms.resqeats.enums.customer.CustomerType;
import com.ffms.resqeats.models.customer.Customer;
import com.ffms.resqeats.service.customer.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer management controller for CRUD operations on customers.
 */
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final ModelMapper modelMapper;

    /**
     * Get all customers
     */
    @GetMapping
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<CustomerSimpleResponseDto>>> getAllCustomers() throws Exception {
        List<CustomerSimpleResponseDto> customers = customerService.findAllCustomers().stream()
                .map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    /**
     * Get customer by ID
     */
    @GetMapping("/{customerId}")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<CustomerResponseDto>> getCustomerById(@PathVariable Long customerId) throws Exception {
        CustomerResponseDto customer = modelMapper.map(customerService.findByCustomerId(customerId), CustomerResponseDto.class);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    /**
     * Get all 'ACTIVE' customers
     */
    @GetMapping("/active")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<CustomerSimpleResponseDto>>> getAllActiveCustomers() throws Exception {
        List<CustomerSimpleResponseDto> customers = customerService.findCustomersByStatus(Status.ACTIVE).stream()
                .map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    /**
     * Get all 'INACTIVE' customers
     */
    @GetMapping("/inactive")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<CustomerSimpleResponseDto>>> getAllInactiveCustomers() throws Exception {
        List<CustomerSimpleResponseDto> customers = customerService.findCustomersByStatus(Status.INACTIVE).stream()
                .map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    /**
     * Get all 'TERMINATED' customers
     */
    @GetMapping("/terminated")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<CustomerSimpleResponseDto>>> getAllTerminatedCustomers() throws Exception {
        List<CustomerSimpleResponseDto> customers = customerService.findCustomersByStatus(Status.TERMINATED).stream()
                .map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    /**
     * Get all 'CHILD' type customers
     */
    @GetMapping("/child")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<CustomerSimpleResponseDto>>> getChildCustomers() throws Exception {
        List<CustomerSimpleResponseDto> customers = customerService.findCustomersByType(CustomerType.CHILD).stream()
                .map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    /**
     * Get all 'PARENT' type customers
     */
    @GetMapping("/parent")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<CustomerSimpleResponseDto>>> getParentCustomersByType() throws Exception {
        List<CustomerSimpleResponseDto> customers = customerService.findCustomersByType(CustomerType.PARENT).stream()
                .map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    /**
     * Get 'CHILD' customers of a 'PARENT' customer
     */
    @GetMapping("/{customerId}/children")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<List<CustomerSimpleResponseDto>>> getChildCustomersByParentId(@PathVariable Long customerId) throws Exception {
        List<CustomerSimpleResponseDto> customers = customerService.findByParentId(customerId).stream()
                .map(customer -> modelMapper.map(customer, CustomerSimpleResponseDto.class))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    /**
     * Get 'PARENT' customer of a 'CHILD' customer
     */
    @GetMapping("/{customerId}/parent")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.readPrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<CustomerResponseDto>> getParentCustomerByChildId(@PathVariable Long customerId) throws Exception {
        CustomerResponseDto customer = modelMapper.map(customerService.findByChildId(customerId), CustomerResponseDto.class);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    /**
     * Create a new customer
     */
    @PostMapping
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.writePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<CustomerResponseDto>> createCustomer(@Valid @RequestBody CustomerCreateDto customerDto) throws Exception {
        Customer customer = customerService.createCustomer(modelMapper.map(customerDto, Customer.class));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(modelMapper.map(customer, CustomerResponseDto.class), "Customer created successfully"));
    }

    /**
     * Update customer
     */
    @PatchMapping("/{customerId}")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<CustomerResponseDto>> updateCustomer(
            @Valid @RequestBody CustomerUpdateDto customerDto,
            @PathVariable Long customerId) throws Exception {
        Customer customer = customerService.updateCustomer(modelMapper.map(customerDto, Customer.class), customerId);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(customer, CustomerResponseDto.class), "Customer updated successfully"));
    }

    /**
     * Activate customer
     */
    @PatchMapping("/{customerId}/activate")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.updatePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<CustomerSimpleResponseDto>> activateCustomer(@PathVariable Long customerId) throws Exception {
        Customer customer = customerService.changeCustomerStatus(Status.ACTIVE, customerId);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(customer, CustomerSimpleResponseDto.class), "Customer activated successfully"));
    }

    /**
     * Deactivate customer
     */
    @PatchMapping("/{customerId}/deactivate")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.updatePrivilege + ',' + @appUtils.deletePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<CustomerSimpleResponseDto>> deactivateCustomer(@PathVariable Long customerId) throws Exception {
        Customer customer = customerService.changeCustomerStatus(Status.INACTIVE, customerId);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(customer, CustomerSimpleResponseDto.class), "Customer deactivated successfully"));
    }

    /**
     * Terminate customer
     */
    @PatchMapping("/{customerId}/terminate")
    @PreAuthorize("hasPermission(#id, @appUtils.customerResource, @appUtils.updatePrivilege + ',' + @appUtils.deletePrivilege + ',' + @appUtils.adminPrivilege)")
    public ResponseEntity<ApiResponse<CustomerSimpleResponseDto>> terminateCustomer(@PathVariable Long customerId) throws Exception {
        Customer customer = customerService.changeCustomerStatus(Status.TERMINATED, customerId);
        return ResponseEntity.ok(ApiResponse.success(modelMapper.map(customer, CustomerSimpleResponseDto.class), "Customer terminated successfully"));
    }
}
