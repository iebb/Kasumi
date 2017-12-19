package md.i0.krfam;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "m_ConfirmedVer", "m_UUID", "m_AccessToken", "m_MyCode" })
public class SaveData
{
    public Integer m_ConfirmedVer;
    public String m_UUID;
    public String m_AccessToken;
    public String m_MyCode;
    public SaveData() {
        super();
    }
}