namespace('planningPoker.GuestService', {
    'gizmo-atheneum.namespaces.Ajax':'Ajax'
}, ({ Ajax }) => {
    const setSessionWrapper = function(setSession) {
        return ((respText) => {
            setSession(JSON.parse(respText));
        });
    };
    const join = function(guestName, sessionKey, setSession) {
        Ajax.request("POST",`/SharedSession/Join/${sessionKey}`,{
            key:"",
            nameTag:guestName,
            lastModified:0,
            joined:0,
            state:{}
        },setSessionWrapper(setSession));
    };
    const leave = function(sessionId, setSession) {
        Ajax.request("DELETE",`/SharedSession/${sessionId}/Leave}`, setSessionWrapper(setSession));
    };
    const getData = function(sessionId, setSession) {
        Ajax.request("GET",`/SharedSession/${sessionId}/GuestData`, setSessionWrapper(setSession));
    }
    const postData = function(sessionId, session, setSession) {
        Ajax.request("POST", `/SharedSession/${sessionId}/GuestData`, session, setSessionWrapper(setSession));
    }
    return { join, leave, getData, postData };
});