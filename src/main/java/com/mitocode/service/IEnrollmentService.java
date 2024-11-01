package com.mitocode.service;

import com.mitocode.model.Enrollment;
import reactor.core.publisher.Mono;

public interface IEnrollmentService extends ICRUD<Enrollment, String> {

    Mono<byte[]> generateReport(String idInvoice);

}
