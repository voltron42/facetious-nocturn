namespace("facetious-nocturn.Host", {
  "gizmo-atheneum.namespaces.Ajax": "Ajax",
  "facetious-nocturn.Callbacks": "Callbacks",
  "facetious-nocturn.Constants": "Constants"
}, ({ Ajax, Callbacks, Constants }) => {
  const Host = function(sessionId, hostId) {
    this.get = function(onSuccess) {
      Ajax.get(`${Constants.API_BASE}/sessions/${sessionId}/${hostId}`, Callbacks(onSuccess));
    };
    this.poll = function(onSuccess) {
      return Ajax.poll('GET', `${Constants.API_BASE}/sessions/${sessionId}/${hostId}`, Constants.POLLING_DELAY, Callbacks(onSuccess));
    };
    this.update = function(data, onSuccess) {
      const payload = {};
      if (data.hostData !== undefined) payload.hostData = data.hostData;
      if (data.commonData !== undefined) payload.commonData = data.commonData;
      Ajax.put(`${Constants.API_BASE}/sessions/${sessionId}/${hostId}`, payload, Callbacks(onSuccess));
    };
    this.kick = function(guestId, onSuccess) {
      Ajax.delete(`${Constants.API_BASE}/sessions/${sessionId}/${hostId}/kick/${guestId}`, Callbacks(onSuccess));
    };
    this.close = function(onSuccess) {
      Ajax.delete(`${Constants.API_BASE}/sessions/${sessionId}/${hostId}/close`, Callbacks(onSuccess));
    }
  };
  const newSession = function({ initialHostData, initialCommonData }, onSuccess) {
    const payload = {};
    if (initialHostData !== undefined) payload.initialHostData = initialHostData;
    if (initialCommonData !== undefined) payload.initialCommonData = initialCommonData;
    Ajax.post(`${Constants.API_BASE}/sessions/host`, payload, Callbacks(onSuccess));
  };
  const init = function(sessionId, hostId) {
    return new Host(sessionId, hostId);
  };
  return { newSession, init };
});