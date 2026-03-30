namespace("spyfall.Spyfall", {
  "common.Utilities": "Utilities",
  "facetious-nocturn.Host": "Host",
  "spyfall.SpyfallHost":"SpyfallHost",
  "spyfall.SpyfallGuest":"SpyfallGuest",
}, ({ Utilities, Host, SpyfallHost, SpyfallGuest }) => {
  const hostNewSession = function() {
    Host.newSession({}, ({ sessionId, hostId }) => {
      Utilities.appendQueryToUrl({ sessionId, hostId });
    });
  };
  return function(props) {
    const { sessionId, hostId, guestId } = props.query;
    if (sessionId && hostId) {
      return <SpyfallHost sessionId={sessionId} hostId={hostId}/>;
    } else if (sessionId) {
      return <SpyfallGuest sessionId={sessionId} guestId={guestId}/>;
    } else {
      return <div className="d-flex flex-column align-items-center justify-content-center h-100">
        <h1 className="mb-4">Spyfall</h1> 
        <button className="btn btn-primary" onClick={() => hostNewSession()}>Host New Session</button>
      </div>;
    }
  }
});