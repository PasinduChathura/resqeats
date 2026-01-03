package com.ffms.trackable.common.service;

import java.util.List;

public interface CommonService<T,ID> {

    T create(T t) throws Exception;
    List<T> findAll() throws Exception;
    T findById(ID id) throws Exception;
    T findById(ID id, String message) throws Exception;
    T update(T t, ID id) throws Exception;
    void deleteById(ID id) throws Exception;
}
