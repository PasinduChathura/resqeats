package com.ffms.trackable.common.controller;

import com.ffms.trackable.common.service.CommonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public abstract class CommonControllerImpl<T, ID, S extends CommonService<T, ID>> implements CommonController<T, ID> {

    protected S service;

    public CommonControllerImpl(S service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<T>> getAll() throws Exception {
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<T> findById(@PathVariable("id") ID id) throws Exception {
        return new ResponseEntity<>(service.findById(id), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<T> update(@RequestBody T t, @PathVariable("id") ID id) throws Exception {
        return new ResponseEntity<>(service.update(t, id), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> delete(@PathVariable("id") ID id) throws Exception {
        service.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Deleted successfully");
    }

    @Override
    public ResponseEntity<T> add(@RequestBody T t) throws Exception {
        return new ResponseEntity<>(service.create(t), HttpStatus.OK);

    }
}
