namespace('planningPoker.HostService', {
    'gizmo-atheneum.namespaces.Ajax':'Ajax'
}, ({ Ajax }) => {
    const setSessionWrapper = function(setSession) {
        return ((respText) => {
            setSession(JSON.parse(respText));
        });
    };
    const host = function(hostName, setSession) {
        Ajax.request("POST","/SharedSession/Host",{
            id:"",
            key:"",
            host:{
                key:"",
                nameTag:hostName,
                lastModified:0,
                state:{}
            },
            context:{},
            guests:{}
        },setSessionWrapper(setSession));
    };
    const kick = function(sessionId, guestKey, setSession) {
        Ajax.request("DELETE",`/SharedSession/${sessionId}/Kick/${guestKey}}`, setSessionWrapper(setSession));
    };
    const close = function(sessionId, setSession) {
        Ajax.request("DELETE",`/SharedSession/${sessionId}/Close`, setSessionWrapper(setSession));
    };
    const getData = function(sessionId, setSession) {
        Ajax.request("GET",`/SharedSession/${sessionId}/HostData`, setSessionWrapper(setSession));
    }
    const postData = function(sessionId, session, setSession) {
        Ajax.request("POST", `/SharedSession/${sessionId}/HostData`, session, setSessionWrapper(setSession));
    }
    return { host, kick, close, getData, postData };
});