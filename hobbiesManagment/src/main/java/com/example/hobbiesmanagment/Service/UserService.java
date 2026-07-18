package com.example.hobbiesmanagment.Service;

import com.example.hobbiesmanagment.DTO.AuthResponse;
import com.example.hobbiesmanagment.DTO.UserDto;
import com.example.hobbiesmanagment.DTO.UserRegistrationDto;
import com.example.hobbiesmanagment.DTO.UserUpdateDto;
import com.example.hobbiesmanagment.Entities.*;
import com.example.hobbiesmanagment.Exception.DuplicateResourceException;
import com.example.hobbiesmanagment.Exception.InvalidCredentialsException;
import com.example.hobbiesmanagment.Exception.ResourceNotFoundException;
import com.example.hobbiesmanagment.Jwt.JwtUtil;
import com.example.hobbiesmanagment.Repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;


    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return UserDto.fromEntity(user);
    }
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return UserDto.fromEntity(user);
    }
    @Transactional
    public void removeSkillFromUser(Long id, String skillName) {
        MentorProfile mentor = mentorProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        Skill skill = skillRepository.findByName(skillName);
        if (skill == null) {
            throw new ResourceNotFoundException("Skill not found");
        }

        mentor.getSkills().remove(skill);
        mentorProfileRepository.save(mentor);
    }
    @Transactional
    public AuthResponse UpdateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (userUpdateDto.getPassword() != null && !userUpdateDto.getPassword().isEmpty()) {
            user.getCredential().setPassword(passwordEncoder.encode(userUpdateDto.getPassword()));
        }
        if (userUpdateDto.getEmail() != null) user.setEmail(userUpdateDto.getEmail());
        if (userUpdateDto.getCity() != null) user.setCity(userUpdateDto.getCity());
        if (userUpdateDto.getPhone() != null) user.setPhone(userUpdateDto.getPhone());
        if (userUpdateDto.getDescription() != null) user.setDescription(userUpdateDto.getDescription());
        if (userUpdateDto.getGender() != null) user.setGender(userUpdateDto.getGender());
        if (userUpdateDto.getName() != null) user.getCredential().setName(userUpdateDto.getName());

        // Update role, if changed
        String newRole = userUpdateDto.getSelectedRole();
        if (newRole != null && !newRole.isBlank()) {
            user.getRole().clear();

            if ("MENTOR_AND_LEARNER".equalsIgnoreCase(newRole)) {
                addRoleToUser(user, RoleName.MENTOR);
                addRoleToUser(user, RoleName.LEARNER);
                if (!mentorProfileRepository.existsByUser(user)) {
                    mentorProfileRepository.save(MentorProfile.builder()
                            .user(user).maxStudents(0).currentStudents(0)
                            .skills(new java.util.HashSet<>()).build());
                }
                if (!learnerProfileRepository.existsByUser(user)) {
                    learnerProfileRepository.save(LearnerProfile.builder()
                            .user(user).hobbies(new java.util.HashSet<>())
                            .favoriteSkills(new java.util.HashSet<>())
                            .sessions(new java.util.HashSet<>()).build());
                }
            } else if ("MENTOR".equalsIgnoreCase(newRole)) {
                addRoleToUser(user, RoleName.MENTOR);
                if (!mentorProfileRepository.existsByUser(user)) {
                    mentorProfileRepository.save(MentorProfile.builder()
                            .user(user).maxStudents(0).currentStudents(0)
                            .skills(new java.util.HashSet<>()).build());
                }
            } else {
                addRoleToUser(user, RoleName.LEARNER);
                if (!learnerProfileRepository.existsByUser(user)) {
                    learnerProfileRepository.save(LearnerProfile.builder()
                            .user(user).hobbies(new java.util.HashSet<>())
                            .favoriteSkills(new java.util.HashSet<>())
                            .sessions(new java.util.HashSet<>()).build());
                }
            }
        }

        User updatedUser = userRepository.save(user);

        // Build a new token with the updated roles
        List<String> roles = updatedUser.getRole().stream()
                .map(role -> role.getName().name().toUpperCase())
                .collect(Collectors.toList());

        // Determine the role string based on the role combination
        String roleString;
        boolean hasMentor = roles.contains("MENTOR");
        boolean hasLearner = roles.contains("LEARNER");
        if (hasMentor && hasLearner) roleString = "MENTOR_AND_LEARNER";
        else if (hasMentor) roleString = "MENTOR";
        else roleString = "LEARNER";

        String token = jwtUtil.generateToken(updatedUser.getEmail(), roles);
        return AuthResponse.builder()
                .token(token)
                .role(roleString)
                .email(updatedUser.getEmail())
                .hasProfile(true)
                .userId(updatedUser.getId())
                .build();
    }
    private void addRoleToUser(User user, RoleName roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));
        user.getRole().add(role);
    }
    public User getUserByUsername(String username) {
        return userRepository.findByCredentialName(username);
    }
    // Converts a registration DTO into a User entity
    public User fromDto(UserRegistrationDto dto) {
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        String roleStr = dto.getSelectedRole();

        java.util.Set<Role> userRoles = new HashSet<>();
        if ("MENTOR_AND_LEARNER".equalsIgnoreCase(roleStr)) {
            Role mentorRole = roleRepository.findByName(RoleName.MENTOR)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.MENTOR).build()));
            Role learnerRole = roleRepository.findByName(RoleName.LEARNER)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.LEARNER).build()));
            userRoles.add(mentorRole);
            userRoles.add(learnerRole);
        } else {
            RoleName roleEnum = RoleName.valueOf(roleStr);
            Role role = roleRepository.findByName(roleEnum)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found."));
            userRoles.add(role);
        }

        return User.builder()
                .credential(new Credential(dto.getName(), encodedPassword))
                .email(dto.getEmail())
                .city(dto.getCity())
                .phone(dto.getPhone())
                .id(Long.parseLong(dto.getIdNumber()))
                .birthday(dto.getBirthday())
                .gender(dto.getGender())
                .description(dto.getDescription())
                .role(userRoles)
                .build();
    }

   // Registers a new user in the system
    @Transactional
    public AuthResponse register(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = fromDto(dto);
        User savedUser = userRepository.save(user);

        List<String> roles = user.getRole().stream()
                .map(role -> role.getName().name().toUpperCase())
                .collect(Collectors.toList());

        String latestRole = roles.isEmpty() ? "" : roles.get(roles.size() - 1);
        boolean isMentor = roles.contains("MENTOR");
        boolean isLearner = roles.contains("LEARNER");
        String roleString;
        if (isMentor && isLearner) roleString = "MENTOR_AND_LEARNER";
        else if (isMentor) roleString = "MENTOR";
        else roleString = "LEARNER";

        if (isMentor) {
            MentorProfile mentorProfile = MentorProfile.builder()
                    .user(savedUser)
                    .maxStudents(0)
                    .currentStudents(0)
                    .skills(new HashSet<>())
                    .build();
            mentorProfileRepository.save(mentorProfile);
        }

        if (isLearner) {
            LearnerProfile learnerProfile = LearnerProfile.builder()
                    .user(savedUser)
                    .hobbies(new HashSet<>())
                    .favoriteSkills(new HashSet<>())
                    .sessions(new HashSet<>())
                    .build();
            learnerProfileRepository.save(learnerProfile);
        }

        boolean hasProfile = isMentor
                ? mentorProfileRepository.existsByUser(savedUser)
                : learnerProfileRepository.existsByUser(savedUser);

        String token = jwtUtil.generateToken(savedUser.getEmail(), roles);
        return AuthResponse.builder()
                .token(token)
                .role(roleString)
                .email(savedUser.getEmail())
                .hasProfile(hasProfile)
                .userId(savedUser.getId())
                .build();
    }
    // Logs a user into the system
    public AuthResponse login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Incorrect email or password"));

        if (!passwordEncoder.matches(password, user.getCredential().getPassword())) {
            throw new InvalidCredentialsException("Incorrect email or password");
        }

        List<String> roles = user.getRole().stream()
                .map(role -> role.getName().name().toUpperCase())
                .collect(Collectors.toList());

        boolean hasProfile = false;
        if (roles.contains("MENTOR")) {
            hasProfile = mentorProfileRepository.existsByUser(user);
        } else if (roles.contains("LEARNER")) {
            hasProfile = learnerProfileRepository.existsByUser(user);
        }

        // Determine the correct role string
        boolean hasMentor = roles.contains("MENTOR");
        boolean hasLearner = roles.contains("LEARNER");
        String roleString;
        if (hasMentor && hasLearner) roleString = "MENTOR_AND_LEARNER";
        else if (hasMentor) roleString = "MENTOR";
        else roleString = "LEARNER";

        String token = jwtUtil.generateToken(user.getEmail(), roles);
        return AuthResponse.builder()
                .token(token)
                .role(roleString)
                .email(user.getEmail())
                .hasProfile(hasProfile)
                .userId(user.getId())
                .build();
    }

    public User save (User user){
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }



}