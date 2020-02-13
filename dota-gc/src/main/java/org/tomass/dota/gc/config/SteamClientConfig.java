package org.tomass.dota.gc.config;

public class SteamClientConfig {

    private String user;

    private String pass;

    private String authCode;

    private String twoFactorAuth;

    private String loginKey;

    private String sentry;

    private String personaName;

    private boolean connectOnStart;

    private boolean reconnectOnDisconnect;

    private int partyCountMembers;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getTwoFactorAuth() {
        return twoFactorAuth;
    }

    public void setTwoFactorAuth(String twoFactorAuth) {
        this.twoFactorAuth = twoFactorAuth;
    }

    public String getLoginKey() {
        return loginKey;
    }

    public void setLoginKey(String loginKey) {
        this.loginKey = loginKey;
    }

    public String getSentry() {
        return sentry;
    }

    public void setSentry(String sentry) {
        this.sentry = sentry;
    }

    public boolean isConnectOnStart() {
        return connectOnStart;
    }

    public void setConnectOnStart(boolean connectOnStart) {
        this.connectOnStart = connectOnStart;
    }

    public boolean isReconnectOnDisconnect() {
        return reconnectOnDisconnect;
    }

    public void setReconnectOnDisconnect(boolean reconnectOnDisconnect) {
        this.reconnectOnDisconnect = reconnectOnDisconnect;
    }

    public String getPersonaName() {
        return personaName;
    }

    public void setPersonaName(String personaName) {
        this.personaName = personaName;
    }

    public int getPartyCountMembers() {
        return partyCountMembers;
    }

    public void setPartyCountMembers(int partyCountMembers) {
        this.partyCountMembers = partyCountMembers;
    }

    @Override
    public String toString() {
        return "AppConfig [user=" + user + ", pass=*****" + ", authCode=" + authCode + ", twoFactorAuth="
                + twoFactorAuth + ", loginKey=" + loginKey + ", sentry=" + sentry + ", connectOnStart=" + connectOnStart
                + ", reconnectOnDisconnect=" + reconnectOnDisconnect + "]";
    }

}