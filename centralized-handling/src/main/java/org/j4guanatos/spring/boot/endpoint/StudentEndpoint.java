package org.j4guanatos.spring.boot.endpoint;

import java.util.Collections;
import java.util.Map;

import javax.validation.Valid;

import org.j4guanatos.spring.boot.dto.StudentDto;
import org.j4guanatos.spring.boot.error.DuplicatedDbValue;
import org.j4guanatos.spring.boot.error.ResourceNotFoundException;
import org.j4guanatos.spring.boot.mapper.Mapper;
import org.j4guanatos.spring.boot.model.Student;
import org.j4guanatos.spring.boot.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/student")
public class StudentEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(StudentEndpoint.class);

	@Autowired
	private Mapper<StudentDto, Student> studentMapper;

	@Autowired
	private StudentRepository studentRepository;

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.CREATED)
	public StudentDto createStudent(@Valid @RequestBody StudentDto studentDto) {
		Student student = studentMapper.toModel(studentDto);
		try {
			student = studentRepository.insert(student);
			logger.debug("student: {}", student);
		} catch (DuplicateKeyException dke) {
			logger.info("Duplicate key id: {}; Exception", student.getId(), dke);
			throw new DuplicatedDbValue("id", student.getId().toString());
		}

		return studentDto;
	}

	@GetMapping(value = "/{studentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public StudentDto retrieveStudent(@PathVariable Long studentId) {
		return studentMapper.toDto(studentRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException("Student", "id", studentId.toString())));
	}

	@PutMapping(value = "/{studentId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public StudentDto updateStudent(@Valid @RequestBody StudentDto studentDto, @PathVariable Long studentId) {
		Student student = studentMapper.toModel(studentDto);
		if (!studentRepository.existsById(studentId)) {
			logger.info("Student with id:{} not found", studentId);
			throw new ResourceNotFoundException("Student", "id", studentId.toString());
		}
		student.setId(studentId);
		studentRepository.save(student);

		return studentMapper.toDto(student);
	}

	@DeleteMapping(value = "/{studentId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public Map<String, Boolean> deleteStudent(@PathVariable Long studentId) {
		if (!studentRepository.existsById(studentId)) {
			logger.info("Student with id:{} not found", studentId);
			throw new ResourceNotFoundException("Student", "id", studentId.toString());
		}
		studentRepository.deleteById(studentId);
		return Collections.singletonMap("success", true);
	}

}
