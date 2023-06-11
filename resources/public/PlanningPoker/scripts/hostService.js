namespace('planningPoker.HostService', {
    'gizmo-atheneum.namespaces.Ajax':'Ajax',
    'planningPoker.SharedSessionUtil':'Util'
}, ({ Ajax, Util }) => {
    const setSessionWrapper = function(setSession) {
        return ((respText) => {
            setSession(JSON.parse(respText));
        });
    };
    const host = function(hostName, setSession, onError) {
        Ajax.request("POST","/api/v1/session/host",Util.buildSession({ nameTag },{}),{
            success:setSessionWrapper(setSession),
            failure:(({ requestedFile, status, statusText, responseText }) => {
                onError(requestedFile, status, statusText, responseText);
            })
        });
    };
    const kick = function(sessionId, guestKey, setSession, onError) {
        Ajax.request("DELETE",`/api/v1/session/${sessionId}/kick/${guestKey}}`, {
            success:setSessionWrapper(setSession),
            failure:((error) => {
                onError(error);
            })
        });
    };
    const close = function(sessionId, setSession, onError) {
        Ajax.request("DELETE",`/api/v1/session/${sessionId}/close`, {
            success:setSessionWrapper(setSession),
            failure:((error) => {
                onError(error);
            })
        });
    };
    const getData = function(sessionId, setSession, onError) {
        return Ajax.poll("GET",`/api/v1/session/${sessionId}/host-data`, 500, {
            success:setSessionWrapper(setSession),
            failure:((error) => {
                onError(error);
            })
        });
    }
    const postData = function(sessionId, session, setSession, onError) {
        Ajax.request("POST", `/api/v1/session/${sessionId}/host-data`, Object.entries(session.guests).reduce(((out, [guestKey, guest]) => {
            out[guestKey] = Util.buildGuest({
                nameTag: session.guest.nameTag,
                state: session.guest.state
            });
            return out;
        }), Util.buildSession({
            nameTag: session.host.nameTag,
            state: session.host.state
        },{
            state: session.context.state
        })), {
            success:setSessionWrapper(setSession),
            failure:((error) => {
                onError(error);
            })
        });
    }
    return { host, kick, close, getData, postData };
});