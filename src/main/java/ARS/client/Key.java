package ARS.client;

public class Key {
    private String key;
    private String subTime;
    private Role role;
    public Key(String key, String subTime, Role role) {
        this.key = key;
        this.subTime = subTime;
        this.role = role;
    }
    public String getKey() {
        return key;
    }
    public String getSubTime() {
        return subTime;
    }
    public Role getRole() {
        return role;
    }
}
