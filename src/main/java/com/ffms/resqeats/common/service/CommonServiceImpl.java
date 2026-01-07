package com.ffms.resqeats.common.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffms.resqeats.exception.common.NotFoundException;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@Slf4j
public abstract class CommonServiceImpl<T, ID, R extends JpaRepository<T, ID>> implements CommonService<T, ID> {

    private final Type[] actualTypeArguments = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
    @Autowired
    protected R repository;

    public abstract String isValid(T t);

    @Override
    public T create(T t) throws Exception {
        log.debug("Creating entity: {}", t);
        String error = isValid(t);
        if (StringUtils.isNotEmpty(error)) {
            log.warn("Validation failed during create: {}", error);
            throw new Exception(error);
        }
        T created = repository.saveAndFlush(t);
        log.info("Entity created: {}", created);
        return created;
    }

    @Override
    public List<T> findAll() throws Exception {
        log.debug("Finding all entities");
        return repository.findAll();
    }

    @Override
    public T findById(ID id) throws Exception {
        String resourceName;
        if (actualTypeArguments[0] instanceof Class<?>) {
            resourceName = ((Class<?>) actualTypeArguments[0]).getSimpleName();
        } else {
            resourceName = "Resource";
        }
        log.debug("Finding {} by id: {}", resourceName, id);
        return repository.findById(id).orElseThrow(() -> new NotFoundException(resourceName + " not found for ID: " + id));
    }

    @Override
    public T findById(ID id, String message) throws Exception {
        log.debug("Finding entity by id with custom message: {}", id);
        return repository.findById(id).orElseThrow(() -> new NotFoundException(message));
    }

    @Override
    public T update(T objToUpdate, ID id) throws Exception {
        log.debug("Updating entity id: {} with data: {}", id, objToUpdate);
        T t = this.findById(id);
        T updateObj = new ObjectMapper().readerForUpdating(t).readValue(new ObjectMapper().writeValueAsString(objToUpdate));
        T updated = repository.saveAndFlush(updateObj);
        log.info("Entity updated: {}", updated);
        return updated;
    }

    @Override
    public void deleteById(ID id) throws Exception {
        log.debug("Deleting entity by id: {}", id);
        this.findById(id);
        repository.deleteById(id);
        log.info("Entity deleted with id: {}", id);
    }
} 