namespace("facetious-nocturn.Guest", {
  "gizmo-atheneum.namespaces.Ajax": "Ajax",
  "facetious-nocturn.Callbacks": "Callbacks",
  "facetious-nocturn.Constants": "Constants"
}, ({ Ajax, Callbacks, Constants }) => {
  const Guest = function(sessionId, guestId) {
    this.get = function(onSuccess) {
      Ajax.get(`${Constants.API_BASE}/sessions/${sessionId}/guest/${guestId}`, Callbacks(onSuccess));
    };
    this.poll = function(onSuccess, onError) {
      return Ajax.poll('GET', `${Constants.API_BASE}/sessions/${sessionId}/guest/${guestId}`, Constants.POLLING_DELAY, Callbacks(onSuccess, onError));
    };
    this.update = function(data, onSuccess, onError) {
      const payload = {};
      if (data.guestData !== undefined) payload.guestData = data.guestData;
      if (data.commonData !== undefined) payload.commonData = data.commonData;
      Ajax.put(`${Constants.API_BASE}/sessions/${sessionId}/guest/${guestId}`, JSON.stringify(payload), Callbacks(onSuccess, onError));
    };
    this.leave = function(onSuccess) {
      Ajax.delete(`${Constants.API_BASE}/sessions/${sessionId}/guest/${guestId}/leave`, Callbacks(onSuccess));
    };
  };
  const joinSession = function(sessionId, initialData, onSuccess) {
    Ajax.post(`${Constants.API_BASE}/sessions/${sessionId}/join`, JSON.stringify({initialData: initialData}), Callbacks(onSuccess));
  };
  const init = function(sessionId, guestId) {
    return new Guest(sessionId, guestId);
  };
  return { joinSession, init };
});