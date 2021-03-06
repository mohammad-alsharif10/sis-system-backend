package com.sis.controller;

import com.sis.dto.AcademicTermDTO;
import com.sis.dto.attendanceReport.FacultyMemberLecturesDTO;
import com.sis.dto.lecture.LectureDTO;
import com.sis.entity.*;
import com.sis.entity.mapper.*;
import com.sis.service.AcademicTermService;
import com.sis.service.AttendanceDetailsService;
import com.sis.service.LectureService;
import com.sis.service.SectionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/lectures")
@CrossOrigin()
@AllArgsConstructor
public class LectureController extends BaseController<Lecture, LectureDTO> {

    private LectureService lectureService;

    private LectureMapper lectureMapper;

    private SectionService sectionService;

    private AcademicTermService academicTermService;

    private AcademicTermMapper academicTermMapper;

    private AcademicYearMapper academicYearMapper;

    private AttendanceDetailsService attendanceDetailsService;

    private CourseMapper courseMapper;
    private FacultyMemberMapper facultyMemberMapper;

    @RequestMapping(value = "/addLecture", method = RequestMethod.POST)
    public ResponseEntity<LectureDTO> addLecture(@RequestBody LectureDTO lectureDTO) {
        Course course = this.courseMapper.toEntity(lectureDTO.getCourseDTO());
        FacultyMember facultyMember = this.facultyMemberMapper.toEntity(lectureDTO.getFacultyMemberDTO());

        AcademicTerm academicTerm = this.academicTermService.getCurrentAcademicTerm();
        AcademicTermDTO academicTermDTO = this.academicTermMapper.toDTO(academicTerm);

        lectureDTO.setAcademicTermDTO(academicTermDTO);
        lectureDTO.setAcademicYearDTO(this.academicYearMapper.toDTO(academicTerm.getAcademicYear()));

        if (!lectureDTO.getAttendanceType().equalsIgnoreCase("Manual")) {
            Random rand = new Random();
            lectureDTO.setAttendanceCode(rand.nextInt());

        }
        boolean isFound = true;
        LectureDTO lectureDTO1 = this.lectureService.searchLecture(lectureDTO.getSectionDTO().getId(),lectureDTO.getLectureDate(),course
                , facultyMember, lectureDTO.getLectureStartTime(), lectureDTO.getLectureEndTime());
        if (lectureDTO1 == null) {
            isFound = false;
        } else {
            lectureDTO.setId(lectureDTO1.getId());
        }

        Lecture lecture = this.lectureMapper.toEntity(lectureDTO);
        LectureDTO lectureDTO2 = this.lectureMapper.toDTO(this.lectureService.save(lecture));

        if (!isFound) {
            this.attendanceDetailsService.saveAttendances(lectureDTO2);
        }
        return new ResponseEntity<>(lectureDTO2, HttpStatus.OK);
    }

    @RequestMapping(value = "/getCurrentLecture/{studentId}", method = RequestMethod.GET)
    public ResponseEntity<Collection<LectureDTO>> getCurrentLectures(@PathVariable long studentId) {

        AcademicTerm academicTerm = this.academicTermService.getCurrentAcademicTerm();
        Collection<Section> sections = this.sectionService.findStudentSections(academicTerm.getAcademicYear(), academicTerm, studentId);
        Collection<LectureDTO> lectureDTOs = new ArrayList<>();
        for (Section sec : sections) {
            lectureDTOs.addAll(this.lectureMapper.toDTOs(sec.getLectures()));
        }
        lectureDTOs = lectureDTOs.stream().filter(lectureDTO -> lectureDTO.getAttendanceStatus() && lectureDTO.getAttendanceType().equalsIgnoreCase("Automatic")).collect(Collectors.toList());
        return new ResponseEntity<>(lectureDTOs, HttpStatus.OK);
    }


    @RequestMapping(value = "/getFacultyMemberLectures/{sectionId}", method = RequestMethod.GET)
    public ResponseEntity<Collection<LectureDTO>> getFacultyMemberLectures(@PathVariable long sectionId) {
        AcademicTerm academicTerm = this.academicTermService.getCurrentAcademicTerm();
        AcademicTermDTO academicTermDTO = this.academicTermMapper.toDTO(academicTerm);
        ArrayList<LectureDTO> lectureDTOs = this.lectureService.
                getFacultyMemberLectures(academicTermDTO.getAcademicYearDTO().getId(), academicTermDTO.getId(), sectionId);
        return new ResponseEntity<>(lectureDTOs, HttpStatus.OK);
    }

    // this function is written by Abdo Ramadan
    @RequestMapping(value = "/getFacultyMemberLecturesToReport/{sectionId}",
            method = RequestMethod.GET)
    public ResponseEntity<Collection<FacultyMemberLecturesDTO>> getFacultyMemberLecturesToReport(
            @PathVariable long sectionId) {
        AcademicTerm academicTerm = this.academicTermService.getCurrentAcademicTerm();
        AcademicTermDTO academicTermDTO = this.academicTermMapper.toDTO(academicTerm);
        ArrayList<FacultyMemberLecturesDTO> facultyMemberLecturesDTOS =
                this.lectureService.getFacultyMemberLecturesToReport(
                        academicTermDTO.getAcademicYearDTO().getId(), academicTermDTO.getId(), sectionId);
        return new ResponseEntity<>(facultyMemberLecturesDTOS, HttpStatus.OK);
    }

}
