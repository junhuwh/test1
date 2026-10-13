package com.bookworm.dto.response;

import com.bookworm.entity.enums.Role;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    private String email;
    private String fullName;
    private Role role;
    private Integer giftPoints;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Integer getGiftPoints() { return giftPoints; }
    public void setGiftPoints(Integer giftPoints) { this.giftPoints = giftPoints; }
}
