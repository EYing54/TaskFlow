package com.taskflow.backend.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.backend.dto.LoginRequest;
import com.taskflow.backend.entity.User;
import com.taskflow.backend.service.UserService;

@RestController
@CrossOrigin(origins = {
                "http://localhost:5173",
                "http://127.0.0.1:5173"
}, allowCredentials = "true")
public class AuthController {

        private final UserService userService;

        public AuthController(UserService userService) {
                this.userService = userService;
        }

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletRequest httpRequest) {
                String username = loginRequest.username();
                String password = loginRequest.password();

                if (username == null || username.isBlank() || password == null || password.isBlank()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "用户名和密码不能为空"));
                }
                username = username.trim();

                if (!userService.login(username, password)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户名或密码错误"));
                }
                User user = userService.findByUsername(username);
                HttpSession session = httpRequest.getSession(true);
                session.setAttribute("userId", user.getId());
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "登录成功"));
        }

        @GetMapping("/auth/me")
        public ResponseEntity<?> currentUser(HttpServletRequest httpRequest) {
                HttpSession session = httpRequest.getSession(false);

                if (session == null || !(session.getAttribute("userId") instanceof Long userId)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "未登录"));
                }
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("userId", userId));
        }

        @PostMapping("/logout")
        public ResponseEntity<?> logout(HttpServletRequest httpRequest) {
                HttpSession session = httpRequest.getSession(false);

                if (session != null) {
                        session.invalidate();
                }
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "退出成功"));
        }
}
