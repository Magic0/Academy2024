package com.mitocode.service.impl;

import com.mitocode.model.Role;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.repo.IRoleRepo;
import com.mitocode.service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends CRUDImpl<Role, String> implements IRoleService {

    private final IRoleRepo repo;

    @Override
    protected IGenericRepo<Role, String> getRepo() {
        return repo;
    }


}
