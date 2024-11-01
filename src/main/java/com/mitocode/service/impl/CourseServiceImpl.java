package com.mitocode.service.impl;

import com.mitocode.model.Course;
import com.mitocode.repo.ICourseRepo;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.service.ICourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl extends CRUDImpl<Course, String> implements ICourseService {

    private final ICourseRepo repo;

    @Override
    protected IGenericRepo<Course, String> getRepo() {
        return repo;
    }

}
