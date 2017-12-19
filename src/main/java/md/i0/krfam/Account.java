package md.i0.krfam;

public class Account {
    public String name;
    public long id;
    public long loaded;
    public boolean isFolder;
    public String server;
    public long parentFolder;
    public boolean locked = false;
    public String u_UUID;
    public String u_Pass;
    public String u_Code;
    public long accountCount;
    public boolean isCurrent() {
        for (Server s : KRFAM.serverList) {
            if (s.codeName.equals(server)) {
                if (s.uuid == null || s.accessToken == null) {
                    return false;
                }
                if (s.uuid.equals(u_UUID) && s.accessToken.equals(u_Pass)) {
                    return true;
                }
            }
        }
        return false;
    }
}
