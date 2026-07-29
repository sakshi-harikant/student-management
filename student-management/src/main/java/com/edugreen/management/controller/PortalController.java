package com.edugreen.management.controller;

import com.edugreen.management.model.Student;
import com.edugreen.management.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

@Controller
public class PortalController {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private SecurityConfig.PasswordHasher passwordHasher;
    

    @GetMapping("/")
    public String homePage() {
        return "index"; 
    }

    // ==========================================
    //       STUDENT AUTHENTICATION MAPPINGS
    // ==========================================
    
    @GetMapping("/student/login")
    public String studentLogin() {
        return "student-login"; 
    }

    @PostMapping("/student/login")
    public String processStudentLogin(@RequestParam String username,
                                      @RequestParam String password,
                                      HttpSession session,
                                      Model model) {
        java.util.Optional<Student> existingStudent = studentRepository.findByUsername(username);

        // 🆕 Change only the second part of this IF condition to use the passwordHasher
        if (existingStudent.isPresent() && passwordHasher.matches(password, existingStudent.get().getPassword())) {
            session.setAttribute("loggedInStudent", existingStudent.get());
            return "redirect:/student/dashboard";
        } else {
            model.addAttribute("error", "Invalid System Username or Security Password.");
            return "student-login";
        }
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(HttpSession session, Model model) {
        Student initialStudent = (Student) session.getAttribute("loggedInStudent");
        if (initialStudent == null) {
            return "redirect:/student/login";
        }
        
        // Re-fetch the student from the database so edited changes reflect instantly upon refresh
        Student freshStudentData = studentRepository.findById(initialStudent.getId()).orElse(initialStudent);
        session.setAttribute("loggedInStudent", freshStudentData);
        
        model.addAttribute("student", freshStudentData);
        return "student-dashboard"; 
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register"; 
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute Student student, Model model) {
        if (studentRepository.findByEmail(student.getEmail()).isPresent()) {
            model.addAttribute("error", "An account with this email address already exists.");
            return "register"; 
        }
        if (studentRepository.findByUsername(student.getUsername()).isPresent()) {
            model.addAttribute("error", "This username is already taken. Choose another.");
            return "register";
        }

        String uniqueId = "EG-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        student.setStudentId(uniqueId);
        
        // Ensure default fallback values are initialized
        if(student.getStatus() == null || student.getStatus().isEmpty()) {
            student.setStatus("Active");
        }
        
        // 🆕 SECURE PASSWORD BLOCK: Hash the plain text password before saving it
        String secureHash = passwordHasher.encode(student.getPassword());
        student.setPassword(secureHash);
        
        // Save the student with the secure hashed password
        studentRepository.save(student);
        return "redirect:/student/login?success=true";
    }
    @PostMapping("/student/profile/update")
    public String studentSelfUpdate(@RequestParam String email,
                                    @RequestParam String phone,
                                    @RequestParam String dob,
                                    @RequestParam String gender,
                                    @RequestParam String address,
                                    HttpSession session) {
        Student currentStudent = (Student) session.getAttribute("loggedInStudent");
        if (currentStudent == null) {
            return "redirect:/student/login";
        }

        // Update only allowed personal metrics
        currentStudent.setEmail(email);
        currentStudent.setPhone(phone);
        
        try {
            currentStudent.setDob(java.time.LocalDate.parse(dob));
        } catch (Exception e) {
            // Fallback or leave as-is if parsing issues occur
        }
        
        currentStudent.setGender(gender);
        currentStudent.setAddress(address);

        // Persist modifications seamlessly back into your database schema
        studentRepository.save(currentStudent);
        
        // Sync the updated state to the active session container
        session.setAttribute("loggedInStudent", currentStudent);

        return "redirect:/student/dashboard?success=true";
    }

    // ==========================================
    //        ADMIN MANAGEMENT MAPPINGS (CRUD)
    // ==========================================

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin-login"; 
    }

    @PostMapping("/admin/login")
    public String processAdminLogin(@RequestParam String username,
                                    @RequestParam String password,
                                    HttpSession session,
                                    Model model) {
        if ("admin".equals(username) && "admin123".equals(password)) {
            session.setAttribute("adminSession", "active");
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("error", "Invalid Master Administrative Credentials.");
            return "admin-login";
        }
    }

