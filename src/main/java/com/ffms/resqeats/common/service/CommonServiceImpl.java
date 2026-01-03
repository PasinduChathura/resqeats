package com.ffms.resqeats.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffms.resqeats.exception.common.NotFoundException;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public abstract class CommonServiceImpl<T, ID, R extends JpaRepository<T, ID>> implements CommonService<T, ID> {

    private final Type[] actualTypeArguments = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
    @Autowired
    protected R repository;

    public abstract String isValid(T t);

    @Override
    public T create(T t) throws Exception {
        String error = isValid(t);
        if (StringUtils.isNotEmpty(error)) {
            throw new Exception(error);
        }
        return repository.saveAndFlush(t);
    }

    @Override
    public List<T> findAll() throws Exception {
        return repository.findAll();
    }

    @Override
    public T findById(ID id) throws Exception {
        String resourceName = ((Class<T>) actualTypeArguments[0]).getSimpleName();
        return repository.findById(id).orElseThrow(() -> new NotFoundException(resourceName + " not found for ID: " + id));
    }

    @Override
    public T findById(ID id, String message) throws Exception {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(message));
    }

    @Override
    public T update(T objToUpdate, ID id) throws Exception {
        T t = this.findById(id);
        T updateObj = new ObjectMapper().readerForUpdating(t).readValue(new ObjectMapper().writeValueAsString(objToUpdate));
        return repository.saveAndFlush(updateObj);
    }

    @Override
    public void deleteById(ID id) throws Exception {
        this.findById(id);
        repository.deleteById(id);
    }
}