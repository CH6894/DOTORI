package com.pingu.DOTORI.controller;

import com.pingu.DOTORI.entity.Users;
import com.pingu.DOTORI.repository.UsersRepository;
import com.pingu.DOTORI.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wish")
@RequiredArgsConstructor
public class WishListController {
    private final WishListService service;
    private final UsersRepository usersRepository;

    public record WishReq(Long itemId) {}

    private Long currentUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }
        
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            String email = user.getUsername();
            Users users = usersRepository.findByEmail(email).orElse(null);
            return users != null ? users.getId() : null;
        }
        
        return null;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> add(@RequestBody WishReq req, Authentication auth) {
        Long userId = currentUserId(auth);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        service.add(userId, req.itemId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> remove(@RequestBody WishReq req, Authentication auth) {
        Long userId = currentUserId(auth);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        service.remove(userId, req.itemId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list(Authentication auth) {
        Long userId = currentUserId(auth);
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        var rows = service.listWithItemDetails(userId);
        return ResponseEntity.ok(rows);
    }
}
