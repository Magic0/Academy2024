package com.mitocode.service.impl;

import com.mitocode.model.Course;
import com.mitocode.model.Enrollment;
import com.mitocode.repo.IEnrollmentRepo;
import com.mitocode.repo.IGenericRepo;
import com.mitocode.service.IEnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl extends CRUDImpl<Enrollment, String> implements IEnrollmentService {

    private final IEnrollmentRepo repo;

    @Override
    protected IGenericRepo<Enrollment, String> getRepo() {
        return repo;
    }

    private Mono<Enrollment> populateCourses(Enrollment enrollment) {
        List<Course> filteredCourses = enrollment.getCourses().stream()
                .filter(Course::isStatus)
                .collect(Collectors.toList());

        enrollment.setCourses(filteredCourses);

        return Mono.just(enrollment);
    }

    private byte[] generatePDF(Enrollment enrollment) {
        try(InputStream stream = getClass().getResourceAsStream("/academy.jrxml")) {
            Map<String, Object> params = new HashMap<>();
            params.put("txt_student", enrollment.getStudent().getLastName() + " " + enrollment.getStudent().getFirstName());
            params.put("txt_dni", enrollment.getStudent().getDni());
            params.put("txt_edad", enrollment.getStudent().getAge());
            JasperReport report = JasperCompileManager.compileReport(stream);
            JasperPrint print = JasperFillManager.fillReport(report, params, new JRBeanCollectionDataSource(enrollment.getCourses()));
            return JasperExportManager.exportReportToPdf(print);
        }catch (Exception e){
            return new byte[0];
        }
    }

    @Override
    public Mono<byte[]> generateReport(String idEnrollment) {
        return repo.findById(idEnrollment)
               .flatMap(this::populateCourses)
               // .flatMap(this::populateItems)
                .map(this::generatePDF)
                .onErrorResume(e -> Mono.empty());

    }
}