 // ========================================================
    // 1. YOUR ORIGINAL DASHBOARD MAPPING (WITH NULL-SAFE GUARD)
    // ========================================================
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) {
            return "redirect:/admin/login";
        }
        
        List<Student> allStudents = studentRepository.findAll();
        model.addAttribute("students", allStudents);
        
        // Dynamically compute card values based on actual database stats
        long totalStudentsCount = allStudents.size();
        long activeStudentsCount = allStudents.stream().filter(s -> "Active".equalsIgnoreCase(s.getStatus())).count();
        
        // Fixed: Added a null filter check here to prevent Whitelabel 500 crashes if a student has an empty/null department
        long uniqueDepartmentsCount = allStudents.stream()
                .filter(s -> s.getDepartment() != null)
                .map(s -> s.getDepartment().trim().toLowerCase())
                .distinct()
                .count();
        
        model.addAttribute("totalStudentsCount", totalStudentsCount);
        model.addAttribute("activeStudentsCount", activeStudentsCount);
        model.addAttribute("departmentsCount", uniqueDepartmentsCount > 0 ? uniqueDepartmentsCount : 0);

        // FETCH RECENT REGISTRATIONS FOR THE NOTIFICATION BOX
        List<Student> recentRegistrations = new java.util.ArrayList<>(allStudents);
        java.util.Collections.reverse(recentRegistrations); 
        
        if (recentRegistrations.size() > 5) {
            recentRegistrations = recentRegistrations.subList(0, 5);
        }
        
        model.addAttribute("recentRegistrations", recentRegistrations);
        model.addAttribute("newRegistrationsCount", recentRegistrations.size());
        model.addAttribute("newRegistrationsCount", totalStudentsCount); 

        // 🆕 Pass the safe highlight flag for your sidebar template
        model.addAttribute("activeTab", "dashboard");

        return "admin-dashboard";
    }

    // ========================================================
    // 2. NEW DEPARTMENTS PAGE MAPPING (SAFE TO ADD NEXT TO IT)
    // ========================================================
    @GetMapping("/admin/departments")
    public String adminDepartments(HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) {
            return "redirect:/admin/login";
        }

        List<Student> allStudents = studentRepository.findAll();

        // Group students by department smoothly while skipping any nulls
        java.util.Map<String, List<Student>> departmentGroupings = allStudents.stream()
                .filter(s -> s.getDepartment() != null && !s.getDepartment().trim().isEmpty())
                .collect(java.util.stream.Collectors.groupingBy(s -> s.getDepartment().trim()));

        model.addAttribute("departmentGroupings", departmentGroupings);
        
        // 🆕 Pass the highlight flag to illuminate the departments tab
        model.addAttribute("activeTab", "departments");

        return "admin-departments"; 
    }
    // Edit Form Route Entry Point
    @GetMapping("/admin/student/edit/{id}")
    public String showEditForm(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        
        Student targetStudent = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student identification key: " + id));
        model.addAttribute("student", targetStudent);
        return "admin-edit-student";
    }
    @PostMapping("/admin/student/update")
    public String updateStudentProfile(@ModelAttribute Student student, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        
        // Retrieve the original record from the database
        Student currentRecord = studentRepository.findById(student.getId()).orElse(null);
        
        if (currentRecord != null) {
            // If a field is missing from the edit screen, keep the existing value to prevent null pointer database crashes
            if (student.getAddress() == null || student.getAddress().isEmpty()) {
                student.setAddress(currentRecord.getAddress());
            }
            if (student.getDob() == null) {
                student.setDob(currentRecord.getDob());
            }
            if (student.getGender() == null || student.getGender().isEmpty()) {
                student.setGender(currentRecord.getGender());
            }
            if (student.getPassword() == null || student.getPassword().isEmpty()) {
                student.setPassword(currentRecord.getPassword());
            }
            if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
                student.setStudentId(currentRecord.getStudentId());
            }
        }
        
        studentRepository.save(student);
        return "redirect:/admin/dashboard?updated=true";
    }

    // Delete Record Endpoint Handling Sequence
    @GetMapping("/admin/student/delete/{id}")
    public String purgeStudentProfile(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("adminSession") == null) return "redirect:/admin/login";
        
        studentRepository.deleteById(id);
        return "redirect:/admin/dashboard?deleted=true";
    }
    
    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
 // ========================================================
    // 🆕 STUDENT FORGOT PASSWORD PROCESSOR
    // ========================================================
    @PostMapping("/student/forgot-password")
    public String handleStudentPasswordReset(
            @RequestParam("studentId") String studentId,
            @RequestParam("email") String email, 
            @RequestParam("newPassword") String newPassword,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        // Find the student by their ID from the database repository
        java.util.Optional<Student> studentOpt = studentRepository.findAll().stream()
                .filter(s -> studentId.equalsIgnoreCase(s.getStudentId()))
                .findFirst();

        if (studentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No student account found with that ID.");
            return "redirect:/student/forgot-password";
        }

        Student student = studentOpt.get();

        // Security check: Verify their registered email matches for identity authorization
        if (student.getEmail() == null || !student.getEmail().equalsIgnoreCase(email.trim())) {
            redirectAttributes.addFlashAttribute("error", "Verification details do not match our records.");
            return "redirect:/student/forgot-password";
        }

        // Hash the new password securely using our SHA-256 tool before persisting
        String secureHash = passwordHasher.encode(newPassword);
        student.setPassword(secureHash);
        
        // Save back to database
        studentRepository.save(student);

        redirectAttributes.addFlashAttribute("success", "Password reset successfully! Please log in.");
        return "redirect:/student/login";
    }

    // Serving the HTML view page configuration
    @GetMapping("/student/forgot-password")
    public String showForgotPasswordPage() {
        return "student-forgot-password";
    }
}