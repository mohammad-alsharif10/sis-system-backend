package com.sis.controller;

import com.sis.dto.DegreeDTO;
import com.sis.entity.Degree;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/degrees")
@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
public class DegreeController extends BaseController<Degree, DegreeDTO> {

}
