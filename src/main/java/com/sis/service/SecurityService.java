package com.sis.service;

import com.sis.dto.AdminDto;
import com.sis.dto.BaseDTO;
import com.sis.dto.facultyMember.FacultyMemberDTO;
import com.sis.dto.security.LoginDTO;
import com.sis.dto.security.RegisterDTO;
import com.sis.dto.student.StudentDTO;
import com.sis.entity.FacultyMember;
import com.sis.entity.Student;
import com.sis.entity.mapper.FacultyMemberMapper;
import com.sis.entity.mapper.StudentMapper;
import com.sis.entity.mapper.UserMapper;
import com.sis.entity.security.User;
import com.sis.exception.InvalidUserNameOrPasswordException;
import com.sis.repository.FacultyMemberRepository;
import com.sis.repository.RoleRepository;
import com.sis.repository.StudentRepository;
import com.sis.repository.UserRepository;
import com.sis.security.JwtProvider;
import com.sis.util.Constants;
import com.sis.util.MessageResponse;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@AllArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final StudentRepository studentRepository;
    private final FacultyMemberRepository facultyMemberRepository;
    private final StudentMapper studentMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FacultyMemberMapper facultyMemberMapper;
    private final CollegeService collegeService;
    private final DepartmentService departmentService;

    private final UserMapper userMapper;

    private final RedisTemplate<String, Map<String, Long>> redisTemplate;

    public ResponseEntity<StudentDTO> registerStudent1(StudentDTO studentDTO) {
        Student student = studentMapper.toEntity(studentDTO);
        User user = student.getUser();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(roleRepository.getRoleStudent());
        student.setUser(userRepository.save(user));
        return new ResponseEntity<>(studentMapper.toDTO(studentRepository.save(student)), HttpStatus.OK);
    }

    public ResponseEntity<MessageResponse> registerStudent(RegisterDTO registerDTO) {
        Optional<Student> optionalStudent = studentRepository.findByNationalId(registerDTO.getNationalityID());
        if (optionalStudent.isEmpty()) {
            // the student is not in the system
            return new ResponseEntity<>(MessageResponse.builder().message("You are not in the system contact The administrator").build(), HttpStatus.BAD_REQUEST);
        } else if (optionalStudent.get().getUser() != null) {
            // student already registered
            return new ResponseEntity<>(MessageResponse.builder().message("Already registered go to login").build(), HttpStatus.BAD_REQUEST);
        } else {
            return registerStudent(registerDTO, optionalStudent.get());
        }
    }

    private ResponseEntity<MessageResponse> registerStudent(RegisterDTO registerDTO, Student student) {
        if (userRepository.findByEmailOrUsername(registerDTO.getUsername(), registerDTO.getUsername()).isPresent()) {
            return new ResponseEntity<>(MessageResponse.builder().message("email already taken").build(), HttpStatus.BAD_REQUEST);
        }
        User user = User.builder()
                .firstname(student.getNameEn())
                .email(registerDTO.getUsername())
                .username(registerDTO.getUsername())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .role(roleRepository.getRoleStudent())
                .type(Constants.TYPE_STUDENT).build();
        student.setUser(userRepository.save(user));
        studentRepository.save(student);
        return new ResponseEntity<>(MessageResponse.builder().message("registered Successfully").build(), HttpStatus.OK);
    }

    public ResponseEntity<FacultyMemberDTO> registerFacultyMember(FacultyMemberDTO facultyMemberDTO) {
        return null;
    }

    public ResponseEntity<BaseDTO> login(LoginDTO loginDto) {
        User user = userRepository.findByEmailOrUsername(loginDto.getUsername(), loginDto.getUsername())
                .orElseThrow(InvalidUserNameOrPasswordException::new);
        generateToken(loginDto, user);
        return collectUserData(user);
    }

    private ResponseEntity<BaseDTO> collectUserData(User user) {
        if (user.getType().equals(Constants.TYPE_STUDENT)) {
            return new ResponseEntity<>(studentMapper.toDTO(studentRepository.findByUser_Id(user.getId()).orElse(new Student())), HttpStatus.OK);
        } else if (user.getType().equals(Constants.TYPE_FACULTY_MEMBER)) {
            return new ResponseEntity<>(facultyMemberMapper.toDTO(facultyMemberRepository.findByUser_Id(user.getId()).orElse(new FacultyMember())), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new AdminDto(userMapper.toDTO(user)), HttpStatus.OK);
        }
    }

    private void generateToken(LoginDTO loginDto, User user) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
        } catch (AuthenticationException authenticationException) {
            throw new InvalidUserNameOrPasswordException();
        }
        user.setToken(Optional.of(jwtProvider.createToken(user.getUsername(), Collections.singletonList(user.getRole())))
                .orElseThrow(InvalidUserNameOrPasswordException::new));
    }

    public ResponseEntity<StudentDTO> registerBulkStudents(MultipartFile file) {
        Map<String, Long> departmentsMap = departmentService.cashAllDepartments();
        Map<String, Long> collegesMap = collegeService.cashAllColleges();
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            sheet.removeRow(sheet.iterator().next());
            processRows(StreamSupport.stream(sheet.spliterator(), false).collect(Collectors.toList()), departmentsMap, collegesMap).
                    forEach(studentDTO -> studentRepository
                            .saveNativeStudent(studentDTO.getNameAr(), studentDTO.getNameEn(), studentDTO.getNationality(),
                                    studentDTO.getNationalId(), studentDTO.getCollegeID(), studentDTO.getDepartmentID(), studentDTO.getUniversityId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<StudentDTO> processRows(List<Row> collect, Map<String, Long> departmentsMap, Map<String, Long> collegesMap) {
        List<StudentDTO> studentList = new ArrayList<>();
        for (Row row : collect) {
            studentList.add(StudentDTO.builder()
                    .nameAr(row.getCell(0).getStringCellValue())
                    .nameEn(row.getCell(1).getStringCellValue())
                    .nationality(row.getCell(2).getStringCellValue())
                    .nationalId(row.getCell(3).getStringCellValue())
                    .birthDate(Date.from(Instant.now()))
                    .collegeID(collegesMap.get(row.getCell(5).getStringCellValue()))
                    .departmentID(departmentsMap.get(row.getCell(6).getStringCellValue()))
                    .universityId(0)
                    .build());
        }
        return studentList;
    }
}
