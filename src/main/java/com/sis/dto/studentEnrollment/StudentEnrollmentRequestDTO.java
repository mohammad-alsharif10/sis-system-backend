package com.sis.dto.studentEnrollment;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class StudentEnrollmentRequestDTO {
    private String searchValue;
    private Long filterCollege;
    private Long filterDepartment;
    private Long filterAcademicYear;
    private Long filterAcademicTerm;
    private Long filterCourse;
    private Long filterStudent;
    private Long filterStudyType;
    private Long filterSection;
    private Long filterMajor;
    private String sortDirection;
    private String sortBy;

}
