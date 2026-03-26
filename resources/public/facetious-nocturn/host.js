namespace("facetious-nocturn.Host", {
  "facetious-nocturn.Callbacks": "Callbacks",
  "facetious-nocturn.Client": "Client",
  "facetious-nocturn.Constants": "Constants"
}, ({ Callbacks, Client, Constants }) => {
  const Host = function(sessionId, hostId) {
    const client = new Client();
    this.get = function(onSuccess) {
      client.get(`${Constants.API_BASE}/sessions/${sessionId}/host/${hostId}`, Callbacks(onSuccess));
    };
    this.poll = function(onSuccess) {
      return client.poll(`${Constants.API_BASE}/sessions/${sessionId}/host/${hostId}`, Constants.POLLING_DELAY, Callbacks(onSuccess));
    };
    this.update = function(data, onSuccess) {
      const payload = {};
      if (data.hostData !== undefined) payload.hostData = data.hostData;
      if (data.commonData !== undefined) payload.commonData = data.commonData;
      client.put(`${Constants.API_BASE}/sessions/${sessionId}/host/${hostId}`, JSON.stringify(payload), Callbacks(onSuccess));
    };
    this.kick = function(guestId, onSuccess) {
      client.delete(`${Constants.API_BASE}/sessions/${sessionId}/host/${hostId}/kick/${guestId}`, Callbacks(onSuccess));
    };
    this.close = function(onSuccess) {
      client.delete(`${Constants.API_BASE}/sessions/${sessionId}/host/${hostId}/close`, Callbacks(onSuccess));
    }
    this.copyInvite = function() {
      const currentUrl = window.location.href.split("?")[0];
      navigator.clipboard.writeText(currentUrl + "?sessionId=" + sessionId);
    }
  };
  const newSession = function({ initialHostData, initialCommonData }, onSuccess) {
    const payload = {};
    if (initialHostData !== undefined) payload.initialHostData = initialHostData;
    if (initialCommonData !== undefined) payload.initialCommonData = initialCommonData;
    Client.request("post", `${Constants.API_BASE}/host`, JSON.stringify(payload), Callbacks(onSuccess));
  };
  const init = function(sessionId, hostId) {
    return new Host(sessionId, hostId);
  };
  return { newSession, init };
});