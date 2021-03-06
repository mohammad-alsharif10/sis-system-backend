package com.sis.controller;

import com.sis.dto.AcademicTermDTO;
import com.sis.entity.AcademicTerm;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academicTerms")
public class AcademicTermController extends BaseController<AcademicTerm, AcademicTermDTO> {

}
