package com.sis.entities.mapper;

import com.sis.dto.attendanceDetails.AttendanceDetailsDTO;
import com.sis.dto.lecture.LectureDTO;
import com.sis.entities.AttendanceDetails;
import com.sis.entities.Lecture;
import com.sis.entities.Student;
import com.sis.util.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.stream.Collectors.toCollection;

@Component

public class AttendanceDetailsMapper implements Mapper<AttendanceDetails, AttendanceDetailsDTO>{

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private LectureMapper lectureMapper;

    @Override
    public AttendanceDetailsDTO toDTO(AttendanceDetails entity) {
        AttendanceDetailsDTO attendanceDetailsDTO = AttendanceDetailsDTO.builder()
                .attendanceStatus(entity.getAttendanceStatus())
                .attendanceDate(entity.getAttendanceDate())
                .lectureStartTime(entity.getLectureStartTime())
                .lectureEndTime(entity.getLectureEndTime())
                .build();
        attendanceDetailsDTO.setId(entity.getId());

        if (entity.getStudents() != null) {
            attendanceDetailsDTO.setStudentDTOs(this.studentMapper.toDTOs(entity.getStudents()));
        }
        if (entity.getLectures() != null) {
            attendanceDetailsDTO.setLectureDTOs(this.lectureMapper.toDTOs(entity.getLectures()));
        }

        return attendanceDetailsDTO;
    }

    @Override
    public AttendanceDetails toEntity(AttendanceDetailsDTO dto) {
            AttendanceDetails attendanceDetails = new AttendanceDetails();
            if(dto != null) {
                attendanceDetails.setAttendanceStatus(dto.getAttendanceStatus());
                attendanceDetails.setAttendanceDate(dto.getAttendanceDate());
                attendanceDetails.setLectureStartTime(dto.getLectureStartTime());
                attendanceDetails.setLectureEndTime(dto.getLectureEndTime());
                attendanceDetails.setId(dto.getId());
                if (dto.getStudentDTOs() != null) {
                    attendanceDetails.setStudents(this.studentMapper.toEntities(dto.getStudentDTOs()));
                }
                if (dto.getLectureDTOs() != null) {
                    attendanceDetails.setLectures(this.lectureMapper.toEntities(dto.getLectureDTOs()));
                }

            }
            return attendanceDetails;
        }

    @Override
    public ArrayList<AttendanceDetailsDTO> toDTOs(Collection<AttendanceDetails> attendanceDetails) {
        return attendanceDetails.stream().map(entity -> toDTO(entity)).collect(toCollection(ArrayList<AttendanceDetailsDTO>::new));
    }

    @Override
    public ArrayList<AttendanceDetails> toEntities(Collection<AttendanceDetailsDTO> attendanceDetailsDTOS) {
        return attendanceDetailsDTOS.stream().map(dto -> toEntity(dto)).collect(toCollection(ArrayList<AttendanceDetails>::new));
    }

    @Override
    public PageResult<AttendanceDetailsDTO> toDataPage(PageResult<AttendanceDetails> entities) {
        return new PageResult<>(entities.getData().stream().map(entity -> toDTO(entity)).collect(toCollection(ArrayList<AttendanceDetailsDTO>::new)), entities.getTotalCount(), entities.getPageSize(), entities.getCurrPage());
    }
}
