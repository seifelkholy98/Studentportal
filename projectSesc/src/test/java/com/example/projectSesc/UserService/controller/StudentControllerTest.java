package com.example.projectSesc.UserService.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import com.example.projectSesc.CourseService.domain.Course;
import com.example.projectSesc.CourseService.repository.CourseRepository;
import com.example.projectSesc.EnrollmentService.domain.Enrollment;
import com.example.projectSesc.EnrollmentService.repository.EnrollmentRepository;
import com.example.projectSesc.UserService.domain.Student;
import com.example.projectSesc.UserService.repository.StudentRepository;
import com.example.projectSesc.UserService.service.StudentService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.MethodName.class)
@Transactional
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentService studentService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder ;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentController studentController;

    private MockHttpSession session;

    private Student student;

    @BeforeAll
    static void setUpBeforeAll() {

    }
    @BeforeEach
    void setUp() {
            student = new Student();
            student.setStudentId(1L);
            student.setUsername("john_doe");
            student.setPassword("password123");
            student.setEmail("john.doe@example.com");
            student.setRole("ROLE_USER");
            student.setSurname("Doe");
            student.setStudentCode("c1234567");
            student.setEnrollments(new ArrayList<>());



        Optional<Student> existingStudent = studentRepository.findByUsername(student.getUsername());
        if (!existingStudent.isPresent()) {
            Student save = studentRepository.save(student);
            String saved = save.toString();
        }

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                student.getUsername(),
                student.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authToken);
        when(securityContext.getAuthentication()).thenReturn(authToken);
        SecurityContextHolder.setContext(securityContext);
        session = new MockHttpSession();
    }

    @Test
    @WithMockUser(username="john_doe")
    public void testRegisterUserSuccess() throws Exception {
        if(studentRepository.findByUsername(student.getUsername()).isPresent())
            studentRepository.delete(student);

        mockMvc.perform(post("/api/students/register")
                .session(session)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", student.getUsername())
                .param("password", student.getPassword())
                .param("email", student.getEmail())
                .param("surname", student.getSurname()))
                .andExpect(status().is3xxRedirection())
                .andDo(print())
                .andExpect(redirectedUrl("/courses"));

    }

    @Test
    public void testLoginSuccess() throws Exception {

        mockMvc.perform(post("/api/students/login")
                .param("username", "john_doe")
                .param("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andDo(print());
    }

    @Test
    @WithMockUser(username="john_doe")
    public void testGetProfile() throws Exception {
        mockMvc.perform(get("/api/students/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "john_doe")
    public void testUpdateProfile() throws Exception {
        mockMvc.perform(put("/api/students/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"new.john.doe@example.com\",\"surname\":\"NewDoe\"}"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithMockUser
    public void testDeleteProfile() throws Exception {

        mockMvc.perform(delete("/api/students/profile"))
                .andExpect(status().isNoContent())
                .andDo(print());
    }
    @Test
    public void testStudentExists() throws Exception {

        mockMvc.perform(get("/api/students/exists/" + student.getUsername()))
                .andExpect(status().isOk())
                .andExpect(content().string("true")).andDo(print());
    }

}
