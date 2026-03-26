namespace("facetious-nocturn.Guest", {
  "facetious-nocturn.Callbacks": "Callbacks",
  "facetious-nocturn.Client": "Client",
  "facetious-nocturn.Constants": "Constants"
}, ({ Callbacks, Client, Constants }) => {
  const Guest = function(sessionId, guestId) {
    const client = new Client();
    this.get = function(onSuccess) {
      client.get(`${Constants.API_BASE}/sessions/${sessionId}/guest/${guestId}`, Callbacks(onSuccess));
    };
    this.poll = function(onSuccess, onError) {
      return client.poll(`${Constants.API_BASE}/sessions/${sessionId}/guest/${guestId}`, Constants.POLLING_DELAY, Callbacks(onSuccess, onError));
    };
    this.update = function(data, onSuccess, onError) {
      const payload = {};
      if (data.fromGuest !== undefined) payload.fromGuest = data.fromGuest;
      if (data.commonData !== undefined) payload.commonData = data.commonData;
      client.put(`${Constants.API_BASE}/sessions/${sessionId}/guest/${guestId}`, JSON.stringify(payload), Callbacks(onSuccess, onError));
    };
    this.leave = function(onSuccess) {
      client.delete(`${Constants.API_BASE}/sessions/${sessionId}/guest/${guestId}/leave`, Callbacks(onSuccess));
    };
  };
  const joinSession = function(sessionId, initialData, onSuccess) {
    Client.request("post", `${Constants.API_BASE}/sessions/${sessionId}/join`, JSON.stringify({initialData: initialData}), Callbacks(onSuccess));
  };
  const init = function(sessionId, guestId) {
    return new Guest(sessionId, guestId);
  };
  return { joinSession, init };
});