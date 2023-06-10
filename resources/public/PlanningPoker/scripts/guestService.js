namespace('planningPoker.GuestService', {
    'gizmo-atheneum.namespaces.Ajax':'Ajax',
    'planningPoker.SharedSessionUtil':'Util'
}, ({ Ajax, Util }) => {
    const setSessionWrapper = function(setSession) {
        return ((respText) => {
            setSession(JSON.parse(respText));
        });
    };
    const join = function(nameTag, sessionKey, setSession, onError) {
        Ajax.request("POST",`/api/v1/session/join/${sessionKey}`,Util.buildGuest({ nameTag }), {
            success:setSessionWrapper(setSession),
            failure:((error) => {
                onError(error);
            })
        });
    };
    const leave = function(sessionId, setSession, onError) {
        Ajax.request("DELETE",`/api/v1/session/${sessionId}/leave}`, {
            success: setSessionWrapper(setSession),
            failure:((error) => {
                onError(error);
            })
        });
    };
    const getData = function(sessionId, setSession, onError) {
        Ajax.poll("GET",`/api/v1/session/${sessionId}/guest-data`, 500, {
            success: setSessionWrapper(setSession),
            failure:((error) => {
                onError(error);
            })
        });
    }
    const postData = function(sessionId, session, setSession, onError) {
        Ajax.request("POST", `/api/v1/session/${sessionId}/guest-data`, Util.buildUserData({
            nameTag: session.guest.nameTag,
            state: session.guest.state
        },{
            state: session.context.state
        }), {
            success: setSessionWrapper(setSession),
            failure:((error) => {
                onError(error);
            })
        });
    }
    return { join, leave, getData, postData };
});