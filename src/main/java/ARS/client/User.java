package ARS.client;

public class User {
    private String name;
    private int uid;
    private String hwid;
    private Role role;
    private String subTime;
    private String path;
    private float memory;

    public User(String name, int uid, String hwid, Role role, String subTime, String path, float memory) {
        this.name = name;
        this.uid = uid;
        this.hwid = hwid;
        this.role = role;
        this.subTime = subTime;
        this.path = path;
        this.memory = memory;
    }

    public String getName() {
        return name;
    }

    public String getHwid() {
        return hwid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setHwid(String hwid) {
        this.hwid = hwid;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public float getMemory() {
        return memory;
    }

    public void setMemory(float memory) {
        this.memory = memory;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSubTime() {
        return subTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", uid=" + uid +
                ", hwid='" + hwid + '\'' +
                ", role=" + role +
                ", subTime='" + subTime + '\'' +
                ", path='" + path + '\'' +
                ", memory=" + memory +
                '}';
    }
}
