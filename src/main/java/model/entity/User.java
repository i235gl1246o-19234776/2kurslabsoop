/*package model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String passwordHash;

    public User(String name, String passwordHash) {
        this.name = name;
        this.passwordHash = passwordHash;
    }
}*/
package model.entity;

public class User {
    private Long id;
    private String name;
    private String passwordHash;
    private UserRole role;

    public User() {
    }

    public User(String name, String passwordHash) {
        this.name = name;
        this.passwordHash = passwordHash;
    }

    public User(Long id, String name, String passwordHash) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != null ? !id.equals(user.id) : user.id != null) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        return passwordHash != null ? passwordHash.equals(user.passwordHash) : user.passwordHash == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (passwordHash != null ? passwordHash.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                '}';
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole newRole) {
        role = newRole;
    }
}